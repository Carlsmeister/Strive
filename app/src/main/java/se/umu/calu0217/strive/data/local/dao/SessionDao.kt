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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(workoutSet: WorkoutSetEntity)
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunPoint(runPoint: RunPointEntity)
}
