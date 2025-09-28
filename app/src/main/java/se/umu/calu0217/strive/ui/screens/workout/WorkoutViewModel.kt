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

    fun showCreateTemplateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun hideCreateTemplateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

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

    fun openAddExercisesDialog(template: WorkoutTemplate) {
        _uiState.value = _uiState.value.copy(
            editingTemplate = template,
            showEditDialog = true
        )
    }

    fun hideEditTemplateDialog() {
        _uiState.value = _uiState.value.copy(
            showEditDialog = false,
            editingTemplate = null
        )
    }

    fun hideTemplateEditor() {
        _uiState.value = _uiState.value.copy(
            showEditorDialog = false,
            editorTemplate = null
        )
    }

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
