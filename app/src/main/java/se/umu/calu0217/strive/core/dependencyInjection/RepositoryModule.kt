package se.umu.calu0217.strive.core.dependencyInjection

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

/**
 * Hilt Dependency Injection module for repository bindings.
 * Provides singleton implementations of repository interfaces throughout the app.
 * @author Carl Lundholm
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds ExerciseRepositoryImpl to ExerciseRepository interface.
     * @param exerciseRepositoryImpl The concrete implementation.
     * @return ExerciseRepository interface for injection.
     * @author Carl Lundholm
     */
    @Binds
    @Singleton
    abstract fun bindExerciseRepository(
        exerciseRepositoryImpl: ExerciseRepositoryImpl
    ): ExerciseRepository

    /**
     * Binds WorkoutRepositoryImpl to WorkoutRepository interface.
     * @param workoutRepositoryImpl The concrete implementation.
     * @return WorkoutRepository interface for injection.
     * @author Carl Lundholm
     */
    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        workoutRepositoryImpl: WorkoutRepositoryImpl
    ): WorkoutRepository

    /**
     * Binds RunRepositoryImpl to RunRepository interface.
     * @param runRepositoryImpl The concrete implementation.
     * @return RunRepository interface for injection.
     * @author Carl Lundholm
     */
    @Binds
    @Singleton
    abstract fun bindRunRepository(
        runRepositoryImpl: RunRepositoryImpl
    ): RunRepository
}
