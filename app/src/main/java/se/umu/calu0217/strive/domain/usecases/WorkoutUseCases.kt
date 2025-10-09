package se.umu.calu0217.strive.domain.usecases

import kotlinx.coroutines.flow.Flow
import se.umu.calu0217.strive.domain.models.WorkoutTemplate
import se.umu.calu0217.strive.domain.repository.WorkoutRepository
import javax.inject.Inject

/**
 * Retrieves all workout templates.
 * Returns a reactive flow that emits updated templates when data changes.
 * @author Carl Lundholm
 */
class GetWorkoutTemplatesUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<WorkoutTemplate>> {
        return workoutRepository.getAllTemplates()
    }
}

/**
 * Creates a new workout template.
 * @return The ID of the newly created template.
 * @author Carl Lundholm
 */
class CreateWorkoutTemplateUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(template: WorkoutTemplate): Long {
        return workoutRepository.insertTemplate(template)
    }
}

/**
 * Starts a new workout session from a template.
 * Creates a session record and prepares for exercise tracking.
 * @return The ID of the newly created workout session.
 * @author Carl Lundholm
 */
class StartWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(templateId: Long): Long {
        return workoutRepository.startWorkout(templateId)
    }
}

/**
 * Records a completed set in a workout session.
 * Updates the session with actual performance data.
 * @author Carl Lundholm
 */
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

/**
 * Finishes a workout session.
 * Marks the session as complete and records final statistics.
 * @author Carl Lundholm
 */
class FinishWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: Long, kcal: Int) {
        workoutRepository.finishWorkout(sessionId, kcal)
    }
}
