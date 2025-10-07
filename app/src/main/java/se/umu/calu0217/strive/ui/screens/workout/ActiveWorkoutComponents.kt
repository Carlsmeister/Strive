package se.umu.calu0217.strive.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.umu.calu0217.strive.core.utils.DateTimeUtils.formatTimeOfDay
import se.umu.calu0217.strive.core.utils.FitnessUtils
import se.umu.calu0217.strive.domain.models.Exercise
import se.umu.calu0217.strive.domain.models.TemplateExercise
import se.umu.calu0217.strive.ui.components.ExerciseDetailDialog
import java.util.Locale

@Composable
fun WorkoutHeader(
    templateName: String,
    startTime: Long,
    isPaused: Boolean,
    doneSets: Int,
    totalSets: Int,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = templateName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sets $doneSets/$totalSets",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = "Started: ${formatTimeOfDay(startTime)}",
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
fun RestTimerCard(
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
fun ElapsedTimeClock(
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
            text = "Elapsed: ${FitnessUtils.formatTime(elapsedSec.toInt())}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun SplitActionFab(
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
fun ExerciseCard(
    exercise: Exercise,
    templateExercise: TemplateExercise,
    completedSets: Map<String, Boolean>,
    isRestMode: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
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
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Move up")
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(Icons.Filled.ArrowDownward, contentDescription = "Move down")
                }
                IconButton(onClick = { showInfoDialog = true }) {
                    Icon(Icons.Filled.Info, contentDescription = "Exercise info", modifier = Modifier.size(24.dp))
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
                            Icon(Icons.Filled.Check, contentDescription = null)
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

fun formatRestTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return if (mins > 0) {
        String.format(Locale.getDefault(), "%d:%02d", mins, secs)
    } else {
        "${secs}s"
    }
}