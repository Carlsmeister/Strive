package se.umu.calu0217.strive.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import se.umu.calu0217.strive.domain.models.*
import se.umu.calu0217.strive.domain.repository.WorkoutRepository
import se.umu.calu0217.strive.domain.repository.ExerciseRepository
import javax.inject.Inject

data class ActiveWorkoutUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val session: WorkoutSession? = null,
    val template: WorkoutTemplate? = null,
    val exercises: List<Exercise> = emptyList(),
    val availableExercises: List<Exercise> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val currentSetIndex: Int = 0,
    val completedSets: Map<String, Boolean> = emptyMap(), // exerciseId_setIndex -> completed
    val isRestMode: Boolean = false,
    val restTimeRemaining: Int = 0,
    val isPaused: Boolean = false
)

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: 0L

    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Load all available exercises for selection UI
        viewModelScope.launch {
            exerciseRepository.getAllExercises().collect { list ->
                _uiState.update { it.copy(availableExercises = list) }
            }
        }
        loadWorkoutSession()
    }

    private fun loadWorkoutSession() {
        viewModelScope.launch {
            try {
                val session = workoutRepository.getWorkoutSessionById(sessionId)
                if (session == null) {
                    _uiState.update { it.copy(error = "Workout session not found", isLoading = false) }
                    return@launch
                }

                val template = if (session.templateId == 0L) {
                    // Quick workout session with no predefined template
                    WorkoutTemplate(
                        id = 0L,
                        name = "Quick Workout",
                        createdAt = session.startedAt,
                        exercises = emptyList()
                    )
                } else {
                    val t = workoutRepository.getTemplateById(session.templateId)
                    if (t == null) {
                        _uiState.update { it.copy(error = "Workout template not found", isLoading = false) }
                        return@launch
                    }
                    t
                }

                // Load exercise details
                val exercises = template.exercises.mapNotNull { templateExercise ->
                    exerciseRepository.getExerciseById(templateExercise.exerciseId)
                }

                val completedSets = session.completedSets.associate { set ->
                    "${set.exerciseId}_${set.setIndex}" to true
                }

                _uiState.update {
                    it.copy(
                        session = session,
                        template = template,
                        exercises = exercises,
                        completedSets = completedSets,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to load workout: ${e.message}", isLoading = false)
                }
            }
        }
    }

    fun completeSet(exerciseId: Long, setIndex: Int, repsDone: Int) {
        viewModelScope.launch {
            try {
                val template = _uiState.value.template ?: return@launch
                val templateExercise = template.exercises.find { it.exerciseId == exerciseId }
                    ?: return@launch

                workoutRepository.completeSet(
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    setIndex = setIndex,
                    repsDone = repsDone,
                    restSecActual = templateExercise.restSec // Will be updated when rest is complete
                )

                // Update UI state
                val setKey = "${exerciseId}_${setIndex}"
                _uiState.update { state ->
                    state.copy(
                        completedSets = state.completedSets + (setKey to true),
                        isRestMode = setIndex < templateExercise.sets - 1, // Rest if not the last set
                        restTimeRemaining = if (setIndex < templateExercise.sets - 1) templateExercise.restSec else 0
                    )
                }

                // Start rest timer if needed
                if (setIndex < templateExercise.sets - 1) {
                    startRestTimer(templateExercise.restSec)
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to complete set: ${e.message}") }
            }
        }
    }

    private fun startRestTimer(restSeconds: Int) {
        viewModelScope.launch {
            for (i in restSeconds downTo 1) {
                _uiState.update { it.copy(restTimeRemaining = i) }
                kotlinx.coroutines.delay(1000)
            }
            _uiState.update { it.copy(isRestMode = false, restTimeRemaining = 0) }
        }
    }

    fun skipRest() {
        _uiState.update { it.copy(isRestMode = false, restTimeRemaining = 0) }
    }

    fun pauseWorkout() {
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resumeWorkout() {
        _uiState.update { it.copy(isPaused = false) }
    }

    fun addExercise(exerciseId: Long, sets: Int, reps: Int, restSec: Int) {
        viewModelScope.launch {
            val current = _uiState.value
            val template = current.template ?: return@launch
            val newTemplateExercise = TemplateExercise(
                exerciseId = exerciseId,
                sets = sets,
                reps = reps,
                restSec = restSec,
                position = template.exercises.size
            )
            val newTemplate = template.copy(exercises = template.exercises + newTemplateExercise)
            val exercise = exerciseRepository.getExerciseById(exerciseId)
            _uiState.update {
                it.copy(
                    template = newTemplate,
                    exercises = if (exercise != null) it.exercises + exercise else it.exercises
                )
            }
        }
    }

    fun finishWorkout(kcalBurned: Int, onWorkoutFinished: () -> Unit) {
        viewModelScope.launch {
            try {
                workoutRepository.finishWorkout(sessionId, kcalBurned)
                onWorkoutFinished()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to finish workout: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
