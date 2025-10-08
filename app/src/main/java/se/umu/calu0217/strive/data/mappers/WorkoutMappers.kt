package se.umu.calu0217.strive.data.mappers

import se.umu.calu0217.strive.data.local.entities.TemplateExerciseEntity
import se.umu.calu0217.strive.data.local.entities.WorkoutSessionEntity
import se.umu.calu0217.strive.data.local.entities.WorkoutTemplateEntity
import se.umu.calu0217.strive.domain.models.TemplateExercise
import se.umu.calu0217.strive.domain.models.WorkoutSession
import se.umu.calu0217.strive.domain.models.WorkoutSet
import se.umu.calu0217.strive.domain.models.WorkoutTemplate

/**
 * Converts a WorkoutTemplateEntity from the database to a domain model.
 * @param exercises List of template exercises to include.
 * @return WorkoutTemplate domain model.
 * @author Carl Lundholm
 */
fun WorkoutTemplateEntity.toDomainModel(exercises: List<TemplateExercise>): WorkoutTemplate =
    WorkoutTemplate(
        id = id,
        name = name,
        createdAt = createdAt,
        exercises = exercises
    )

/**
 * Converts a WorkoutTemplate domain model to a database entity.
 * Note: This does not include exercise mappings.
 * @return WorkoutTemplateEntity for database storage.
 * @author Carl Lundholm
 */
fun WorkoutTemplate.toEntity(): WorkoutTemplateEntity =
    WorkoutTemplateEntity(
        id = id,
        name = name,
        createdAt = createdAt
    )

/**
 * Converts a WorkoutSessionEntity from the database to a domain model.
 * @param completedSets List of completed sets to include.
 * @return WorkoutSession domain model.
 * @author Carl Lundholm
 */
fun WorkoutSessionEntity.toDomainModel(completedSets: List<WorkoutSet>): WorkoutSession =
    WorkoutSession(
        id = id,
        templateId = templateId,
        startedAt = startedAt,
        endedAt = endedAt,
        kcal = kcal,
        completedSets = completedSets
    )

/**
 * Converts a TemplateExerciseEntity from the database to a domain model.
 * @return TemplateExercise domain model.
 * @author Carl Lundholm
 */
fun TemplateExerciseEntity.toDomainModel(): TemplateExercise =
    TemplateExercise(
        exerciseId = exerciseId,
        sets = sets,
        reps = reps,
        restSec = restSec,
        position = position
    )

/**
 * Converts a TemplateExercise domain model to a database entity.
 * @param templateId ID of the parent template.
 * @return TemplateExerciseEntity for database storage.
 * @author Carl Lundholm
 */
fun TemplateExercise.toEntity(templateId: Long): TemplateExerciseEntity =
    TemplateExerciseEntity(
        templateId = templateId,
        exerciseId = exerciseId,
        sets = sets,
        reps = reps,
        restSec = restSec,
        position = position
    )
