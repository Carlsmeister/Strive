package se.umu.calu0217.strive.data.mappers

import se.umu.calu0217.strive.data.local.entities.TemplateExerciseEntity
import se.umu.calu0217.strive.data.local.entities.WorkoutSessionEntity
import se.umu.calu0217.strive.data.local.entities.WorkoutTemplateEntity
import se.umu.calu0217.strive.domain.models.TemplateExercise
import se.umu.calu0217.strive.domain.models.WorkoutSession
import se.umu.calu0217.strive.domain.models.WorkoutSet
import se.umu.calu0217.strive.domain.models.WorkoutTemplate

// Centralized mappers for Workout-related models

fun WorkoutTemplateEntity.toDomainModel(exercises: List<TemplateExercise>): WorkoutTemplate =
    WorkoutTemplate(
        id = id,
        name = name,
        createdAt = createdAt,
        exercises = exercises
    )

fun WorkoutTemplate.toEntity(): WorkoutTemplateEntity =
    WorkoutTemplateEntity(
        id = id,
        name = name,
        createdAt = createdAt
    )

fun WorkoutSessionEntity.toDomainModel(completedSets: List<WorkoutSet>): WorkoutSession =
    WorkoutSession(
        id = id,
        templateId = templateId,
        startedAt = startedAt,
        endedAt = endedAt,
        kcal = kcal,
        completedSets = completedSets
    )

// TemplateExercise mappings to consolidate repeated conversions in repositories
fun TemplateExerciseEntity.toDomainModel(): TemplateExercise =
    TemplateExercise(
        exerciseId = exerciseId,
        sets = sets,
        reps = reps,
        restSec = restSec,
        position = position
    )

fun TemplateExercise.toEntity(templateId: Long): TemplateExerciseEntity =
    TemplateExerciseEntity(
        templateId = templateId,
        exerciseId = exerciseId,
        sets = sets,
        reps = reps,
        restSec = restSec,
        position = position
    )
