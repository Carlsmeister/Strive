package se.umu.calu0217.strive.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import se.umu.calu0217.strive.data.local.dao.*
import se.umu.calu0217.strive.data.local.entities.*
import se.umu.calu0217.strive.domain.models.*
import se.umu.calu0217.strive.domain.repository.WorkoutRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutTemplateDao: WorkoutTemplateDao,
    private val templateExerciseDao: TemplateExerciseDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val workoutSetDao: WorkoutSetDao,
    private val exerciseDao: ExerciseDao
) : WorkoutRepository {

    override fun getAllTemplates(): Flow<List<WorkoutTemplate>> {
        return workoutTemplateDao.getAllTemplates().map { templates ->
            templates.map { template ->
                val exercises = templateExerciseDao.getTemplateExercises(template.id)
                template.toDomainModel(emptyList()) // Will be loaded separately if needed
            }
        }
    }

    override suspend fun getTemplateById(id: Long): WorkoutTemplate? {
        val template = workoutTemplateDao.getTemplateById(id) ?: return null
        val exercises = templateExerciseDao.getTemplateExercises(id)
        // Convert Flow to List for this specific case
        return template.toDomainModel(emptyList()) // Simplified for now
    }

    override suspend fun insertTemplate(template: WorkoutTemplate): Long {
        val templateEntity = WorkoutTemplateEntity(
            id = template.id,
            name = template.name,
            createdAt = template.createdAt
        )
        val templateId = workoutTemplateDao.insertTemplate(templateEntity)

        // Insert template exercises
        val templateExercises = template.exercises.map { exercise ->
            TemplateExerciseEntity(
                templateId = templateId,
                exerciseId = exercise.exerciseId,
                sets = exercise.sets,
                reps = exercise.reps,
                restSec = exercise.restSec,
                position = exercise.position
            )
        }
        templateExerciseDao.insertTemplateExercises(templateExercises)

        return templateId
    }

    override suspend fun updateTemplate(template: WorkoutTemplate) {
        val templateEntity = WorkoutTemplateEntity(
            id = template.id,
            name = template.name,
            createdAt = template.createdAt
        )
        workoutTemplateDao.updateTemplate(templateEntity)

        // Update template exercises
        templateExerciseDao.deleteTemplateExercises(template.id)
        val templateExercises = template.exercises.map { exercise ->
            TemplateExerciseEntity(
                templateId = template.id,
                exerciseId = exercise.exerciseId,
                sets = exercise.sets,
                reps = exercise.reps,
                restSec = exercise.restSec,
                position = exercise.position
            )
        }
        templateExerciseDao.insertTemplateExercises(templateExercises)
    }

    override suspend fun deleteTemplate(template: WorkoutTemplate) {
        templateExerciseDao.deleteTemplateExercises(template.id)
        workoutTemplateDao.deleteTemplate(template.toEntity())
    }

    override suspend fun startWorkout(templateId: Long): Long {
        val session = WorkoutSessionEntity(
            templateId = templateId,
            startedAt = System.currentTimeMillis(),
            endedAt = null,
            kcal = 0
        )
        return workoutSessionDao.insertSession(session)
    }

    override suspend fun getActiveWorkoutSession(): WorkoutSession? {
        return workoutSessionDao.getActiveSession()?.toDomainModel(emptyList())
    }

    override suspend fun completeSet(sessionId: Long, exerciseId: Long, setIndex: Int, repsDone: Int, restSecActual: Int) {
        val workoutSet = WorkoutSetEntity(
            sessionId = sessionId,
            exerciseId = exerciseId,
            setIndex = setIndex,
            repsPlanned = 0, // Will be filled from template
            repsDone = repsDone,
            restSecPlanned = 0, // Will be filled from template
            restSecActual = restSecActual
        )
        workoutSetDao.insertSet(workoutSet)
    }

    override suspend fun finishWorkout(sessionId: Long, kcal: Int) {
        val session = workoutSessionDao.getSessionById(sessionId)
        session?.let {
            val updatedSession = it.copy(
                endedAt = System.currentTimeMillis(),
                kcal = kcal
            )
            workoutSessionDao.updateSession(updatedSession)
        }
    }

    override fun getAllWorkoutSessions(): Flow<List<WorkoutSession>> {
        return workoutSessionDao.getAllSessions().map { sessions ->
            sessions.map { it.toDomainModel(emptyList()) }
        }
    }

    override suspend fun getWorkoutSessionById(id: Long): WorkoutSession? {
        return workoutSessionDao.getSessionById(id)?.toDomainModel(emptyList())
    }
}

// Extension functions for entity/domain model conversion
private fun WorkoutTemplateEntity.toDomainModel(exercises: List<TemplateExercise>): WorkoutTemplate {
    return WorkoutTemplate(
        id = id,
        name = name,
        createdAt = createdAt,
        exercises = exercises
    )
}

private fun WorkoutTemplate.toEntity(): WorkoutTemplateEntity {
    return WorkoutTemplateEntity(
        id = id,
        name = name,
        createdAt = createdAt
    )
}

private fun WorkoutSessionEntity.toDomainModel(completedSets: List<WorkoutSet>): WorkoutSession {
    return WorkoutSession(
        id = id,
        templateId = templateId,
        startedAt = startedAt,
        endedAt = endedAt,
        kcal = kcal,
        completedSets = completedSets
    )
}
