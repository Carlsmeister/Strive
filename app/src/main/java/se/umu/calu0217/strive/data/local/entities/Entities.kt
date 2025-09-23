package se.umu.calu0217.strive.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

@Entity(tableName = "workout_templates")
data class WorkoutTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long
)

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

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: Long,
    val startedAt: Long,
    val endedAt: Long?,
    val kcal: Int
)

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

@Entity(tableName = "run_points")
data class RunPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val runId: Long,
    val idx: Int,
    val lat: Double,
    val lng: Double,
    val timestamp: Long
)

// Type converter for List<String> fields
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
