package se.umu.calu0217.strive.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import se.umu.calu0217.strive.data.local.dao.*
import se.umu.calu0217.strive.data.local.entities.*

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutTemplateEntity::class,
        TemplateExerciseEntity::class,
        WorkoutSessionEntity::class,
        WorkoutSetEntity::class,
        RunSessionEntity::class,
        RunPointEntity::class
    ],
    version = 2, // Incremented version due to schema changes
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class StriveDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutTemplateDao(): WorkoutTemplateDao
    abstract fun templateExerciseDao(): TemplateExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun workoutSetDao(): WorkoutSetDao
    abstract fun runSessionDao(): RunSessionDao
    abstract fun runPointDao(): RunPointDao

    companion object {
        const val DATABASE_NAME = "strive_database"
    }
}
