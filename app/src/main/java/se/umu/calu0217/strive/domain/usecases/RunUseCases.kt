package se.umu.calu0217.strive.domain.usecases

import kotlinx.coroutines.flow.Flow
import se.umu.calu0217.strive.domain.models.RunSession
import se.umu.calu0217.strive.domain.repository.RunRepository
import javax.inject.Inject

class StartRunUseCase @Inject constructor(
    private val runRepository: RunRepository
) {
    suspend operator fun invoke(): Long {
        return runRepository.startRun()
    }
}

class GetActiveRunSessionUseCase @Inject constructor(
    private val runRepository: RunRepository
) {
    suspend operator fun invoke(): RunSession? {
        return runRepository.getActiveRunSession()
    }
}

class AddRunPointUseCase @Inject constructor(
    private val runRepository: RunRepository
) {
    suspend operator fun invoke(runId: Long, lat: Double, lng: Double) {
        runRepository.addRunPoint(runId, lat, lng)
    }
}

class UpdateRunSessionUseCase @Inject constructor(
    private val runRepository: RunRepository
) {
    suspend operator fun invoke(runId: Long, distance: Double, elapsedSec: Int, pace: Double) {
        runRepository.updateRunSession(runId, distance, elapsedSec, pace)
    }
}

class FinishRunUseCase @Inject constructor(
    private val runRepository: RunRepository
) {
    suspend operator fun invoke(runId: Long, kcal: Int) {
        runRepository.finishRun(runId, kcal)
    }
}

class GetAllRunSessionsUseCase @Inject constructor(
    private val runRepository: RunRepository
) {
    operator fun invoke(): Flow<List<RunSession>> {
        return runRepository.getAllRunSessions()
    }
}
