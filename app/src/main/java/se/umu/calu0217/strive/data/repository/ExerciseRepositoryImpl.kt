package se.umu.calu0217.strive.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import se.umu.calu0217.strive.BuildConfig
import se.umu.calu0217.strive.data.local.dao.ExerciseDao
import se.umu.calu0217.strive.data.local.entities.ExerciseEntity
import se.umu.calu0217.strive.data.remote.ExerciseApiService
import se.umu.calu0217.strive.data.remote.ExerciseDto
import se.umu.calu0217.strive.domain.models.Exercise
import se.umu.calu0217.strive.domain.repository.ExerciseRepository
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.Request
import android.content.SharedPreferences
import okhttp3.Response

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val apiService: ExerciseApiService,
    @ApplicationContext private val context: Context
) : ExerciseRepository {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("strive_prefs", Context.MODE_PRIVATE)
    }
    private val httpProbeClient by lazy { OkHttpClient.Builder().build() }

    private data class ImageHost(val base: String, val ext: String)

    private fun getCachedImageHost(): ImageHost? {
        val base = prefs.getString("image_host_base", null)
        val ext = prefs.getString("image_host_ext", null)
        return if (base != null && ext != null) ImageHost(base, ext) else null
    }

    private fun cacheImageHost(host: ImageHost) {
        prefs.edit().putString("image_host_base", host.base).putString("image_host_ext", host.ext).apply()
    }

    private fun resolveImageHost(sampleIds: List<String>): ImageHost? {
        // Return cached if available
        getCachedImageHost()?.let { return it }
        val candidates = listOf(
            ImageHost("https://d205bpvrqc9yn1.cloudfront.net/", ".gif"),
            ImageHost("https://v2.exercisedb.io/image/", ".png"),
            ImageHost("https://exercisedb.io/image/", ".png"),
            ImageHost("https://exercisedb.p.rapidapi.com/image/", ".png")
        )
        for (candidate in candidates) {
            val sampleUrl = candidate.base + (sampleIds.firstOrNull() ?: "0001") + candidate.ext
            try {
                val req = Request.Builder().url(sampleUrl).head().build()
                httpProbeClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        Log.d("ExerciseRepository", "Resolved image host: ${candidate.base} *${candidate.ext}")
                        cacheImageHost(candidate)
                        return candidate
                    } else {
                        Log.d("ExerciseRepository", "Image host candidate failed ${candidate.base} status=${resp.code}")
                    }
                }
            } catch (e: Exception) {
                Log.d("ExerciseRepository", "Image host candidate error ${candidate.base}: ${e.localizedMessage}")
            }
        }
        Log.d("ExerciseRepository", "No image host resolved; images will remain null")
        return null
    }

    override fun getAllExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getExerciseById(id: Long): Exercise? {
        return exerciseDao.getExerciseById(id)?.toDomainModel()
    }

    override fun searchExercises(query: String): Flow<List<Exercise>> {
        return exerciseDao.searchExercises(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getExercisesByBodyPart(bodyPart: String): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByBodyPart(bodyPart).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun seedExercises() {
        try {
            val existingCount = exerciseDao.countExercises()
            if (existingCount > 0) {
                Log.d("ExerciseRepository", "Already have $existingCount exercises, skipping seed")
                return
            }

            // Prefer remote if we have an API key, otherwise fall back to local assets
            val useRemote = BuildConfig.RAPIDAPI_KEY.isNotBlank()

            val exerciseEntities: List<ExerciseEntity> = if (useRemote) {
                // Try to get all exercises by fetching from different body parts to get complete dataset
                val aggregated = fetchAllBodyPartExercises()
                val exercises = if (aggregated.isNotEmpty()) {
                    Log.d("ExerciseRepository", "Using aggregated exercises from body parts: ${aggregated.size}")
                    aggregated
                } else {
                    // Fallback to direct API call with higher limit
                    Log.d("ExerciseRepository", "Falling back to direct API call")
                    try {
                        apiService.getAllExercises(limit = 1000)
                    } catch (e: Exception) {
                        Log.d("ExerciseRepository", "Direct API call failed: ${e.localizedMessage}")
                        emptyList()
                    }
                }

                if (exercises.isNotEmpty()) {
                    Log.d("ExerciseRepository", "Processing ${exercises.size} exercises from API")

                    exercises.map { dto ->
                        // Priority order for image URLs with RapidAPI:
                        // 1. gifUrl from API response (direct URLs, no auth needed)
                        // 2. First image from images array (if available)
                        // 3. Set to null for clean placeholder fallback
                        val imageUrl = when {
                            !dto.gifUrl.isNullOrBlank() -> {
                                Log.d("ExerciseRepository", "Using direct gifUrl: ${dto.gifUrl}")
                                dto.gifUrl
                            }
                            dto.images.isNotEmpty() -> {
                                Log.d("ExerciseRepository", "Using first image: ${dto.images.first()}")
                                dto.images.first()
                            }
                            else -> {
                                Log.d("ExerciseRepository", "No image URL available for: ${dto.name}")
                                null // Clean placeholder fallback
                            }
                        }

                        ExerciseEntity(
                            id = dto.id.hashCode().toLong(),
                            name = dto.name,
                            bodyParts = listOf(dto.bodyPart, dto.target) + dto.secondaryMuscles.takeIf { it.isNotEmpty() }.orEmpty(),
                            equipment = dto.equipment,
                            instructions = if (dto.instructions.isNotEmpty()) dto.instructions else listOf("No instructions provided."),
                            imageUrl = imageUrl
                        )
                    }
                } else {
                    Log.d("ExerciseRepository", "No remote exercises found, using local assets")
                    loadExercisesFromAssets()
                }
            } else {
                Log.d("ExerciseRepository", "No API key, using local assets")
                loadExercisesFromAssets()
            }

            if (exerciseEntities.isEmpty()) {
                throw DataException("No exercises available (remote and local sources were empty).")
            }

            exerciseDao.insertExercises(exerciseEntities)
            Log.d("ExerciseRepository", "Seeded ${exerciseEntities.size} exercises")

            // Log a sample of stored image URLs
            try {
                val sample = exerciseDao.getAllExercisesOnce().take(3).map { "${it.name} -> ${it.imageUrl}" }
                Log.d("ExerciseRepository", "Sample exercises: $sample")
            } catch (_: Exception) {}
        } catch (e: NetworkException) {
            throw e
        } catch (e: DataException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to load exercises: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private fun loadExercisesFromAssets(): List<ExerciseEntity> {
        return try {
            val jsonString = context.assets.open("seed_exercises.json").bufferedReader().use { it.readText() }
            val json = Json { ignoreUnknownKeys = true }
            val seeds = json.decodeFromString<List<SeedExercise>>(jsonString)
            seeds.map { seed ->
                ExerciseEntity(
                    name = seed.name,
                    bodyParts = seed.bodyParts,
                    equipment = seed.equipment,
                    instructions = seed.instructions,
                    imageUrl = seed.imageUrl
                )
            }
        } catch (e: IOException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun backfillMissingExerciseImages() {
        // Only attempt if we have an API key
        if (BuildConfig.RAPIDAPI_KEY.isBlank()) return
        val missing = try { exerciseDao.getExercisesMissingImages() } catch (e: Exception) { return }
        if (missing.isEmpty()) return
        val remote = try { apiService.getAllExercises(limit = 200) } catch (e: Exception) { emptyList() }
        if (remote.isEmpty()) {
            // Even if remote empty, we can still construct deterministic URLs based on IDs if names line up later
            missing.forEach { entity ->
                // If the name matches one of the remote exercises (should not happen here) else attempt id fallback not possible (we lost original id), skip
            }
            return
        }
        // Map by lowercase name for best-effort match
        val byName = remote.associateBy { it.name.trim().lowercase() }
        var updatedCount = 0
        missing.forEach { entity ->
            val match = byName[entity.name.trim().lowercase()]
            if (match != null) {
                val imageUrl = match.gifUrl ?: "https://d205bpvrqc9yn1.cloudfront.net/${match.id}.gif"
                val updated = entity.copy(imageUrl = imageUrl)
                try {
                    exerciseDao.updateExercise(updated)
                    updatedCount++
                } catch (_: Exception) { }
            }
        }
        Log.d("ExerciseRepository", "Backfill complete. Updated $updatedCount/${missing.size} exercises with image URLs")
        // If we failed to update any AND all existing are missing images or dataset is tiny (likely local seed), attempt a full remote refresh/replace
        if (updatedCount == 0) {
            val total = try { exerciseDao.countExercises() } catch (_: Exception) { 0 }
            if (total > 0 && (missing.size == total || total <= 10)) {
                Log.d("ExerciseRepository", "Attempting full remote replacement of local seed dataset (total=$total, missing=${missing.size})")
                val remoteFull = try { apiService.getAllExercises(limit = 200) } catch (e: Exception) { emptyList() }
                if (remoteFull.isNotEmpty()) {
                    val entities = remoteFull.map { dto ->
                        val imageUrl = dto.gifUrl ?: "https://d205bpvrqc9yn1.cloudfront.net/${dto.id}.gif"
                        ExerciseEntity(
                            id = dto.id.hashCode().toLong(),
                            name = dto.name,
                            bodyParts = listOf(dto.bodyPart, dto.target) + dto.secondaryMuscles.takeIf { it.isNotEmpty() }.orEmpty(),
                            equipment = dto.equipment,
                            instructions = if (dto.instructions.isNotEmpty()) dto.instructions else listOf("No instructions provided."),
                            imageUrl = imageUrl
                        )
                    }
                    try {
                        exerciseDao.deleteAllExercises()
                        exerciseDao.insertExercises(entities)
                        Log.d("ExerciseRepository", "Replaced local seed with remote dataset size=${entities.size}")
                        try { Log.d("ExerciseRepository", "Replacement sample=" + exerciseDao.getAllExercisesOnce().take(3).map { it.imageUrl }) } catch (_: Exception) {}
                    } catch (e: Exception) {
                        Log.d("ExerciseRepository", "Failed to replace dataset: ${e.localizedMessage}")
                    }
                } else {
                    Log.d("ExerciseRepository", "Remote replacement fetch returned empty; keeping local seed.")
                }
            }
        }
    }

    private fun fetchOpenSourceDataset(max: Int = 500): List<ExerciseDto> {
        val url = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/dist/exercises.json"
        return try {
            val req = Request.Builder().url(url).get().build()
            httpProbeClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.d("ExerciseRepository", "Open source dataset HTTP ${resp.code}")
                    return emptyList()
                }
                val bodyStr = resp.body?.string() ?: return emptyList()
                val json = Json { ignoreUnknownKeys = true }
                val all = json.decodeFromString<List<ExerciseDto>>(bodyStr)
                val limited = all.take(max)
                Log.d("ExerciseRepository", "Loaded open source dataset size=${limited.size}")
                limited
            }
        } catch (e: Exception) {
            Log.d("ExerciseRepository", "Failed to load open source dataset: ${e.localizedMessage}")
            emptyList()
        }
    }

    private suspend fun fetchAllBodyPartExercises(): List<ExerciseDto> {
        return try {
            val parts = apiService.getBodyPartList()
            val collected = mutableMapOf<String, ExerciseDto>()
            for (part in parts) {
                try {
                    // Increase limit per body part to get more exercises
                    val partExercises = apiService.getExercisesByBodyPart(part, limit = 1000)
                    partExercises.forEach { dto -> collected[dto.id] = dto }
                    Log.d("ExerciseRepository", "Fetched ${partExercises.size} exercises for $part (total: ${collected.size})")
                } catch (e: Exception) {
                    Log.d("ExerciseRepository", "Failed bodyPart $part: ${e.localizedMessage}")
                }
                // Don't limit the total - get as many as possible
                if (collected.size > 2000) break // Only break if we get a huge amount
            }
            Log.d("ExerciseRepository", "Aggregated exercises across body parts size=${collected.size}")
            collected.values.toList()
        } catch (e: Exception) {
            Log.d("ExerciseRepository", "fetchAllBodyPartExercises error: ${e.localizedMessage}")
            emptyList()
        }
    }

    // Add method to force refresh exercises
    override suspend fun forceRefreshExercises() {
        if (BuildConfig.RAPIDAPI_KEY.isBlank()) {
            Log.d("ExerciseRepository", "No API key available for refresh")
            return
        }

        try {
            Log.d("ExerciseRepository", "Force refreshing exercises from API...")
            // Clear existing exercises
            exerciseDao.deleteAllExercises()

            // Fetch fresh data
            val aggregated = fetchAllBodyPartExercises()
            val exercises = if (aggregated.isNotEmpty()) {
                Log.d("ExerciseRepository", "Using aggregated exercises from body parts: ${aggregated.size}")
                aggregated
            } else {
                // Fallback to direct API call with higher limit
                Log.d("ExerciseRepository", "Falling back to direct API call")
                apiService.getAllExercises(limit = 1000)
            }

            if (exercises.isNotEmpty()) {
                Log.d("ExerciseRepository", "Processing ${exercises.size} exercises from API")

                val exerciseEntities = exercises.map { dto ->
                    // Priority order for image URLs with RapidAPI:
                    // 1. gifUrl from API response (direct URLs, no auth needed)
                    // 2. First image from images array (if available)
                    // 3. Set to null for clean placeholder fallback
                    val imageUrl = when {
                        !dto.gifUrl.isNullOrBlank() -> {
                            Log.d("ExerciseRepository", "Using direct gifUrl: ${dto.gifUrl}")
                            dto.gifUrl
                        }
                        dto.images.isNotEmpty() -> {
                            Log.d("ExerciseRepository", "Using first image: ${dto.images.first()}")
                            dto.images.first()
                        }
                        else -> {
                            Log.d("ExerciseRepository", "No image URL available for: ${dto.name}")
                            null // Clean placeholder fallback
                        }
                    }

                    ExerciseEntity(
                        id = dto.id.hashCode().toLong(),
                        name = dto.name,
                        bodyParts = listOf(dto.bodyPart, dto.target) + dto.secondaryMuscles.takeIf { it.isNotEmpty() }.orEmpty(),
                        equipment = dto.equipment,
                        instructions = if (dto.instructions.isNotEmpty()) dto.instructions else listOf("No instructions provided."),
                        imageUrl = imageUrl
                    )
                }

                exerciseDao.insertExercises(exerciseEntities)
                Log.d("ExerciseRepository", "Force refresh complete! Loaded ${exerciseEntities.size} exercises")
            }
        } catch (e: Exception) {
            Log.e("ExerciseRepository", "Force refresh failed: ${e.localizedMessage}")
            throw e
        }
    }
}

@Serializable
private data class SeedExercise(
    val name: String,
    val bodyParts: List<String>,
    val equipment: String,
    val instructions: List<String>,
    val imageUrl: String? = null
)

// Custom exception classes for better error handling
class NetworkException(message: String) : Exception(message)
class DataException(message: String) : Exception(message)

// Extension function to convert entity to domain model
private fun ExerciseEntity.toDomainModel(): Exercise {
    return Exercise(
        id = id,
        name = name,
        bodyParts = bodyParts,
        equipment = equipment,
        instructions = instructions,
        imageUrl = imageUrl
    )
}

// Extension function to convert DTO to domain model (if needed)
private fun ExerciseDto.toDomainModel(): Exercise {
    // Domain conversion now leaves imageUrl null; enrichment handled earlier
    return Exercise(
        id = id.hashCode().toLong(),
        name = name,
        bodyParts = listOf(bodyPart, target) + secondaryMuscles.takeIf { it.isNotEmpty() }.orEmpty(),
        equipment = equipment,
        instructions = instructions,
        imageUrl = gifUrl // may still be null
    )
}
