package se.umu.calu0217.strive.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ExerciseApiService {

    @GET("exercises")
    suspend fun getAllExercises(
        @Query("limit") limit: Int = 100
    ): List<ExerciseDto>

    @GET("exercises/bodyPart/{bodyPart}")
    suspend fun getExercisesByBodyPart(
        @Path("bodyPart") bodyPart: String,
        @Query("limit") limit: Int = 50
    ): List<ExerciseDto>
}

@Serializable
data class ExerciseDto(
    val id: String,
    val name: String,
    val bodyPart: String,
    val equipment: String,
    val gifUrl: String,
    val target: String,
    val instructions: List<String>
)
