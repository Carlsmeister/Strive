package se.umu.calu0217.strive.domain.usecases

import kotlinx.coroutines.flow.Flow
import se.umu.calu0217.strive.domain.models.Exercise
import se.umu.calu0217.strive.domain.repository.ExerciseRepository
import javax.inject.Inject

class GetExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    operator fun invoke(): Flow<List<Exercise>> {
        return exerciseRepository.getAllExercises()
    }
}

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

class GetExercisesByBodyPartUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    operator fun invoke(bodyPart: String): Flow<List<Exercise>> {
        return exerciseRepository.getExercisesByBodyPart(bodyPart)
    }
}

class SeedExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke() {
        exerciseRepository.seedExercises()
    }
}

class BackfillMissingExerciseImagesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke() {
        exerciseRepository.backfillMissingExerciseImages()
    }
}

class ForceRefreshExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke() {
        exerciseRepository.forceRefreshExercises()
    }
}
