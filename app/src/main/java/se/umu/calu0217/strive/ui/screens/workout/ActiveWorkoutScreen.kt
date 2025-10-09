package se.umu.calu0217.strive.ui.screens.workout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.umu.calu0217.strive.R
import se.umu.calu0217.strive.core.constants.UiConstants
import se.umu.calu0217.strive.ui.components.AddExerciseDialog
import se.umu.calu0217.strive.ui.components.LoadingIndicator
import se.umu.calu0217.strive.ui.components.ConfirmationDialog

/**
 * Screen for an active workout session in progress.
 * Displays current exercise, set tracking, rest timer, and workout progress.
 *
 * @param onNavigateBack Callback to navigate back when the workout is finished or cancelled.
 * @param viewModel The view model managing the active workout state (injected via Hilt).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    onNavigateBack: () -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showCongratsDialog by rememberSaveable { mutableStateOf(false) }
    var showExitConfirmDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        showExitConfirmDialog = true
    }

    if (uiState.isLoading) {
        LoadingIndicator()
        return
    }

    if (uiState.error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.error_prefix, uiState.error!!),
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(stringResource(R.string.go_back))
                }
            }
        }
        return
    }

    val template = uiState.template ?: return
    val session = uiState.session ?: return

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(UiConstants.STANDARD_PADDING)
        ) {
            // Header with workout info
            run {
                val totalSets = template.exercises.sumOf { it.sets }
                val doneSets = uiState.completedSets.size
                WorkoutHeader(
                    templateName = template.name,
                    startTime = session.startedAt,
                    isPaused = uiState.isPaused,
                    doneSets = doneSets,
                    totalSets = totalSets,
                    onPauseToggle = { if (uiState.isPaused) viewModel.resumeWorkout() else viewModel.pauseWorkout() }
                )
            }

            Spacer(modifier = Modifier.height(UiConstants.STANDARD_PADDING))

            // Rest timer overlay
            if (uiState.isRestMode) {
                RestTimerCard(
                    timeRemaining = uiState.restTimeRemaining,
                    onSkipRest = viewModel::skipRest
                )
                Spacer(modifier = Modifier.height(UiConstants.STANDARD_PADDING))
            }

            // Add exercise + controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add exercise (compact) aligned to start (left)
                FilledTonalButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = UiConstants.MEDIUM_PADDING, vertical = UiConstants.SMALL_PADDING)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(UiConstants.HALF_SMALL_PADDING))
                    Text(stringResource(R.string.add_exercise))
                }

                // Push controls to the far right
                Spacer(modifier = Modifier.weight(1f))

                // Right-aligned controls group removed; merged into bottom split FAB
            }
            Spacer(modifier = Modifier.height(UiConstants.MEDIUM_PADDING))

            if (template.exercises.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(UiConstants.STANDARD_PADDING), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.no_exercises_added),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(UiConstants.SMALL_PADDING))
                        Button(onClick = { showAddDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(UiConstants.HALF_SMALL_PADDING))
                            Text(stringResource(R.string.add_your_first_exercise))
                        }
                    }
                }
            } else {
                // Exercise list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(UiConstants.MEDIUM_PADDING),
                    contentPadding = PaddingValues(bottom = 70.dp)
                ) {
                    itemsIndexed(template.exercises) { index, templateExercise ->
                        val exercise = uiState.exercises.find { it.id == templateExercise.exerciseId }
                        if (exercise != null) {
                            ExerciseCard(
                                exercise = exercise,
                                templateExercise = templateExercise,
                                completedSets = uiState.completedSets,
                                isRestMode = uiState.isRestMode,
                                canMoveUp = index > 0,
                                canMoveDown = index < template.exercises.size - 1,
                                onMoveUp = { viewModel.moveExerciseUp(index) },
                                onMoveDown = { viewModel.moveExerciseDown(index) },
                                onCompleteSet = { setIndex, reps ->
                                    viewModel.completeSet(exercise.id, setIndex, reps)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Bottom-center split Complete|Stop button (FAB-like)
        val completeEnabled = template.exercises.any { te ->
            (0 until te.sets).all { setIndex ->
                uiState.completedSets.containsKey("${te.exerciseId}_$setIndex")
            }
        }
        SplitActionFab(
            onComplete = { showCongratsDialog = true },
            onStop = { viewModel.finishWorkoutAuto(onNavigateBack) },
            completeEnabled = completeEnabled,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(UiConstants.STANDARD_PADDING)
        )

    }

    if (showAddDialog) {
        AddExerciseDialog(
            availableExercises = uiState.availableExercises,
            onDismiss = { showAddDialog = false },
            onAdd = { exercise, sets, reps, restSec ->
                viewModel.addExercise(exercise.id, sets, reps, restSec)
                showAddDialog = false
            }
        )
    }

    if (showCongratsDialog) {
        AlertDialog(
            onDismissRequest = { showCongratsDialog = false },
            title = { Text("Well done!🎉") },
            text = { Text("One step closer to your goal!🚀") },
            confirmButton = {
                TextButton(onClick = {
                    showCongratsDialog = false
                    viewModel.finishWorkoutAuto(onNavigateBack)
                }) { Text("Continue") }
            }
        )
    }

    // Exit confirmation dialog
    if (showExitConfirmDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.exit_workout_title),
            message = stringResource(R.string.exit_workout_message),
            confirmText = stringResource(R.string.exit),
            dismissText = stringResource(R.string.cancel),
            onDismiss = { showExitConfirmDialog = false },
            onConfirm = {
                showExitConfirmDialog = false
                viewModel.finishWorkoutAuto(onNavigateBack)
            }
        )
    }
}
