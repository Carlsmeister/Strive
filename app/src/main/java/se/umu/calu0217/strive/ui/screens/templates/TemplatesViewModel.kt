package se.umu.calu0217.strive.ui.screens.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import se.umu.calu0217.strive.domain.models.WorkoutTemplate
import se.umu.calu0217.strive.domain.models.TemplateExercise
import se.umu.calu0217.strive.domain.usecases.CreateWorkoutTemplateUseCase
import se.umu.calu0217.strive.domain.usecases.GetWorkoutTemplatesUseCase
import se.umu.calu0217.strive.domain.usecases.StartWorkoutUseCase
import javax.inject.Inject

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val getWorkoutTemplatesUseCase: GetWorkoutTemplatesUseCase,
    private val createWorkoutTemplateUseCase: CreateWorkoutTemplateUseCase,
    private val startWorkoutUseCase: StartWorkoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplatesUiState())
    val uiState: StateFlow<TemplatesUiState> = _uiState.asStateFlow()

    val templates = getWorkoutTemplatesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createTemplate(name: String) {
        viewModelScope.launch {
            try {
                val template = WorkoutTemplate(
                    name = name,
                    createdAt = System.currentTimeMillis(),
                    exercises = emptyList()
                )
                createWorkoutTemplateUseCase(template)
                _uiState.value = _uiState.value.copy(showCreateDialog = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error creating template"
                )
            }
        }
    }

    fun startWorkout(templateId: Long, onNavigateToWorkout: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val sessionId = startWorkoutUseCase(templateId)
                onNavigateToWorkout(sessionId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error starting workout"
                )
            }
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class TemplatesUiState(
    val showCreateDialog: Boolean = false,
    val error: String? = null
)
