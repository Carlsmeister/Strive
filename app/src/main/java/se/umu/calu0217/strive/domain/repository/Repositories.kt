package se.umu.calu0217.strive.domain.repository

import kotlinx.coroutines.flow.Flow
import se.umu.calu0217.strive.domain.models.*

interface ExerciseRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    suspend fun getExerciseById(id: Long): Exercise?
    fun searchExercises(query: String): Flow<List<Exercise>>
    fun getExercisesByBodyPart(bodyPart: String): Flow<List<Exercise>>
    suspend fun seedExercises()
    suspend fun backfillMissingExerciseImages()
    suspend fun forceRefreshExercises()
}

interface WorkoutRepository {
    fun getAllTemplates(): Flow<List<WorkoutTemplate>>
    suspend fun getTemplateById(id: Long): WorkoutTemplate?
    suspend fun insertTemplate(template: WorkoutTemplate): Long
    suspend fun updateTemplate(template: WorkoutTemplate)
    suspend fun deleteTemplate(template: WorkoutTemplate)
    suspend fun addExerciseToTemplate(templateId: Long, templateExercise: TemplateExercise)

    suspend fun startWorkout(templateId: Long): Long
    suspend fun getActiveWorkoutSession(): WorkoutSession?
    suspend fun completeSet(sessionId: Long, exerciseId: Long, setIndex: Int, repsDone: Int, restSecActual: Int)
    suspend fun finishWorkout(sessionId: Long, kcal: Int)
    fun getAllWorkoutSessions(): Flow<List<WorkoutSession>>
    suspend fun getWorkoutSessionById(id: Long): WorkoutSession?
}

interface RunRepository {
    suspend fun startRun(): Long
    suspend fun getActiveRunSession(): RunSession?
    suspend fun addRunPoint(runId: Long, lat: Double, lng: Double)
    suspend fun updateRunSession(runId: Long, distance: Double, elapsedSec: Int, pace: Double)
    suspend fun finishRun(runId: Long, kcal: Int)
    fun getAllRunSessions(): Flow<List<RunSession>>
    suspend fun getRunSessionById(id: Long): RunSession?
}
