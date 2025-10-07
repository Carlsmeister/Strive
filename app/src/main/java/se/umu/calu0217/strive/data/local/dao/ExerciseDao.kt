package se.umu.calu0217.strive.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import se.umu.calu0217.strive.data.local.entities.*

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' " +
            "OR equipment LIKE '%' || :query || '%' " +
            "OR bodyParts LIKE '%' || :query || '%' " +
            "ORDER BY name COLLATE NOCASE ASC")
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>


    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun countExercises(): Int


    @Query("SELECT * FROM exercises WHERE imageUrl IS NULL OR imageUrl = ''")
    suspend fun getExercisesMissingImages(): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)


    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()

}

@Dao
interface WorkoutTemplateDao {
    @Query("SELECT * FROM workout_templates ORDER BY createdAt DESC")
    fun getAllTemplates(): Flow<List<WorkoutTemplateEntity>>

    @Query("SELECT * FROM workout_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): WorkoutTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity): Long

    @Update
    suspend fun updateTemplate(template: WorkoutTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: WorkoutTemplateEntity)
}

@Dao
interface TemplateExerciseDao {
    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY position")
    fun getTemplateExercises(templateId: Long): Flow<List<TemplateExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercise(templateExercise: TemplateExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercises(templateExercises: List<TemplateExerciseEntity>)


    @Query("DELETE FROM template_exercises WHERE templateId = :templateId")
    suspend fun deleteTemplateExercises(templateId: Long)
}
