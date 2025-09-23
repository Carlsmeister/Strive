package se.umu.calu0217.strive.core.di

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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

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

    @Provides
    fun provideExerciseDao(database: StriveDatabase): ExerciseDao {
        return database.exerciseDao()
    }

    @Provides
    fun provideWorkoutTemplateDao(database: StriveDatabase): WorkoutTemplateDao {
        return database.workoutTemplateDao()
    }

    @Provides
    fun provideTemplateExerciseDao(database: StriveDatabase): TemplateExerciseDao {
        return database.templateExerciseDao()
    }

    @Provides
    fun provideWorkoutSessionDao(database: StriveDatabase): WorkoutSessionDao {
        return database.workoutSessionDao()
    }

    @Provides
    fun provideWorkoutSetDao(database: StriveDatabase): WorkoutSetDao {
        return database.workoutSetDao()
    }

    @Provides
    fun provideRunSessionDao(database: StriveDatabase): RunSessionDao {
        return database.runSessionDao()
    }

    @Provides
    fun provideRunPointDao(database: StriveDatabase): RunPointDao {
        return database.runPointDao()
    }
}
