package se.umu.calu0217.strive.core.dependencyInjection

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.umu.calu0217.strive.data.local.StriveDatabase
import se.umu.calu0217.strive.data.local.dao.*
import javax.inject.Singleton

/**
 * Hilt Dependency Injection module for database and DAO provisioning.
 * Provides singleton instances of the Room database and all DAOs.
 * @author Carl Lundholm
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the singleton Room database instance.
     * Uses fallbackToDestructiveMigration to recreate database on schema changes.
     * @param context Application context for database creation.
     * @return StriveDatabase singleton instance.
     * @author Carl Lundholm
     */
    @Provides
    @Singleton
    fun provideStriveDatabase(@ApplicationContext context: Context): StriveDatabase {
        return Room.databaseBuilder(
            context,
            StriveDatabase::class.java,
            StriveDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides ExerciseDao for exercise data access.
     * @param database The StriveDatabase instance.
     * @return ExerciseDao for injection.
     * @author Carl Lundholm
     */
    @Provides
    fun provideExerciseDao(database: StriveDatabase): ExerciseDao {
        return database.exerciseDao()
    }

    /**
     * Provides WorkoutTemplateDao for template data access.
     * @param database The StriveDatabase instance.
     * @return WorkoutTemplateDao for injection.
     * @author Carl Lundholm
     */
    @Provides
    fun provideWorkoutTemplateDao(database: StriveDatabase): WorkoutTemplateDao {
        return database.workoutTemplateDao()
    }

    /**
     * Provides TemplateExerciseDao for template exercise data access.
     * @param database The StriveDatabase instance.
     * @return TemplateExerciseDao for injection.
     * @author Carl Lundholm
     */
    @Provides
    fun provideTemplateExerciseDao(database: StriveDatabase): TemplateExerciseDao {
        return database.templateExerciseDao()
    }

    /**
     * Provides WorkoutSessionDao for workout session data access.
     * @param database The StriveDatabase instance.
     * @return WorkoutSessionDao for injection.
     * @author Carl Lundholm
     */
    @Provides
    fun provideWorkoutSessionDao(database: StriveDatabase): WorkoutSessionDao {
        return database.workoutSessionDao()
    }

    /**
     * Provides WorkoutSetDao for workout set data access.
     * @param database The StriveDatabase instance.
     * @return WorkoutSetDao for injection.
     * @author Carl Lundholm
     */
    @Provides
    fun provideWorkoutSetDao(database: StriveDatabase): WorkoutSetDao {
        return database.workoutSetDao()
    }

    /**
     * Provides RunSessionDao for run session data access.
     * @param database The StriveDatabase instance.
     * @return RunSessionDao for injection.
     * @author Carl Lundholm
     */
    @Provides
    fun provideRunSessionDao(database: StriveDatabase): RunSessionDao {
        return database.runSessionDao()
    }

    /**
     * Provides RunPointDao for GPS point data access.
     * @param database The StriveDatabase instance.
     * @return RunPointDao for injection.
     * @author Carl Lundholm
     */
    @Provides
    fun provideRunPointDao(database: StriveDatabase): RunPointDao {
        return database.runPointDao()
    }
}
