package se.umu.calu0217.strive.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import se.umu.calu0217.strive.core.error.toAppError
import se.umu.calu0217.strive.domain.models.Exercise
import se.umu.calu0217.strive.domain.models.TemplateExercise
import se.umu.calu0217.strive.domain.usecases.BackfillMissingExerciseImagesUseCase
import se.umu.calu0217.strive.domain.usecases.ForceRefreshExercisesUseCase
import se.umu.calu0217.strive.domain.usecases.GetExercisesUseCase
import se.umu.calu0217.strive.domain.usecases.SearchExercisesUseCase
import se.umu.calu0217.strive.domain.usecases.SeedExercisesUseCase
import javax.inject.Inject

/**
 * Manages exercise exploration and discovery.
 * Provides search, filtering, and exercise database management functionality.
 * @param getExercisesUseCase Use case for retrieving all exercises.
 * @param searchExercisesUseCase Use case for searching exercises.
 * @param seedExercisesUseCase Use case for initializing exercise database.
 * @param backfillMissingExerciseImagesUseCase Use case for fetching missing exercise images.
 * @param forceRefreshExercisesUseCase Use case for refreshing exercise data from API.
 * @param workoutRepository Repository for workout template operations.
 * @author Carl Lundholm
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getExercisesUseCase: GetExercisesUseCase,
    private val searchExercisesUseCase: SearchExercisesUseCase,
    private val seedExercisesUseCase: SeedExercisesUseCase,
    private val backfillMissingExerciseImagesUseCase: BackfillMissingExerciseImagesUseCase,
    private val forceRefreshExercisesUseCase: ForceRefreshExercisesUseCase,
    private val workoutRepository: se.umu.calu0217.strive.domain.repository.WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilters = MutableStateFlow<Set<String>>(emptySet())
    val selectedFilters: StateFlow<Set<String>> = _selectedFilters.asStateFlow()

    private val _templates = MutableStateFlow<List<se.umu.calu0217.strive.domain.models.WorkoutTemplate>>(emptyList())
    val templates: StateFlow<List<se.umu.calu0217.strive.domain.models.WorkoutTemplate>> = _templates.asStateFlow()

    private val filterAliases: Map<String, List<String>> = mapOf(
        "legs" to listOf("quadriceps", "hamstrings", "glutes", "calves", "legs"),
        "arms" to listOf("biceps", "triceps", "forearms", "arms"),
        "core" to listOf("core", "abs", "abdominals", "obliques"),
        "back" to listOf("back", "lats", "latissimus", "trapezius", "traps", "lower back"),
        "shoulders" to listOf("shoulders", "delts", "deltoids"),
        "chest" to listOf("chest", "pectorals", "pecs")
    )

    val exercises = combine(
        searchQuery.debounce(300),
        selectedFilters
    ) { query, filters -> query to filters }
        .flatMapLatest { (query, filters) ->
            searchExercisesUseCase(query).map { list ->
                val filtered = if (filters.isEmpty()) list
                else list.filter { exercise -> matchesFilters(exercise, filters) }

                filtered
                    .map { it to it.name.trim().lowercase() }
                    .distinctBy { it.second }
                    .sortedBy { it.second }
                    .map { it.first }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        initializeData()
    }

    /**
     * Checks if an exercise matches the selected filters.
     * @param exercise The exercise to check.
     * @param filters Set of filter strings to match against.
     * @return True if the exercise matches any of the filters.
     * @author Carl Lundholm
     */
    private fun matchesFilters(exercise: Exercise, filters: Set<String>): Boolean {
        if (filters.isEmpty()) return true
        val parts = exercise.bodyParts.map { it.lowercase() }
        val allowedTokens = filters.flatMap { f ->
            val key = f.lowercase()
            filterAliases[key] ?: listOf(key)
        }.map { it.lowercase() }
        return parts.any { part -> allowedTokens.any { token -> part.contains(token) } }
    }

    /**
     * Initializes the exercise database by seeding and backfilling images.
     * @author Carl Lundholm
     */
    private fun initializeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                seedExercisesUseCase()
                backfillMissingExerciseImagesUseCase()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                val appError = e.toAppError()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = appError.message
                )
            }
        }
    }

    /**
     * Updates the search query for filtering exercises.
     * @param query The search query string.
     * @author Carl Lundholm
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Toggles a body part filter on/off.
     * @param filter The filter to toggle (e.g., "legs", "arms", "chest").
     * @author Carl Lundholm
     */
    fun toggleFilter(filter: String) {
        val currentFilters = _selectedFilters.value.toMutableSet()
        if (currentFilters.contains(filter)) {
            currentFilters.remove(filter)
        } else {
            currentFilters.add(filter)
        }
        _selectedFilters.value = currentFilters
    }

    /**
     * Clears the current error message from the UI state.
     * @author Carl Lundholm
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Retries loading exercises after a failure.
     * @author Carl Lundholm
     */
    fun retryLoadingExercises() {
        _uiState.value = _uiState.value.copy(error = null)
        initializeData()
    }

    /**
     * Forces a refresh of exercise data from the API.
     * @author Carl Lundholm
     */
    fun forceRefreshExercises() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                forceRefreshExercisesUseCase()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                val appError = e.toAppError()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = appError.message
                )
            }
        }
    }

    /**
     * Loads all workout templates for the add-to-template feature.
     * @author Carl Lundholm
     */
    fun loadTemplates() {
        viewModelScope.launch {
            try {
                workoutRepository.getAllTemplates().collect { templates ->
                    _templates.value = templates
                }
            } catch (e: Exception) {
                val appError = e.toAppError()
                _uiState.value = _uiState.value.copy(error = appError.message)
            }
        }
    }

    /**
     * Adds an exercise to an existing template.
     * @param exerciseId ID of the exercise to add.
     * @param templateId ID of the template to add the exercise to.
     * @param sets Number of sets.
     * @param reps Number of repetitions per set.
     * @param restSec Rest time between sets in seconds.
     * @author Carl Lundholm
     */
    fun addExerciseToTemplate(
        exerciseId: Long,
        templateId: Long,
        sets: Int,
        reps: Int,
        restSec: Int
    ) {
        viewModelScope.launch {
            try {
                val template = workoutRepository.getTemplateById(templateId)
                val nextPosition = (template?.exercises?.maxOfOrNull { it.position } ?: -1) + 1

                val templateExercise = TemplateExercise(
                    exerciseId = exerciseId,
                    sets = sets,
                    reps = reps,
                    restSec = restSec,
                    position = nextPosition
                )

                workoutRepository.addExerciseToTemplate(templateId, templateExercise)

                loadTemplates()
            } catch (e: Exception) {
                val appError = e.toAppError()
                _uiState.value = _uiState.value.copy(error = appError.message)
            }
        }
    }

    /**
     * Creates a new template with an exercise as the first exercise.
     * @param templateName Name for the new template.
     * @param firstExerciseId ID of the first exercise to add.
     * @author Carl Lundholm
     */
    fun createNewTemplate(templateName: String, firstExerciseId: Long) {
        viewModelScope.launch {
            try {
                val newTemplate = se.umu.calu0217.strive.domain.models.WorkoutTemplate(
                    name = templateName,
                    createdAt = System.currentTimeMillis()
                )

                val templateId = workoutRepository.insertTemplate(newTemplate)

                val templateExercise = TemplateExercise(
                    exerciseId = firstExerciseId,
                    sets = 3,
                    reps = 12,
                    restSec = 60,
                    position = 0
                )

                workoutRepository.addExerciseToTemplate(templateId, templateExercise)

                loadTemplates()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create new template: ${e.localizedMessage}"
                )
            }
        }
    }
}

/**
 * UI state for the explore screen.
 * @property isLoading Indicates if data is being loaded.
 * @property error Error message to display, if any.
 * @author Carl Lundholm
 */
data class ExploreUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)