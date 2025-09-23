package se.umu.calu0217.strive.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.umu.calu0217.strive.data.repository.ExerciseRepositoryImpl
import se.umu.calu0217.strive.data.repository.RunRepositoryImpl
import se.umu.calu0217.strive.data.repository.WorkoutRepositoryImpl
import se.umu.calu0217.strive.domain.repository.ExerciseRepository
import se.umu.calu0217.strive.domain.repository.RunRepository
import se.umu.calu0217.strive.domain.repository.WorkoutRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(
        exerciseRepositoryImpl: ExerciseRepositoryImpl
    ): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        workoutRepositoryImpl: WorkoutRepositoryImpl
    ): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindRunRepository(
        runRepositoryImpl: RunRepositoryImpl
    ): RunRepository
}
