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
import se.umu.calu0217.strive.data.mappers.toDomainModel
import se.umu.calu0217.strive.data.mappers.toEntity
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ExerciseRepository using Room database and REST API.
 * Manages exercise data with local caching and API synchronization.
 * @param exerciseDao DAO for local exercise database operations.
 * @param apiService API service for fetching exercise data.
 * @param context Application context for accessing resources.
 * @author Carl Lundholm
 */
@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val apiService: ExerciseApiService,
    @ApplicationContext private val context: Context
) : ExerciseRepository {


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

                    exercises.map { it.toEntity() }
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
        if (remote.isEmpty()) return
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
                    } catch (e: Exception) {
                        Log.d("ExerciseRepository", "Failed to replace dataset: ${e.localizedMessage}")
                    }
                } else {
                    Log.d("ExerciseRepository", "Remote replacement fetch returned empty; keeping local seed.")
                }
            }
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

                val exerciseEntities = exercises.map { it.toEntity() }

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

// Custom exception for internal data errors
private class DataException(message: String) : Exception(message)
