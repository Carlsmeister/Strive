package se.umu.calu0217.strive.domain.usecases

import kotlinx.coroutines.flow.Flow
import se.umu.calu0217.strive.domain.models.Exercise
import se.umu.calu0217.strive.domain.repository.ExerciseRepository
import javax.inject.Inject

/**
 * Retrieves all exercises from the database.
 * Returns a reactive flow that emits updated exercises when data changes.
 * @author Carl Lundholm
 */
class GetExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    operator fun invoke(): Flow<List<Exercise>> {
        return exerciseRepository.getAllExercises()
    }
}

/**
 * Searches exercises by name or body part.
 * Returns empty query results in all exercises, otherwise filtered results.
 * @author Carl Lundholm
 */
class SearchExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    operator fun invoke(query: String): Flow<List<Exercise>> {
        return if (query.isBlank()) {
            exerciseRepository.getAllExercises()
        } else {
            exerciseRepository.searchExercises(query)
        }
    }
}

/**
 * Seeds the exercise database with initial data if empty.
 * Fetches exercise data from API and stores locally.
 * @author Carl Lundholm
 */
class SeedExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke() {
        exerciseRepository.seedExercises()
    }
}

/**
 * Attempts to fetch missing exercise images from the API.
 * Backfills any exercises that don't have image URLs.
 * @author Carl Lundholm
 */
class BackfillMissingExerciseImagesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke() {
        exerciseRepository.backfillMissingExerciseImages()
    }
}

/**
 * Forces a complete refresh of exercise data from the API.
 * Replaces all existing exercise data with fresh data.
 * @author Carl Lundholm
 */
class ForceRefreshExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke() {
        exerciseRepository.forceRefreshExercises()
    }
}
