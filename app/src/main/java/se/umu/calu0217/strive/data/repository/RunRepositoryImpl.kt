package se.umu.calu0217.strive.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.umu.calu0217.strive.data.local.dao.RunSessionDao
import se.umu.calu0217.strive.data.local.dao.RunPointDao
import se.umu.calu0217.strive.data.local.entities.RunSessionEntity
import se.umu.calu0217.strive.data.local.entities.RunPointEntity
import se.umu.calu0217.strive.domain.models.RunSession
import se.umu.calu0217.strive.domain.repository.RunRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunRepositoryImpl @Inject constructor(
    private val runSessionDao: RunSessionDao,
    private val runPointDao: RunPointDao
) : RunRepository {

    override suspend fun startRun(): Long {
        val runSession = RunSessionEntity(
            startedAt = System.currentTimeMillis(),
            endedAt = null,
            distance = 0.0,
            elapsedSec = 0,
            kcal = 0,
            pace = 0.0
        )
        return runSessionDao.insertRunSession(runSession)
    }

    override suspend fun getActiveRunSession(): RunSession? {
        return runSessionDao.getActiveRunSession()?.toDomainModel(emptyList())
    }

    override suspend fun addRunPoint(runId: Long, lat: Double, lng: Double) {
        val runPoint = RunPointEntity(
            runId = runId,
            idx = 0, // Will be calculated based on existing points
            lat = lat,
            lng = lng,
            timestamp = System.currentTimeMillis()
        )
        runPointDao.insertRunPoint(runPoint)
    }

    override suspend fun updateRunSession(runId: Long, distance: Double, elapsedSec: Int, pace: Double) {
        val runSession = runSessionDao.getRunSessionById(runId)
        runSession?.let {
            val updatedSession = it.copy(
                distance = distance,
                elapsedSec = elapsedSec,
                pace = pace
            )
            runSessionDao.updateRunSession(updatedSession)
        }
    }

    override suspend fun finishRun(runId: Long, kcal: Int) {
        val runSession = runSessionDao.getRunSessionById(runId)
        runSession?.let {
            val updatedSession = it.copy(
                endedAt = System.currentTimeMillis(),
                kcal = kcal
            )
            runSessionDao.updateRunSession(updatedSession)
        }
    }

    override fun getAllRunSessions(): Flow<List<RunSession>> {
        return runSessionDao.getAllRunSessions().map { sessions ->
            sessions.map { it.toDomainModel(emptyList()) }
        }
    }

    override suspend fun getRunSessionById(id: Long): RunSession? {
        return runSessionDao.getRunSessionById(id)?.toDomainModel(emptyList())
    }
}

// Extension function for entity to domain model conversion
private fun RunSessionEntity.toDomainModel(points: List<se.umu.calu0217.strive.domain.models.RunPoint>): RunSession {
    return RunSession(
        id = id,
        startedAt = startedAt,
        endedAt = endedAt,
        distance = distance,
        elapsedSec = elapsedSec,
        kcal = kcal,
        pace = pace,
        points = points
    )
}
