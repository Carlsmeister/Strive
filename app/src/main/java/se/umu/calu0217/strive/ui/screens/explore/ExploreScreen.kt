package se.umu.calu0217.strive.ui.screens.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import se.umu.calu0217.strive.domain.models.Exercise
import se.umu.calu0217.strive.ui.components.AddToTemplateDialog
import se.umu.calu0217.strive.ui.components.ErrorCard
import se.umu.calu0217.strive.ui.components.ExerciseDetailDialog
import se.umu.calu0217.strive.ui.components.LoadingIndicator
import se.umu.calu0217.strive.ui.components.VerticalScrollbar

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
        // Local UI state for dialogs
        var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
        var exerciseToAddToTemplate by remember { mutableStateOf<Exercise?>(null) }

        // Search Bar
        ExploreSearchBar(
            value = searchQuery,
            onValueChange = viewModel::updateSearchQuery
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips
        ExploreFiltersRow(
            selectedFilters = selectedFilters,
            onToggle = viewModel::toggleFilter
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content
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

        // Exercise Detail Dialog
        if (selectedExercise != null) {
            ExerciseDetailDialog(
                exercise = selectedExercise!!,
                onDismiss = { selectedExercise = null }
            )
        }

        // Add to Template Dialog
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








@Composable
private fun ExploreSearchBar(
    value: String,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Search exercises...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun ExploreFiltersRow(
    selectedFilters: Set<String>,
    onToggle: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val filters = listOf("chest", "back", "legs", "shoulders", "arms", "core")
        items(filters) { filter ->
            FilterChip(
                selected = selectedFilters.contains(filter),
                onClick = { onToggle(filter) },
                label = { Text(filter.replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}


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

@Composable
private fun ExploreExerciseList(
    exercises: List<Exercise>,
    onExerciseClick: (Exercise) -> Unit,
    onAddToTemplate: (Exercise) -> Unit
) {
    val listState = rememberLazyListState()
    Box(modifier = Modifier.fillMaxSize()) {
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
