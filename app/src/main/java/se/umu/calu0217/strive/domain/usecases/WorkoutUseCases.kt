package se.umu.calu0217.strive.domain.usecases

import kotlinx.coroutines.flow.Flow
import se.umu.calu0217.strive.domain.models.WorkoutTemplate
import se.umu.calu0217.strive.domain.models.WorkoutSession
import se.umu.calu0217.strive.domain.repository.WorkoutRepository
import javax.inject.Inject

class GetWorkoutTemplatesUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<WorkoutTemplate>> {
        return workoutRepository.getAllTemplates()
    }
}

class CreateWorkoutTemplateUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(template: WorkoutTemplate): Long {
        return workoutRepository.insertTemplate(template)
    }
}

class StartWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(templateId: Long): Long {
        return workoutRepository.startWorkout(templateId)
    }
}

class CompleteSetUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(
        sessionId: Long,
        exerciseId: Long,
        setIndex: Int,
        repsDone: Int,
        restSecActual: Int
    ) {
        workoutRepository.completeSet(sessionId, exerciseId, setIndex, repsDone, restSecActual)
    }
}

class FinishWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: Long, kcal: Int) {
        workoutRepository.finishWorkout(sessionId, kcal)
    }
}
