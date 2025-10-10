package se.umu.calu0217.strive.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import se.umu.calu0217.strive.domain.models.*
import se.umu.calu0217.strive.domain.repository.WorkoutRepository
import se.umu.calu0217.strive.domain.repository.ExerciseRepository
import se.umu.calu0217.strive.core.utils.FitnessUtils
import javax.inject.Inject

/**
 * UI state for active workout session.
 * @property isLoading Indicates if workout data is being loaded.
 * @property error Error message to display, if any.
 * @property session The current workout session data.
 * @property template The workout template being followed.
 * @property exercises List of exercises in the workout.
 * @property availableExercises All available exercises for adding to workout.
 * @property currentExerciseIndex Index of the currently active exercise.
 * @property currentSetIndex Index of the currently active set.
 * @property completedSets Map tracking completed sets (exerciseId_setIndex -> completed).
 * @property isRestMode Whether the user is currently in rest mode.
 * @property restTimeRemaining Seconds remaining in the rest timer.
 * @property isPaused Whether the workout is paused.
 * @author Carl Lundholm
 */
data class ActiveWorkoutUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val session: WorkoutSession? = null,
    val template: WorkoutTemplate? = null,
    val exercises: List<Exercise> = emptyList(),
    val availableExercises: List<Exercise> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val currentSetIndex: Int = 0,
    val completedSets: Map<String, Boolean> = emptyMap(),
    val isRestMode: Boolean = false,
    val restTimeRemaining: Int = 0,
    val isPaused: Boolean = false
)

/**
 * Handles state for active training/workout sessions.
 * Manages exercise progression, set completion, rest timers, and workout finalization.
 * @param workoutRepository Repository for workout data operations.
 * @param exerciseRepository Repository for exercise data operations.
 * @param savedStateHandle Handles navigation arguments including sessionId.
 * @author Carl Lundholm
 */
@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: 0L

    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState = _uiState.asStateFlow()

    private var restTimerJob: Job? = null

    init {
        viewModelScope.launch {
            exerciseRepository.getAllExercises().collect { list ->
                _uiState.update { it.copy(availableExercises = list) }
            }
        }
        loadWorkoutSession()
    }

    /**
     * Loads the workout session and its associated template and exercises from the database.
     * Updates UI state with loaded data or error messages.
     * @author Carl Lundholm
     */
    private fun loadWorkoutSession() {
        viewModelScope.launch {
            try {
                val session = workoutRepository.getWorkoutSessionById(sessionId)
                if (session == null) {
                    _uiState.update { it.copy(error = "Workout session not found", isLoading = false) }
                    return@launch
                }

                val template = if (session.templateId == 0L) {
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

    /**
     * Marks a set as completed and starts rest timer if needed.
     * @param exerciseId ID of the exercise being performed.
     * @param setIndex Index of the set being completed (0-based).
     * @param repsDone Number of repetitions completed in this set.
     * @author Carl Lundholm
     */
    fun completeSet(exerciseId: Long, setIndex: Int, repsDone: Int) {
        viewModelScope.launch {
            try {
                if (_uiState.value.isRestMode) {
                    return@launch
                }

                val template = _uiState.value.template ?: return@launch
                val templateExercise = template.exercises.find { it.exerciseId == exerciseId }
                    ?: return@launch

                workoutRepository.completeSet(
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    setIndex = setIndex,
                    repsDone = repsDone,
                    restSecActual = templateExercise.restSec
                )

                val setKey = "${exerciseId}_${setIndex}"
                _uiState.update { state ->
                    state.copy(
                        completedSets = state.completedSets + (setKey to true),
                        isRestMode = setIndex < templateExercise.sets - 1,
                        restTimeRemaining = if (setIndex < templateExercise.sets - 1) templateExercise.restSec else 0
                    )
                }

                if (setIndex < templateExercise.sets - 1) {
                    startRestTimer(templateExercise.restSec)
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to complete set: ${e.message}") }
            }
        }
    }

    /**
     * Starts a countdown rest timer.
     * @param restSeconds Duration of rest in seconds.
     * @author Carl Lundholm
     */
    private fun startRestTimer(restSeconds: Int) {
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            _uiState.update { it.copy(isRestMode = true, restTimeRemaining = restSeconds) }
            for (i in restSeconds downTo 1) {
                _uiState.update { it.copy(restTimeRemaining = i) }
                kotlinx.coroutines.delay(1000)
            }
            // Rest finished
            _uiState.update { it.copy(isRestMode = false, restTimeRemaining = 0) }
        }
    }

    /**
     * Skips the current rest period and allows immediate continuation.
     * @author Carl Lundholm
     */
    fun skipRest() {
        restTimerJob?.cancel()
        restTimerJob = null
        _uiState.update { it.copy(isRestMode = false, restTimeRemaining = 0) }
    }

    /**
     * Pauses the current workout session.
     * @author Carl Lundholm
     */
    fun pauseWorkout() {
        _uiState.update { it.copy(isPaused = true) }
    }

    /**
     * Resumes a paused workout session.
     * @author Carl Lundholm
     */
    fun resumeWorkout() {
        _uiState.update { it.copy(isPaused = false) }
    }

    /**
     * Adds a new exercise to the active workout session.
     * @param exerciseId ID of the exercise to add.
     * @param sets Number of sets for the exercise.
     * @param reps Number of reps per set.
     * @param restSec Rest time between sets in seconds.
     * @author Carl Lundholm
     */
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

    /**
     * Reorders exercises within the active workout session (local only, not persisted).
     * @param fromIndex Current index of the exercise to move.
     * @param toIndex Target index to move the exercise to.
     * @author Carl Lundholm
     */
    fun moveExercise(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val current = _uiState.value
            val template = current.template ?: return@launch
            val ordered = template.exercises.sortedBy { it.position }.toMutableList()
            if (fromIndex !in ordered.indices || toIndex !in ordered.indices || fromIndex == toIndex) return@launch
            val item = ordered.removeAt(fromIndex)
            ordered.add(toIndex, item)
            val reindexed = ordered.mapIndexed { idx, te -> te.copy(position = idx) }
            val exMap = current.exercises.associateBy { it.id }
            val newExercises = reindexed.mapNotNull { exMap[it.exerciseId] }
            _uiState.update {
                it.copy(
                    template = template.copy(exercises = reindexed),
                    exercises = newExercises
                )
            }
        }
    }

    /**
     * Moves an exercise up in the order (decreases index by 1).
     * @param index Current index of the exercise.
     * @author Carl Lundholm
     */
    fun moveExerciseUp(index: Int) {
        if (index > 0) moveExercise(index, index - 1)
    }

    /**
     * Moves an exercise down in the order (increases index by 1).
     * @param index Current index of the exercise.
     * @author Carl Lundholm
     */
    fun moveExerciseDown(index: Int) {
        val size = _uiState.value.template?.exercises?.size ?: return
        if (index < size - 1) moveExercise(index, index + 1)
    }

    /**
     * Finishes the workout with automatically calculated calories based on workout duration.
     * Uses MET value for moderate intensity strength training (6.0) and assumes 70kg body weight.
     * @param onWorkoutFinished Callback invoked when workout is successfully finished.
     * @author Carl Lundholm
     */
    fun finishWorkoutAuto(onWorkoutFinished: () -> Unit) {
        viewModelScope.launch {
            try {
                val session = _uiState.value.session
                val start = session?.startedAt ?: System.currentTimeMillis()
                val now = System.currentTimeMillis()
                val elapsedMs = (now - start).coerceAtLeast(0L)
                val timeHours = elapsedMs / 3_600_000.0
                val defaultWeightKg = 70.0
                val strengthTrainingMet = 6.0
                val kcal = FitnessUtils.calculateCalories(strengthTrainingMet, defaultWeightKg, timeHours)
                    .coerceAtLeast(if (elapsedMs > 0) 1 else 0)
                workoutRepository.finishWorkout(sessionId, kcal)
                onWorkoutFinished()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to finish workout: ${e.message}") }
            }
        }
    }
}
