package se.umu.calu0217.strive.domain.usecases

import kotlinx.coroutines.flow.Flow
import se.umu.calu0217.strive.domain.models.RunSession
import se.umu.calu0217.strive.domain.repository.RunRepository
import javax.inject.Inject

/**
 * Starts a new run/cycling/walking session.
 * Creates a session record and prepares for GPS tracking.
 * @return The ID of the newly created run session.
 * @author Carl Lundholm
 */
class StartRunUseCase @Inject constructor(
    private val runRepository: RunRepository
) {
    suspend operator fun invoke(): Long {
        return runRepository.startRun()
    }
}

/**
 * Retrieves the currently active run session if one exists.
 * Used to resume tracking after app restart.
 * @author Carl Lundholm
 */
class GetActiveRunSessionUseCase @Inject constructor(
    private val runRepository: RunRepository
) {
    suspend operator fun invoke(): RunSession? {
        return runRepository.getActiveRunSession()
    }
}

/**
 * Adds a GPS location point to a run session.
 * Records the route taken during the activity.
 * @author Carl Lundholm
 */
class AddRunPointUseCase @Inject constructor(
    private val runRepository: RunRepository
) {
    suspend operator fun invoke(runId: Long, lat: Double, lng: Double) {
        runRepository.addRunPoint(runId, lat, lng)
    }
}

/**
 * Updates run session statistics in real-time.
 * Called as GPS data is received to keep session data current.
 * @author Carl Lundholm
 */
class UpdateRunSessionUseCase @Inject constructor(
    private val runRepository: RunRepository
) {
    suspend operator fun invoke(runId: Long, distance: Double, elapsedSec: Int, pace: Double) {
        runRepository.updateRunSession(runId, distance, elapsedSec, pace)
    }
}

/**
 * Finishes a run session.
 * Marks the session as complete and records final statistics including calories.
 * @author Carl Lundholm
 */
class FinishRunUseCase @Inject constructor(
    private val runRepository: RunRepository
) {
    suspend operator fun invoke(runId: Long, kcal: Int) {
        runRepository.finishRun(runId, kcal)
    }
}

/**
 * Retrieves all completed run sessions.
 * Returns a reactive flow that emits updated sessions when data changes.
 * @author Carl Lundholm
 */
class GetAllRunSessionsUseCase @Inject constructor(
    private val runRepository: RunRepository
) {
    operator fun invoke(): Flow<List<RunSession>> {
        return runRepository.getAllRunSessions()
    }
}
