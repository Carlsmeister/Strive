package se.umu.calu0217.strive.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.umu.calu0217.strive.domain.models.Exercise
import se.umu.calu0217.strive.domain.models.TemplateExercise
import se.umu.calu0217.strive.ui.screens.explore.ExerciseDetailDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    onNavigateBack: () -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showCongratsDialog by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
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
                    text = "Error: ${uiState.error}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Go Back")
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
                .padding(16.dp)
        ) {
            // Header with workout info
            WorkoutHeader(
                templateName = template.name,
                startTime = session.startedAt,
                isPaused = uiState.isPaused,
                onPauseToggle = { if (uiState.isPaused) viewModel.resumeWorkout() else viewModel.pauseWorkout() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rest timer overlay
            if (uiState.isRestMode) {
                RestTimerCard(
                    timeRemaining = uiState.restTimeRemaining,
                    onSkipRest = viewModel::skipRest
                )
                Spacer(modifier = Modifier.height(16.dp))
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
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Exercise")
                }

                // Push controls to the far right
                Spacer(modifier = Modifier.weight(1f))

                // Right-aligned controls group removed; merged into bottom split FAB
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (template.exercises.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No exercises added yet",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add your first exercise")
                        }
                    }
                }
            } else {
                // Exercise list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(template.exercises) { index, templateExercise ->
                        val exercise = uiState.exercises.find { it.id == templateExercise.exerciseId }
                        if (exercise != null) {
                            ExerciseCard(
                                exercise = exercise,
                                templateExercise = templateExercise,
                                completedSets = uiState.completedSets,
                                isRestMode = uiState.isRestMode,
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
                .padding(16.dp)
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
}

@Composable
private fun WorkoutHeader(
    templateName: String,
    startTime: Long,
    isPaused: Boolean,
    onPauseToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = templateName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Started: ${formatTime(startTime)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Elapsed time clock on the left
                ElapsedTimeClock(startTime = startTime, isPaused = isPaused)
                Spacer(modifier = Modifier.weight(1f))
                // Pause/Resume button on the right
                Button(
                    onClick = onPauseToggle,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (isPaused) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resume")
                    } else {
                        Icon(Icons.Filled.Pause, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pause")
                    }
                }
            }
        }
    }
}

@Composable
private fun RestTimerCard(
    timeRemaining: Int,
    onSkipRest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rest: ${formatRestTime(timeRemaining)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            TextButton(onClick = onSkipRest) {
                Text("Skip")
            }
        }
    }
}

@Composable
private fun ElapsedTimeClock(
    startTime: Long,
    isPaused: Boolean
) {
    var totalPausedMs by remember { mutableStateOf(0L) }
    var pauseStartedAt by remember { mutableStateOf<Long?>(null) }
    var prevPaused by remember { mutableStateOf(isPaused) }
    var tick by remember { mutableStateOf(0L) }

    LaunchedEffect(isPaused) {
        if (isPaused && pauseStartedAt == null) {
            pauseStartedAt = System.currentTimeMillis()
        } else if (!isPaused && prevPaused) {
            val now = System.currentTimeMillis()
            pauseStartedAt?.let { start ->
                totalPausedMs += (now - start)
                pauseStartedAt = null
            }
        }
        prevPaused = isPaused
    }

    LaunchedEffect(isPaused) {
        while (true) {
            if (!isPaused) {
                tick = System.currentTimeMillis()
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    val now = if (tick == 0L) System.currentTimeMillis() else tick
    val pausedOngoing = pauseStartedAt?.let { now - it } ?: 0L
    val elapsedSec = ((now - startTime) - totalPausedMs - pausedOngoing).coerceAtLeast(0L) / 1000L

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Timer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Elapsed: ${formatElapsed(elapsedSec)}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun SplitActionFab(
    onComplete: () -> Unit,
    onStop: () -> Unit,
    completeEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(if (completeEnabled) 1f else 0.5f)
                        .clickable(enabled = completeEnabled, onClick = onComplete),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Complete")
                    }
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onStop() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stop")
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomControlsOverlay(
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    onFinishWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(onClick = onPauseToggle) {
                if (isPaused) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Resume")
                } else {
                    Icon(Icons.Filled.Pause, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pause")
                }
            }
            Button(
                onClick = onFinishWorkout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Filled.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stop")
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    templateExercise: TemplateExercise,
    completedSets: Map<String, Boolean>,
    isRestMode: Boolean,
    onCompleteSet: (Int, Int) -> Unit
) {
    var showRepsDialog by remember { mutableStateOf<Int?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { showInfoDialog = true }) {
                    Icon(Icons.Filled.Info, contentDescription = "Exercise info", modifier = Modifier.size(30.dp))
                }
            }
            Text(
                text = "${templateExercise.sets} sets × ${templateExercise.reps} reps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Rest: ${templateExercise.restSec}s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sets
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(templateExercise.sets) { setIndex ->
                    val setKey = "${exercise.id}_$setIndex"
                    val isCompleted = completedSets[setKey] == true

                    if (isCompleted) {
                        FilledTonalButton(
                            onClick = { },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = false
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${setIndex + 1}")
                        }
                    } else {
                        Button(
                            onClick = { showRepsDialog = setIndex },
                            enabled = !isRestMode
                        ) {
                            Text("Set ${setIndex + 1}")
                        }
                    }
                }
            }
        }
    }

    // Exercise info dialog
    if (showInfoDialog) {
        ExerciseDetailDialog(exercise = exercise, onDismiss = { showInfoDialog = false })
    }

    // Reps input dialog
    showRepsDialog?.let { setIndex ->
        var repsInput by remember { mutableStateOf(templateExercise.reps.toString()) }

        AlertDialog(
            onDismissRequest = { showRepsDialog = null },
            title = { Text("Set ${setIndex + 1} - ${exercise.name}") },
            text = {
                Column {
                    Text("How many reps did you complete?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = repsInput,
                        onValueChange = { repsInput = it },
                        label = { Text("Reps completed") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isRestMode,
                    onClick = {
                        val reps = repsInput.toIntOrNull() ?: templateExercise.reps
                        onCompleteSet(setIndex, reps)
                        showRepsDialog = null
                    }
                ) {
                    Text("Complete Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRepsDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AddExerciseDialog(
    availableExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onAdd: (Exercise, Int, Int, Int) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var setsInput by remember { mutableStateOf("3") }
    var repsInput by remember { mutableStateOf("10") }
    var restInput by remember { mutableStateOf("60") }

    val filtered = remember(query, availableExercises) {
        if (query.isBlank()) availableExercises.take(20) else availableExercises.filter { it.name.contains(query, ignoreCase = true) }.take(20)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exercise") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search exercises") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Exercise list
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    itemsIndexed(filtered) { _, ex ->
                        ListItem(
                            headlineContent = { Text(ex.name) },
                            supportingContent = { Text(ex.bodyParts.joinToString()) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .let { it }
                                .then(Modifier)
                                .clickable {
                                    selectedExercise = ex
                                    query = ex.name
                                }
                        )
                        Divider()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                selectedExercise?.let {
                    Text("Selected: ${it.name}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = setsInput,
                        onValueChange = { setsInput = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("Sets") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = repsInput,
                        onValueChange = { repsInput = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("Reps") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = restInput,
                        onValueChange = { restInput = it.filter { ch -> ch.isDigit() }.take(4) },
                        label = { Text("Rest (s)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedExercise != null,
                onClick = {
                    val ex = selectedExercise ?: return@TextButton
                    val sets = setsInput.toIntOrNull() ?: 3
                    val reps = repsInput.toIntOrNull() ?: 10
                    val rest = restInput.toIntOrNull() ?: 60
                    onAdd(ex, sets, reps, rest)
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun formatTime(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return format.format(date)
}

private fun formatRestTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return if (mins > 0) {
        String.format("%d:%02d", mins, secs)
    } else {
        "${secs}s"
    }
}

private fun formatElapsed(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
