package se.umu.calu0217.strive.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.umu.calu0217.strive.domain.models.WorkoutTemplate
import se.umu.calu0217.strive.domain.usecases.CreateWorkoutTemplateUseCase
import se.umu.calu0217.strive.domain.usecases.GetWorkoutTemplatesUseCase
import se.umu.calu0217.strive.domain.repository.WorkoutRepository
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val getWorkoutTemplatesUseCase: GetWorkoutTemplatesUseCase,
    private val createWorkoutTemplateUseCase: CreateWorkoutTemplateUseCase,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        loadTemplates()
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
        // TODO: Navigate to template editor
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
    val showCreateDialog: Boolean = false
)
