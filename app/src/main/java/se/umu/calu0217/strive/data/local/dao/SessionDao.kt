package se.umu.calu0217.strive.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import se.umu.calu0217.strive.data.local.entities.*

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE endedAt IS NULL LIMIT 1")
    suspend fun getActiveSession(): WorkoutSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Delete
    suspend fun deleteSession(session: WorkoutSessionEntity)
}

@Dao
interface WorkoutSetDao {
    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY setIndex")
    fun getSessionSets(sessionId: Long): Flow<List<WorkoutSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(workoutSet: WorkoutSetEntity)

    @Update
    suspend fun updateSet(workoutSet: WorkoutSetEntity)

    @Query("DELETE FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun deleteSessionSets(sessionId: Long)
}

@Dao
interface RunSessionDao {
    @Query("SELECT * FROM run_sessions ORDER BY startedAt DESC")
    fun getAllRunSessions(): Flow<List<RunSessionEntity>>

    @Query("SELECT * FROM run_sessions WHERE id = :id")
    suspend fun getRunSessionById(id: Long): RunSessionEntity?

    @Query("SELECT * FROM run_sessions WHERE endedAt IS NULL LIMIT 1")
    suspend fun getActiveRunSession(): RunSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunSession(runSession: RunSessionEntity): Long

    @Update
    suspend fun updateRunSession(runSession: RunSessionEntity)

    @Delete
    suspend fun deleteRunSession(runSession: RunSessionEntity)
}

@Dao
interface RunPointDao {
    @Query("SELECT * FROM run_points WHERE runId = :runId ORDER BY idx")
    fun getRunPoints(runId: Long): Flow<List<RunPointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunPoint(runPoint: RunPointEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunPoints(runPoints: List<RunPointEntity>)

    @Query("DELETE FROM run_points WHERE runId = :runId")
    suspend fun deleteRunPoints(runId: Long)
}
