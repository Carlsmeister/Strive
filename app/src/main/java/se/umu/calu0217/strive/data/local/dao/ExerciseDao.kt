package se.umu.calu0217.strive.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import se.umu.calu0217.strive.data.local.entities.*

/**
 * Data Access Object for exercise-related database operations.
 * Provides methods to query, insert, update, and delete exercises from the database.
 */
@Dao
interface ExerciseDao {
    /**
     * Retrieves all exercises from the database, sorted alphabetically by name (case-insensitive).
     *
     * @return A Flow emitting a list of all exercises, updated automatically when data changes.
     */
    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    /**
     * Retrieves a specific exercise by its unique identifier.
     *
     * @param id The unique identifier of the exercise.
     * @return The exercise entity if found, null otherwise.
     */
    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): ExerciseEntity?

    /**
     * Searches for exercises matching the given query string.
     * The search is performed across exercise name, equipment, and body parts fields.
     * Results are sorted alphabetically by name (case-insensitive).
     *
     * @param query The search query string to match against exercise properties.
     * @return A Flow emitting a list of matching exercises.
     */
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' " +
            "OR equipment LIKE '%' || :query || '%' " +
            "OR bodyParts LIKE '%' || :query || '%' " +
            "ORDER BY name COLLATE NOCASE ASC")
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>

    /**
     * Counts the total number of exercises in the database.
     *
     * @return The total count of exercises.
     */
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun countExercises(): Int

    /**
     * Retrieves all exercises that don't have an associated image URL.
     *
     * @return A list of exercises with null or empty image URLs.
     */
    @Query("SELECT * FROM exercises WHERE imageUrl IS NULL OR imageUrl = ''")
    suspend fun getExercisesMissingImages(): List<ExerciseEntity>

    /**
     * Inserts a list of exercises into the database.
     * If an exercise with the same primary key already exists, it will be replaced.
     *
     * @param exercises The list of exercise entities to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    /**
     * Updates an existing exercise in the database.
     *
     * @param exercise The exercise entity with updated values.
     */
    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    /**
     * Deletes all exercises from the database.
     */
    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()

}

/**
 * Data Access Object for workout template-related database operations.
 * Manages workout templates that users can create and reuse for their workouts.
 */
@Dao
interface WorkoutTemplateDao {
    /**
     * Retrieves all workout templates, ordered by creation date (most recent first).
     *
     * @return A Flow emitting a list of all workout templates.
     */
    @Query("SELECT * FROM workout_templates ORDER BY createdAt DESC")
    fun getAllTemplates(): Flow<List<WorkoutTemplateEntity>>

    /**
     * Retrieves a specific workout template by its unique identifier.
     *
     * @param id The unique identifier of the workout template.
     * @return The workout template entity if found, null otherwise.
     */
    @Query("SELECT * FROM workout_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): WorkoutTemplateEntity?

    /**
     * Inserts a new workout template into the database.
     *
     * @param template The workout template entity to insert.
     * @return The row ID of the newly inserted template.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity): Long

    /**
     * Updates an existing workout template in the database.
     *
     * @param template The workout template entity with updated values.
     */
    @Update
    suspend fun updateTemplate(template: WorkoutTemplateEntity)

    /**
     * Deletes a workout template from the database.
     *
     * @param template The workout template entity to delete.
     */
    @Delete
    suspend fun deleteTemplate(template: WorkoutTemplateEntity)

    /**
     * Deletes all workout templates from the database.
     */
    @Query("DELETE FROM workout_templates")
    suspend fun deleteAllTemplates()
}

/**
 * Data Access Object for template exercise-related database operations.
 * Manages the exercises that are part of workout templates, including their order and configuration.
 */
@Dao
interface TemplateExerciseDao {
    /**
     * Retrieves all exercises for a specific workout template, ordered by position.
     *
     * @param templateId The unique identifier of the workout template.
     * @return A Flow emitting a list of template exercises in the correct order.
     */
    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY position")
    fun getTemplateExercises(templateId: Long): Flow<List<TemplateExerciseEntity>>

    /**
     * Inserts a single template exercise into the database.
     *
     * @param templateExercise The template exercise entity to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercise(templateExercise: TemplateExerciseEntity)

    /**
     * Inserts multiple template exercises into the database.
     *
     * @param templateExercises The list of template exercise entities to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercises(templateExercises: List<TemplateExerciseEntity>)

    /**
     * Deletes all exercises associated with a specific workout template.
     *
     * @param templateId The unique identifier of the workout template.
     */
    @Query("DELETE FROM template_exercises WHERE templateId = :templateId")
    suspend fun deleteTemplateExercises(templateId: Long)

    /**
     * Deletes all template exercises from the database.
     */
    @Query("DELETE FROM template_exercises")
    suspend fun deleteAllTemplateExercises()
}
