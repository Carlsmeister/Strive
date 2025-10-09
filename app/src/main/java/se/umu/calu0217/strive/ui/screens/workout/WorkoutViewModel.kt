package se.umu.calu0217.strive.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.umu.calu0217.strive.domain.models.Exercise
import se.umu.calu0217.strive.domain.models.TemplateExercise
import se.umu.calu0217.strive.domain.models.WorkoutTemplate
import se.umu.calu0217.strive.domain.usecases.CreateWorkoutTemplateUseCase
import se.umu.calu0217.strive.domain.usecases.GetWorkoutTemplatesUseCase
import se.umu.calu0217.strive.domain.repository.WorkoutRepository
import se.umu.calu0217.strive.domain.repository.ExerciseRepository
import javax.inject.Inject

/**
 * Manages workout templates and session initiation.
 * Handles creating, editing, and deleting workout templates, as well as starting workout sessions.
 * @param getWorkoutTemplatesUseCase Use case for retrieving workout templates.
 * @param createWorkoutTemplateUseCase Use case for creating new workout templates.
 * @param workoutRepository Repository for workout data operations.
 * @param exerciseRepository Repository for exercise data operations.
 * @author Carl Lundholm
 */
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val getWorkoutTemplatesUseCase: GetWorkoutTemplatesUseCase,
    private val createWorkoutTemplateUseCase: CreateWorkoutTemplateUseCase,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        loadTemplates()
        // Load available exercises for the add-exercise dialog
        viewModelScope.launch {
            try {
                exerciseRepository.getAllExercises().collect { list ->
                    _uiState.value = _uiState.value.copy(availableExercises = list)
                }
            } catch (_: Exception) {
                // Ignore; UI will handle empty state
            }
        }
    }

    /**
     * Loads all workout templates from the repository.
     * @author Carl Lundholm
     */
    private fun loadTemplates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                getWorkoutTemplatesUseCase().collect { templates ->
                    _uiState.value = _uiState.value.copy(
                        templates = templates,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /**
     * Starts a quick workout session without a predefined template.
     * @param onNavigateToActiveWorkout Callback with the created session ID for navigation.
     * @author Carl Lundholm
     */
    fun startQuickWorkout(onNavigateToActiveWorkout: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                // Use the existing startWorkout method with templateId 0 for quick workout
                val sessionId = workoutRepository.startWorkout(0L)
                onNavigateToActiveWorkout(sessionId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to start workout"
                )
            }
        }
    }

    /**
     * Starts a workout session from an existing template.
     * @param templateId ID of the template to use for the workout.
     * @param onNavigateToActiveWorkout Callback with the created session ID for navigation.
     * @author Carl Lundholm
     */
    fun startWorkoutFromTemplate(templateId: Long, onNavigateToActiveWorkout: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val sessionId = workoutRepository.startWorkout(templateId)
                onNavigateToActiveWorkout(sessionId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to start workout"
                )
            }
        }
    }

    /**
     * Shows the dialog for creating a new workout template.
     * @author Carl Lundholm
     */
    fun showCreateTemplateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    /**
     * Hides the template creation dialog.
     * @author Carl Lundholm
     */
    fun hideCreateTemplateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    /**
     * Creates a new workout template with the specified name.
     * @param templateName Name for the new workout template.
     * @author Carl Lundholm
     */
    fun createTemplate(templateName: String) {
        viewModelScope.launch {
            try {
                val template = WorkoutTemplate(
                    name = templateName,
                    createdAt = System.currentTimeMillis(),
                    exercises = emptyList()
                )
                createWorkoutTemplateUseCase(template)
                _uiState.value = _uiState.value.copy(showCreateDialog = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to create template"
                )
            }
        }
    }

    /**
     * Opens the editor for modifying an existing template.
     * @param template The template to edit.
     * @author Carl Lundholm
     */
    fun editTemplate(template: WorkoutTemplate) {
        // Open full editor for this template (load with exercises)
        viewModelScope.launch {
            try {
                val full = workoutRepository.getTemplateById(template.id) ?: template
                _uiState.value = _uiState.value.copy(
                    showEditorDialog = true,
                    editorTemplate = full
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to open editor"
                )
            }
        }
    }

    /**
     * Opens the dialog to add exercises to a template.
     * @param template The template to add exercises to.
     * @author Carl Lundholm
     */
    fun openAddExercisesDialog(template: WorkoutTemplate) {
        _uiState.value = _uiState.value.copy(
            editingTemplate = template,
            showEditDialog = true
        )
    }

    /**
     * Hides the edit template dialog.
     * @author Carl Lundholm
     */
    fun hideEditTemplateDialog() {
        _uiState.value = _uiState.value.copy(
            showEditDialog = false,
            editingTemplate = null
        )
    }

    /**
     * Hides the full template editor.
     * @author Carl Lundholm
     */
    fun hideTemplateEditor() {
        _uiState.value = _uiState.value.copy(
            showEditorDialog = false,
            editorTemplate = null
        )
    }

    /**
     * Saves changes to an edited template.
     * @param updated The updated template to save.
     * @author Carl Lundholm
     */
    fun saveEditedTemplate(updated: WorkoutTemplate) {
        viewModelScope.launch {
            try {
                workoutRepository.updateTemplate(updated)
                _uiState.value = _uiState.value.copy(
                    showEditorDialog = false,
                    editorTemplate = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to save template"
                )
            }
        }
    }

    /**
     * Adds an exercise to a template.
     * @param exerciseId ID of the exercise to add.
     * @param sets Number of sets for the exercise.
     * @param reps Number of repetitions per set.
     * @param restSec Rest time between sets in seconds.
     * @author Carl Lundholm
     */
    fun addExerciseToTemplate(exerciseId: Long, sets: Int, reps: Int, restSec: Int) {
        viewModelScope.launch {
            try {
                val template = _uiState.value.editingTemplate ?: return@launch
                // Fetch full template with exercises to compute position
                val full = workoutRepository.getTemplateById(template.id) ?: template
                val nextPosition = (full.exercises.maxOfOrNull { it.position } ?: -1) + 1
                val te = TemplateExercise(
                    exerciseId = exerciseId,
                    sets = sets,
                    reps = reps,
                    restSec = restSec,
                    position = nextPosition
                )
                workoutRepository.addExerciseToTemplate(template.id, te)
                // Close dialog after adding one exercise
                _uiState.value = _uiState.value.copy(showEditDialog = false, editingTemplate = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add exercise"
                )
            }
        }
    }

    /**
     * Deletes a workout template.
     * @param template The template to delete.
     * @author Carl Lundholm
     */
    fun deleteTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteTemplate(template)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete template"
                )
            }
        }
    }
}

/**
 * UI state for the workout screen.
 * @property templates List of all workout templates.
 * @property isLoading Indicates if templates are being loaded.
 * @property error Error message to display, if any.
 * @property showCreateDialog Whether to show the create template dialog.
 * @property availableExercises List of all available exercises.
 * @property showEditDialog Whether to show the add exercise dialog.
 * @property editingTemplate The template currently being edited for adding exercises.
 * @property showEditorDialog Whether to show the full template editor.
 * @property editorTemplate The template currently being edited in full editor.
 * @author Carl Lundholm
 */
data class WorkoutUiState(
    val templates: List<WorkoutTemplate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val availableExercises: List<Exercise> = emptyList(),
    // Add-exercise dialog state (for adding a new exercise to a template)
    val showEditDialog: Boolean = false,
    val editingTemplate: WorkoutTemplate? = null,
    // Full template editor state
    val showEditorDialog: Boolean = false,
    val editorTemplate: WorkoutTemplate? = null
)
