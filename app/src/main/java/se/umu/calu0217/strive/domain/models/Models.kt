package se.umu.calu0217.strive.domain.models

data class Exercise(
    val id: Long = 0,
    val name: String,
    val bodyParts: List<String>,
    val equipment: String,
    val instructions: List<String>,
    val imageUrl: String?
)

data class WorkoutTemplate(
    val id: Long = 0,
    val name: String,
    val createdAt: Long,
    val exercises: List<TemplateExercise> = emptyList()
)

data class TemplateExercise(
    val exerciseId: Long,
    val sets: Int,
    val reps: Int,
    val restSec: Int,
    val position: Int
)

data class WorkoutSession(
    val id: Long = 0,
    val templateId: Long,
    val startedAt: Long,
    val endedAt: Long?,
    val kcal: Int = 0,
    val completedSets: List<WorkoutSet> = emptyList()
)

data class WorkoutSet(
    val sessionId: Long,
    val exerciseId: Long,
    val setIndex: Int,
    val repsPlanned: Int,
    val repsDone: Int,
    val restSecPlanned: Int,
    val restSecActual: Int
)

data class RunSession(
    val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long?,
    val distance: Double = 0.0, // in meters
    val elapsedSec: Int = 0,
    val kcal: Int = 0,
    val pace: Double = 0.0, // min/km
    val points: List<RunPoint> = emptyList()
)

data class RunPoint(
    val runId: Long,
    val lat: Double,
    val lng: Double,
    val timestamp: Long
)
