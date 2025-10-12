package se.umu.calu0217.strive.domain.models

/**
 * Domain model representing an exercise.
 * @property id Unique identifier for the exercise.
 * @property name Name of the exercise.
 * @property bodyParts List of body parts targeted by this exercise.
 * @property equipment Equipment required for the exercise.
 * @property instructions Step-by-step instructions for performing the exercise.
 * @property imageUrl URL to the exercise demonstration image.
 * @property usesWeight Whether this exercise uses external weight (false for bodyweight exercises).
 * @author Carl Lundholm
 */
data class Exercise(
    val id: Long = 0,
    val name: String,
    val bodyParts: List<String>,
    val equipment: String,
    val instructions: List<String>,
    val imageUrl: String?,
    val usesWeight: Boolean = true
)

/**
 * Domain model representing a workout template.
 * @property id Unique identifier for the template.
 * @property name Name of the workout template.
 * @property createdAt Timestamp when the template was created (milliseconds).
 * @property exercises List of exercises included in this template.
 * @author Carl Lundholm
 */
data class WorkoutTemplate(
    val id: Long = 0,
    val name: String,
    val createdAt: Long,
    val exercises: List<TemplateExercise> = emptyList()
)

/**
 * Domain model representing an exercise configuration within a template.
 * @property exerciseId ID of the exercise.
 * @property sets Number of sets to perform.
 * @property reps Number of repetitions per set.
 * @property restSec Rest time between sets in seconds.
 * @property position Order position within the template.
 * @author Carl Lundholm
 */
data class TemplateExercise(
    val exerciseId: Long,
    val sets: Int,
    val reps: Int,
    val restSec: Int,
    val position: Int
)

/**
 * Domain model representing a workout session.
 * @property id Unique identifier for the session.
 * @property templateId ID of the template used (0 for quick workout).
 * @property startedAt Timestamp when the workout started (milliseconds).
 * @property endedAt Timestamp when the workout ended (milliseconds), null if active.
 * @property kcal Calories burned during the workout.
 * @property completedSets List of completed sets in this session.
 * @author Carl Lundholm
 */
data class WorkoutSession(
    val id: Long = 0,
    val templateId: Long,
    val startedAt: Long,
    val endedAt: Long?,
    val kcal: Int = 0,
    val completedSets: List<WorkoutSet> = emptyList()
)

/**
 * Domain model representing a completed set in a workout.
 * @property sessionId ID of the parent workout session.
 * @property exerciseId ID of the exercise performed.
 * @property setIndex Index of this set within the exercise (0-based).
 * @property repsPlanned Number of reps planned from template.
 * @property repsDone Actual number of reps completed.
 * @property restSecPlanned Planned rest time from template (seconds).
 * @property restSecActual Actual rest time taken (seconds).
 * @property weightKg Weight used in kilograms (null for bodyweight exercises).
 * @author Carl Lundholm
 */
data class WorkoutSet(
    val sessionId: Long,
    val exerciseId: Long,
    val setIndex: Int,
    val repsPlanned: Int,
    val repsDone: Int,
    val restSecPlanned: Int,
    val restSecActual: Int,
    val weightKg: Double? = null
)

/**
 * Domain model representing a run/cycling/walking session.
 * @property id Unique identifier for the session.
 * @property startedAt Timestamp when the activity started (milliseconds).
 * @property endedAt Timestamp when the activity ended (milliseconds), null if active.
 * @property distance Total distance covered in meters.
 * @property elapsedSec Total time elapsed in seconds.
 * @property kcal Calories burned during the activity.
 * @property pace Average pace in minutes per kilometer.
 * @property points List of GPS location points recorded during the run.
 * @author Carl Lundholm
 */
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

/**
 * Domain model representing a GPS location point during a run.
 * @property runId ID of the parent run session.
 * @property lat GPS latitude coordinate.
 * @property lng GPS longitude coordinate.
 * @property timestamp When this point was recorded (milliseconds).
 * @author Carl Lundholm
 */
data class RunPoint(
    val runId: Long,
    val lat: Double,
    val lng: Double,
    val timestamp: Long
)
