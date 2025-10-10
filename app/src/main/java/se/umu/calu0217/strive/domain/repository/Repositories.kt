package se.umu.calu0217.strive.domain.repository

import kotlinx.coroutines.flow.Flow
import se.umu.calu0217.strive.domain.models.*

/**
 * Repository for exercise data operations.
 * Manages exercise database including seeding, searching, and image management.
 * @author Carl Lundholm
 */
interface ExerciseRepository {
    /**
     * Retrieves all exercises as a reactive flow.
     * @return Flow of exercise list that updates when data changes.
     * @author Carl Lundholm
     */
    fun getAllExercises(): Flow<List<Exercise>>

    /**
     * Retrieves a single exercise by ID.
     * @param id The exercise ID.
     * @return The exercise if found, null otherwise.
     * @author Carl Lundholm
     */
    suspend fun getExerciseById(id: Long): Exercise?

    /**
     * Searches exercises by name or body part.
     * @param query Search query string.
     * @return Flow of matching exercises.
     * @author Carl Lundholm
     */
    fun searchExercises(query: String): Flow<List<Exercise>>

    /**
     * Seeds the exercise database with initial data if empty.
     * @author Carl Lundholm
     */
    suspend fun seedExercises()

    /**
     * Attempts to fetch missing exercise images from the API.
     * @author Carl Lundholm
     */
    suspend fun backfillMissingExerciseImages()

    /**
     * Forces a complete refresh of exercise data from the API.
     * @author Carl Lundholm
     */
    suspend fun forceRefreshExercises()
}

/**
 * Repository for workout template and session data operations.
 * Manages templates, exercises within templates, and active workout sessions.
 * @author Carl Lundholm
 */
interface WorkoutRepository {
    /**
     * Retrieves all workout templates as a reactive flow.
     * @return Flow of template list with exercises that updates when data changes.
     * @author Carl Lundholm
     */
    fun getAllTemplates(): Flow<List<WorkoutTemplate>>

    /**
     * Retrieves a single template by ID with all its exercises.
     * @param id The template ID.
     * @return The template if found, null otherwise.
     * @author Carl Lundholm
     */
    suspend fun getTemplateById(id: Long): WorkoutTemplate?

    /**
     * Inserts a new workout template.
     * @param template The template to insert.
     * @return The ID of the newly created template.
     * @author Carl Lundholm
     */
    suspend fun insertTemplate(template: WorkoutTemplate): Long

    /**
     * Updates an existing workout template.
     * @param template The template with updated data.
     * @author Carl Lundholm
     */
    suspend fun updateTemplate(template: WorkoutTemplate)

    /**
     * Deletes a workout template and all its exercises.
     * @param template The template to delete.
     * @author Carl Lundholm
     */
    suspend fun deleteTemplate(template: WorkoutTemplate)

    /**
     * Adds an exercise to a workout template.
     * @param templateId ID of the template.
     * @param templateExercise The exercise configuration to add.
     * @author Carl Lundholm
     */
    suspend fun addExerciseToTemplate(templateId: Long, templateExercise: TemplateExercise)

    /**
     * Starts a new workout session from a template.
     * @param templateId ID of the template to use (0 for quick workout).
     * @return The ID of the newly created workout session.
     * @author Carl Lundholm
     */
    suspend fun startWorkout(templateId: Long): Long

    /**
     * Retrieves the currently active workout session if one exists.
     * @return The active session if found, null otherwise.
     * @author Carl Lundholm
     */
    suspend fun getActiveWorkoutSession(): WorkoutSession?

    /**
     * Records a completed set in a workout session.
     * @param sessionId ID of the workout session.
     * @param exerciseId ID of the exercise.
     * @param setIndex Index of the set (0-based).
     * @param repsDone Number of reps completed.
     * @param restSecActual Actual rest time taken in seconds.
     * @author Carl Lundholm
     */
    suspend fun completeSet(sessionId: Long, exerciseId: Long, setIndex: Int, repsDone: Int, restSecActual: Int)

    /**
     * Finishes a workout session and records calories burned.
     * @param sessionId ID of the session to finish.
     * @param kcal Calories burned during the workout.
     * @author Carl Lundholm
     */
    suspend fun finishWorkout(sessionId: Long, kcal: Int)

    /**
     * Retrieves all completed workout sessions as a reactive flow.
     * @return Flow of workout session list.
     * @author Carl Lundholm
     */
    fun getAllWorkoutSessions(): Flow<List<WorkoutSession>>

    /**
     * Retrieves a single workout session by ID.
     * @param id The session ID.
     * @return The session if found, null otherwise.
     * @author Carl Lundholm
     */
    suspend fun getWorkoutSessionById(id: Long): WorkoutSession?

    /**
     * Deletes all workout data including templates, sessions, and sets.
     * @author Carl Lundholm
     */
    suspend fun deleteAllWorkoutData()
}

/**
 * Repository for running/cycling/walking session data operations.
 * Manages GPS-tracked cardio sessions and route points.
 * @author Carl Lundholm
 */
interface RunRepository {
    /**
     * Starts a new run/cardio session.
     * @return The ID of the newly created session.
     * @author Carl Lundholm
     */
    suspend fun startRun(): Long

    /**
     * Retrieves the currently active run session if one exists.
     * @return The active session if found, null otherwise.
     * @author Carl Lundholm
     */
    suspend fun getActiveRunSession(): RunSession?

    /**
     * Adds a GPS point to a run session.
     * @param runId ID of the run session.
     * @param lat GPS latitude.
     * @param lng GPS longitude.
     * @author Carl Lundholm
     */
    suspend fun addRunPoint(runId: Long, lat: Double, lng: Double)

    /**
     * Updates run session statistics.
     * @param runId ID of the run session.
     * @param distance Total distance in meters.
     * @param elapsedSec Total elapsed time in seconds.
     * @param pace Current pace in minutes per kilometer.
     * @author Carl Lundholm
     */
    suspend fun updateRunSession(runId: Long, distance: Double, elapsedSec: Int, pace: Double)

    /**
     * Finishes a run session and records calories burned.
     * @param runId ID of the session to finish.
     * @param kcal Calories burned during the run.
     * @author Carl Lundholm
     */
    suspend fun finishRun(runId: Long, kcal: Int)

    /**
     * Retrieves all completed run sessions as a reactive flow.
     * @return Flow of run session list.
     * @author Carl Lundholm
     */
    fun getAllRunSessions(): Flow<List<RunSession>>

    /**
     * Deletes all run data including sessions and GPS points.
     * @author Carl Lundholm
     */
    suspend fun deleteAllRunData()
}
