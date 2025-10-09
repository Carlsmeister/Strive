package se.umu.calu0217.strive.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API service for fetching exercise data from ExerciseDB API.
 * @author Carl Lundholm
 */
interface ExerciseApiService {

    /**
     * Fetches all exercises with an optional limit.
     * @param limit Maximum number of exercises to retrieve (default 100).
     * @return List of exercise DTOs from the API.
     * @author Carl Lundholm
     */
    @GET("exercises")
    suspend fun getAllExercises(
        @Query("limit") limit: Int = 100
    ): List<ExerciseDto>

    /**
     * Fetches exercises targeting a specific body part.
     * @param bodyPart The body part to filter by (e.g., "chest", "legs").
     * @param limit Maximum number of exercises to retrieve (default 50).
     * @return List of exercise DTOs for the specified body part.
     * @author Carl Lundholm
     */
    @GET("exercises/bodyPart/{bodyPart}")
    suspend fun getExercisesByBodyPart(
        @Path("bodyPart") bodyPart: String,
        @Query("limit") limit: Int = 50
    ): List<ExerciseDto>

    /**
     * Fetches the list of available body parts from the API.
     * @return List of body part strings.
     * @author Carl Lundholm
     */
    @GET("exercises/bodyPartList")
    suspend fun getBodyPartList(): List<String>
}

/**
 * Data transfer object for exercise data from the API.
 * @property id Unique identifier from the API.
 * @property name Name of the exercise.
 * @property bodyPart Primary body part targeted.
 * @property equipment Equipment required.
 * @property gifUrl URL to animated GIF demonstration.
 * @property images List of image URLs.
 * @property target Primary target muscle.
 * @property secondaryMuscles List of secondary muscles worked.
 * @property instructions Step-by-step exercise instructions.
 * @author Carl Lundholm
 */
@Serializable
data class ExerciseDto(
    val id: String,
    val name: String,
    val bodyPart: String,
    val equipment: String,
    val gifUrl: String? = null,
    val images: List<String> = emptyList(),
    val target: String,
    val secondaryMuscles: List<String> = emptyList(),
    val instructions: List<String> = emptyList()
)
