package se.umu.calu0217.strive.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Database entity for exercises.
 * @property id Unique identifier for the exercise.
 * @property name Name of the exercise.
 * @property bodyParts List of body parts targeted by this exercise.
 * @property equipment Equipment needed for the exercise.
 * @property instructions Step-by-step instructions for performing the exercise.
 * @property imageUrl URL to the exercise demonstration image.
 * @author Carl Lundholm
 */
@Entity(tableName = "exercises")
@TypeConverters(StringListConverter::class)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val bodyParts: List<String>,
    val equipment: String,
    val instructions: List<String>,
    val imageUrl: String?
)

/**
 * Database entity for workout templates.
 * @property id Unique identifier for the template.
 * @property name Name of the workout template.
 * @property createdAt Timestamp when the template was created (milliseconds).
 * @author Carl Lundholm
 */
@Entity(tableName = "workout_templates")
data class WorkoutTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long
)

/**
 * Database entity for exercises within a template.
 * @property id Unique identifier for this template exercise.
 * @property templateId ID of the parent template.
 * @property exerciseId ID of the exercise.
 * @property sets Number of sets to perform.
 * @property reps Number of repetitions per set.
 * @property restSec Rest time between sets in seconds.
 * @property position Order position within the template.
 * @author Carl Lundholm
 */
@Entity(
    tableName = "template_exercises",
    indices = [Index(value = ["templateId"]), Index(value = ["exerciseId"])]
)
data class TemplateExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: Long,
    val exerciseId: Long,
    val sets: Int,
    val reps: Int,
    val restSec: Int,
    val position: Int
)

/**
 * Database entity for workout sessions.
 * @property id Unique identifier for the session.
 * @property templateId ID of the template used (0 for quick workout).
 * @property startedAt Timestamp when the workout started (milliseconds).
 * @property endedAt Timestamp when the workout ended (milliseconds), null if active.
 * @property kcal Calories burned during the workout.
 * @author Carl Lundholm
 */
@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: Long,
    val startedAt: Long,
    val endedAt: Long?,
    val kcal: Int
)

/**
 * Database entity for individual workout sets.
 * @property id Unique identifier for this set.
 * @property sessionId ID of the parent workout session.
 * @property exerciseId ID of the exercise performed.
 * @property setIndex Index of this set within the exercise (0-based).
 * @property repsPlanned Number of reps planned from template.
 * @property repsDone Actual number of reps completed.
 * @property restSecPlanned Planned rest time from template (seconds).
 * @property restSecActual Actual rest time taken (seconds).
 * @author Carl Lundholm
 */
@Entity(tableName = "workout_sets")
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val setIndex: Int,
    val repsPlanned: Int,
    val repsDone: Int,
    val restSecPlanned: Int,
    val restSecActual: Int
)

/**
 * Database entity for run/cycling/walking sessions.
 * @property id Unique identifier for the session.
 * @property startedAt Timestamp when the activity started (milliseconds).
 * @property endedAt Timestamp when the activity ended (milliseconds), null if active.
 * @property distance Total distance covered in meters.
 * @property elapsedSec Total time elapsed in seconds.
 * @property kcal Calories burned during the activity.
 * @property pace Average pace in minutes per kilometer.
 * @author Carl Lundholm
 */
@Entity(tableName = "run_sessions")
data class RunSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long?,
    val distance: Double,
    val elapsedSec: Int,
    val kcal: Int,
    val pace: Double
)

/**
 * Database entity for GPS location points during a run.
 * @property id Unique identifier for this point.
 * @property runId ID of the parent run session.
 * @property lat GPS latitude coordinate.
 * @property lng GPS longitude coordinate.
 * @property timestamp When this point was recorded (milliseconds).
 * @author Carl Lundholm
 */
@Entity(tableName = "run_points")
data class RunPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val runId: Long,
    val lat: Double,
    val lng: Double,
    val timestamp: Long
)

/**
 * Type converter for storing List<String> in Room database.
 * Converts between list and JSON string representation.
 * @author Carl Lundholm
 */
class StringListConverter {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return Json.decodeFromString<List<String>>(value)
    }
}
