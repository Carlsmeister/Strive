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
import se.umu.calu0217.strive.domain.usecases.BackfillMissingExerciseImagesUseCase
import se.umu.calu0217.strive.domain.usecases.ForceRefreshExercisesUseCase
import se.umu.calu0217.strive.domain.usecases.GetExercisesUseCase
import se.umu.calu0217.strive.domain.usecases.SearchExercisesUseCase
import se.umu.calu0217.strive.domain.usecases.SeedExercisesUseCase
import javax.inject.Inject

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

    // Added templates state
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

                // Optimize: compute trim().lowercase() only once per item
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

    private fun matchesFilters(exercise: Exercise, filters: Set<String>): Boolean {
        if (filters.isEmpty()) return true
        val parts = exercise.bodyParts.map { it.lowercase() }
        val allowedTokens = filters.flatMap { f ->
            val key = f.lowercase()
            filterAliases[key] ?: listOf(key)
        }.map { it.lowercase() }
        return parts.any { part -> allowedTokens.any { token -> part.contains(token) } }
    }

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                seedExercisesUseCase()
                // Attempt backfill for images if any are missing
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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFilter(filter: String) {
        val currentFilters = _selectedFilters.value.toMutableSet()
        if (currentFilters.contains(filter)) {
            currentFilters.remove(filter)
        } else {
            currentFilters.add(filter)
        }
        _selectedFilters.value = currentFilters
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun retryLoadingExercises() {
        _uiState.value = _uiState.value.copy(error = null)
        initializeData()
    }

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

    // Template-related methods
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

    fun addExerciseToTemplate(
        exerciseId: Long,
        templateId: Long,
        sets: Int,
        reps: Int,
        restSec: Int
    ) {
        viewModelScope.launch {
            try {
                // Get the current template to find the next position
                val template = workoutRepository.getTemplateById(templateId)
                val nextPosition = (template?.exercises?.maxOfOrNull { it.position } ?: -1) + 1

                // Create the template exercise
                val templateExercise = se.umu.calu0217.strive.domain.models.TemplateExercise(
                    exerciseId = exerciseId,
                    sets = sets,
                    reps = reps,
                    restSec = restSec,
                    position = nextPosition
                )

                // Add exercise to template through repository
                workoutRepository.addExerciseToTemplate(templateId, templateExercise)

                // Refresh templates
                loadTemplates()
            } catch (e: Exception) {
                val appError = e.toAppError()
                _uiState.value = _uiState.value.copy(error = appError.message)
            }
        }
    }

    fun createNewTemplate(templateName: String, firstExerciseId: Long) {
        viewModelScope.launch {
            try {
                // Create new template
                val newTemplate = se.umu.calu0217.strive.domain.models.WorkoutTemplate(
                    name = templateName,
                    createdAt = System.currentTimeMillis()
                )

                val templateId = workoutRepository.insertTemplate(newTemplate)

                // Add the first exercise to the new template
                val templateExercise = se.umu.calu0217.strive.domain.models.TemplateExercise(
                    exerciseId = firstExerciseId,
                    sets = 3,
                    reps = 12,
                    restSec = 60,
                    position = 0
                )

                workoutRepository.addExerciseToTemplate(templateId, templateExercise)

                // Refresh templates
                loadTemplates()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create new template: ${e.localizedMessage}"
                )
            }
        }
    }
}

data class ExploreUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)