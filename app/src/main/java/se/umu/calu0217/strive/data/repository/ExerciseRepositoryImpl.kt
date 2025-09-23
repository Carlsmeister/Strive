package se.umu.calu0217.strive.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.umu.calu0217.strive.data.local.dao.ExerciseDao
import se.umu.calu0217.strive.data.local.entities.ExerciseEntity
import se.umu.calu0217.strive.data.remote.ExerciseApiService
import se.umu.calu0217.strive.data.remote.ExerciseDto
import se.umu.calu0217.strive.domain.models.Exercise
import se.umu.calu0217.strive.domain.repository.ExerciseRepository
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

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

    override fun getExercisesByBodyPart(bodyPart: String): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByBodyPart(bodyPart).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun seedExercises() {
        try {
            // Check if exercises already exist in database first - SIMPLE APPROACH
            // Use a simpler check to avoid Flow collection issues
            val exercises = try {
                apiService.getAllExercises(limit = 50)
            } catch (e: UnknownHostException) {
                throw NetworkException("No internet connection. Please check your connection and try again.")
            } catch (e: SocketTimeoutException) {
                throw NetworkException("Connection timed out. Please try again.")
            } catch (e: Exception) {
                throw NetworkException("Failed to load exercises. Please try again later.")
            }

            if (exercises.isEmpty()) {
                throw DataException("No exercises available from the server.")
            }

            val exerciseEntities = exercises.map { dto ->
                ExerciseEntity(
                    name = dto.name,
                    bodyParts = listOf(dto.bodyPart, dto.target),
                    equipment = dto.equipment,
                    instructions = dto.instructions,
                    imageUrl = dto.gifUrl
                )
            }

            exerciseDao.insertExercises(exerciseEntities)
        } catch (e: NetworkException) {
            throw e
        } catch (e: DataException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to load exercises: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}

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
    return Exercise(
        id = id.hashCode().toLong(), // Convert string ID to long
        name = name,
        bodyParts = listOf(bodyPart, target),
        equipment = equipment,
        instructions = instructions,
        imageUrl = gifUrl
    )
}
