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

/**
 * Implementation of RunRepository using Room database.
 * Manages run/cycling/walking sessions and GPS tracking data.
 * @param runSessionDao DAO for run session operations.
 * @param runPointDao DAO for GPS point operations.
 * @author Carl Lundholm
 */
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
        if (lat.isNaN() || lng.isNaN()) return
        if (lat !in -90.0..90.0 || lng !in -180.0..180.0) return
        val runPoint = RunPointEntity(
            runId = runId,
            lat = lat,
            lng = lng,
            timestamp = System.currentTimeMillis()
        )
        runPointDao.insertRunPoint(runPoint)
    }

    override suspend fun updateRunSession(runId: Long, distance: Double, elapsedSec: Int, pace: Double) {
        val runSession = runSessionDao.getRunSessionById(runId)
        runSession?.let {
            val safeDistance = if (distance.isFinite() && distance >= 0.0) distance else it.distance
            val safeElapsed = if (elapsedSec >= 0) elapsedSec else it.elapsedSec
            val safePace = if (pace.isFinite() && pace >= 0.0) pace else it.pace
            val updatedSession = it.copy(
                distance = safeDistance,
                elapsedSec = safeElapsed,
                pace = safePace
            )
            runSessionDao.updateRunSession(updatedSession)
        }
    }

    override suspend fun finishRun(runId: Long, kcal: Int) {
        val runSession = runSessionDao.getRunSessionById(runId)
        runSession?.let {
            val updatedSession = it.copy(
                endedAt = System.currentTimeMillis(),
                kcal = kcal.coerceAtLeast(0)
            )
            runSessionDao.updateRunSession(updatedSession)
        }
    }

    override fun getAllRunSessions(): Flow<List<RunSession>> {
        return runSessionDao.getAllRunSessions().map { sessions ->
            sessions.map { it.toDomainModel(emptyList()) }
        }
    }

    override suspend fun deleteAllRunData() {
        runPointDao.deleteAllRunPoints()
        runSessionDao.deleteAllRunSessions()
    }

}

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
