package se.umu.calu0217.strive.ui.screens.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.umu.calu0217.strive.R
import se.umu.calu0217.strive.core.utils.getGridColumns
import se.umu.calu0217.strive.core.utils.isLandscape
import se.umu.calu0217.strive.domain.models.Exercise
import se.umu.calu0217.strive.ui.components.AddToTemplateDialog
import se.umu.calu0217.strive.ui.components.ErrorCard
import se.umu.calu0217.strive.ui.components.ExerciseDetailDialog
import se.umu.calu0217.strive.ui.components.LoadingIndicator
import se.umu.calu0217.strive.ui.components.VerticalScrollbar

/**
 * Main screen for exploring and discovering exercises.
 * Provides search functionality, filtering by body parts, and the ability to view exercise details
 * or add exercises to workout templates.
 *
 * @param viewModel The view model managing exercise data and search state (injected via Hilt).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilters by viewModel.selectedFilters.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
        var exerciseToAddToTemplate by remember { mutableStateOf<Exercise?>(null) }

        val isLandscape = isLandscape()

        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExploreSearchBar(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier.weight(1f)
                )

                ExploreFiltersRow(
                    selectedFilters = selectedFilters,
                    onToggle = viewModel::toggleFilter,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            ExploreSearchBar(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExploreFiltersRow(
                selectedFilters = selectedFilters,
                onToggle = viewModel::toggleFilter
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                LoadingIndicator()
            }
            uiState.error != null -> {
                ExploreErrorState(
                    error = uiState.error!!,
                    onDismiss = viewModel::clearError,
                    onRetry = { viewModel.retryLoadingExercises() },
                    onForceRefresh = { viewModel.forceRefreshExercises() }
                )
            }
            exercises.isEmpty() -> {
                ExploreEmptyState()
            }
            else -> {
                ExploreExerciseList(
                    exercises = exercises,
                    onExerciseClick = { exercise -> selectedExercise = exercise },
                    onAddToTemplate = { exercise -> exerciseToAddToTemplate = exercise }
                )
            }
        }

        if (selectedExercise != null) {
            ExerciseDetailDialog(
                exercise = selectedExercise!!,
                onDismiss = { selectedExercise = null }
            )
        }

        if (exerciseToAddToTemplate != null) {
            AddToTemplateDialog(
                exercise = exerciseToAddToTemplate!!,
                onDismiss = { exerciseToAddToTemplate = null },
                onAddToTemplate = { templateId, sets, reps, restSec ->
                    viewModel.addExerciseToTemplate(
                        exerciseToAddToTemplate!!.id,
                        templateId,
                        sets,
                        reps,
                        restSec
                    )
                    exerciseToAddToTemplate = null
                },
                onCreateNewTemplate = { templateName ->
                    viewModel.createNewTemplate(templateName, exerciseToAddToTemplate!!.id)
                    exerciseToAddToTemplate = null
                }
            )
        }
    }
}

/**
 * Search bar component for filtering exercises by name, equipment, or body parts.
 *
 * @param value The current search query text.
 * @param onValueChange Callback invoked when the search query changes.
 */
@Composable
private fun ExploreSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(stringResource(R.string.search_exercises_hint)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Horizontal row of filter chips for selecting body part categories.
 * Allows multi-selection of body parts to filter exercises.
 *
 * @param selectedFilters Set of currently selected filter strings.
 * @param onToggle Callback invoked when a filter is toggled on/off.
 */
@Composable
private fun ExploreFiltersRow(
    selectedFilters: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLandscape = isLandscape()

    LazyRow(
        modifier = if (isLandscape) {
            modifier.height(56.dp)
        } else {
            modifier
        },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val filters = listOf("chest", "back", "legs", "shoulders", "arms", "core")
        items(filters) { filter ->
            FilterChip(
                selected = selectedFilters.contains(filter),
                onClick = { onToggle(filter) },
                label = { Text(filter.replaceFirstChar { it.uppercase() }) },
                modifier = if (isLandscape) {
                    Modifier.height(48.dp)
                } else {
                    Modifier
                }
            )
        }
    }
}

/**
 * Displays an error state with retry and refresh options.
 *
 * @param error The error message to display.
 * @param onDismiss Callback to dismiss the error.
 * @param onRetry Callback to retry the failed operation.
 * @param onForceRefresh Callback to force refresh exercise data from the API.
 */
@Composable
private fun ExploreErrorState(
    error: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onForceRefresh: () -> Unit
) {
    ErrorCard(
        error = error,
        onDismiss = onDismiss,
        onRetry = onRetry,
        onForceRefresh = onForceRefresh
    )
}

/**
 * Displays an empty state message when no exercises are found.
 */
@Composable
private fun ExploreEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_exercises_found),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Scrollable list of exercise cards with a custom vertical scrollbar.
 * Uses adaptive grid layout for larger screens and landscape mode.
 *
 * @param exercises List of exercises to display.
 * @param onExerciseClick Callback invoked when an exercise card is clicked.
 * @param onAddToTemplate Callback invoked when the "add to template" button is clicked.
 */
@Composable
private fun ExploreExerciseList(
    exercises: List<Exercise>,
    onExerciseClick: (Exercise) -> Unit,
    onAddToTemplate: (Exercise) -> Unit
) {
    val listState = rememberLazyListState()
    val gridColumns = getGridColumns()

    Box(modifier = Modifier.fillMaxSize()) {
        if (gridColumns == 1) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(exercises) { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        onClick = { onExerciseClick(exercise) },
                        onAddToTemplate = { onAddToTemplate(exercise) }
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 16.dp, bottom = 16.dp)
            ) {
                items(exercises.chunked(gridColumns)) { rowExercises ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowExercises.forEach { exercise ->
                            Box(modifier = Modifier.weight(1f)) {
                                ExerciseCard(
                                    exercise = exercise,
                                    onClick = { onExerciseClick(exercise) },
                                    onAddToTemplate = { onAddToTemplate(exercise) }
                                )
                            }
                        }
                        repeat(gridColumns - rowExercises.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        VerticalScrollbar(
            listState = listState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(6.dp)
                .padding(vertical = 8.dp)
        )
    }
}
