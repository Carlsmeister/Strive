package se.umu.calu0217.strive.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import se.umu.calu0217.strive.data.local.entities.*

/**
 * Data Access Object for workout session-related database operations.
 * Manages workout sessions, including active and completed sessions.
 */
@Dao
interface WorkoutSessionDao {
    /**
     * Retrieves all workout sessions, ordered by start time (most recent first).
     *
     * @return A Flow emitting a list of all workout sessions.
     */
    @Query("SELECT * FROM workout_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    /**
     * Retrieves a specific workout session by its unique identifier.
     *
     * @param id The unique identifier of the workout session.
     * @return The workout session entity if found, null otherwise.
     */
    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): WorkoutSessionEntity?

    /**
     * Retrieves the currently active workout session (one that hasn't ended yet).
     *
     * @return The active workout session entity if one exists, null otherwise.
     */
    @Query("SELECT * FROM workout_sessions WHERE endedAt IS NULL LIMIT 1")
    suspend fun getActiveSession(): WorkoutSessionEntity?

    /**
     * Inserts a new workout session into the database.
     *
     * @param session The workout session entity to insert.
     * @return The row ID of the newly inserted session.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    /**
     * Updates an existing workout session in the database.
     *
     * @param session The workout session entity with updated values.
     */
    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    /**
     * Deletes a workout session from the database.
     *
     * @param session The workout session entity to delete.
     */
    @Delete
    suspend fun deleteSession(session: WorkoutSessionEntity)
}

/**
 * Data Access Object for workout set-related database operations.
 * Manages individual sets performed during workout sessions.
 */
@Dao
interface WorkoutSetDao {
    /**
     * Inserts a new workout set into the database.
     *
     * @param workoutSet The workout set entity to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(workoutSet: WorkoutSetEntity)
}

/**
 * Data Access Object for run session-related database operations.
 * Manages running sessions, including active and completed runs.
 */
@Dao
interface RunSessionDao {
    /**
     * Retrieves all run sessions, ordered by start time (most recent first).
     *
     * @return A Flow emitting a list of all run sessions.
     */
    @Query("SELECT * FROM run_sessions ORDER BY startedAt DESC")
    fun getAllRunSessions(): Flow<List<RunSessionEntity>>

    /**
     * Retrieves a specific run session by its unique identifier.
     *
     * @param id The unique identifier of the run session.
     * @return The run session entity if found, null otherwise.
     */
    @Query("SELECT * FROM run_sessions WHERE id = :id")
    suspend fun getRunSessionById(id: Long): RunSessionEntity?

    /**
     * Retrieves the currently active run session (one that hasn't ended yet).
     *
     * @return The active run session entity if one exists, null otherwise.
     */
    @Query("SELECT * FROM run_sessions WHERE endedAt IS NULL LIMIT 1")
    suspend fun getActiveRunSession(): RunSessionEntity?

    /**
     * Inserts a new run session into the database.
     *
     * @param runSession The run session entity to insert.
     * @return The row ID of the newly inserted run session.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunSession(runSession: RunSessionEntity): Long

    /**
     * Updates an existing run session in the database.
     *
     * @param runSession The run session entity with updated values.
     */
    @Update
    suspend fun updateRunSession(runSession: RunSessionEntity)

    /**
     * Deletes a run session from the database.
     *
     * @param runSession The run session entity to delete.
     */
    @Delete
    suspend fun deleteRunSession(runSession: RunSessionEntity)
}

/**
 * Data Access Object for run point-related database operations.
 * Manages GPS location points collected during running sessions.
 */
@Dao
interface RunPointDao {

    /**
     * Inserts a new run point (GPS location data) into the database.
     *
     * @param runPoint The run point entity to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunPoint(runPoint: RunPointEntity)
}
