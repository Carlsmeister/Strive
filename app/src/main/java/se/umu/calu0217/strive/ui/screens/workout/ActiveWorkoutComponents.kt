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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.umu.calu0217.strive.R
import se.umu.calu0217.strive.core.constants.UiConstants
import se.umu.calu0217.strive.core.utils.DateTimeUtils.formatTimeOfDay
import se.umu.calu0217.strive.core.utils.FitnessUtils
import se.umu.calu0217.strive.domain.models.Exercise
import se.umu.calu0217.strive.domain.models.TemplateExercise
import se.umu.calu0217.strive.ui.components.ExerciseDetailDialog
import java.util.Locale

/**
 * Header card displaying workout session information.
 * Shows template name, start time, pause state, and progress through sets.
 *
 * @param templateName Name of the workout template being followed.
 * @param startTime Timestamp when the workout started.
 * @param isPaused Whether the workout is currently paused.
 * @param doneSets Number of completed sets.
 * @param totalSets Total number of sets in the workout.
 * @param onPauseToggle Callback to toggle pause/resume state.
 */
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
            modifier = Modifier.padding(UiConstants.STANDARD_PADDING)
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
                Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))
                Text(
                    text = stringResource(R.string.sets_progress, doneSets, totalSets),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = stringResource(R.string.started_at, formatTimeOfDay(startTime)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = UiConstants.SMALL_PADDING),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElapsedTimeClock(startTime = startTime, isPaused = isPaused)
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onPauseToggle,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = UiConstants.SMALL_PADDING)
                ) {
                    if (isPaused) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(UiConstants.EXTRA_SMALL_PADDING))
                        Text("Resume")
                    } else {
                        Icon(Icons.Filled.Pause, contentDescription = null)
                        Spacer(modifier = Modifier.width(UiConstants.EXTRA_SMALL_PADDING))
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
                .padding(UiConstants.STANDARD_PADDING),
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
                Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))
                Text(
                    text = stringResource(R.string.rest_time, formatRestTime(timeRemaining)),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            TextButton(onClick = onSkipRest) {
                Text(stringResource(R.string.skip))
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
        Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))
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
                        Text(stringResource(R.string.complete_set))
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
            modifier = Modifier.padding(UiConstants.STANDARD_PADDING)
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
                Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(Icons.Filled.ArrowUpward, contentDescription = stringResource(R.string.move_up))
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(Icons.Filled.ArrowDownward, contentDescription = stringResource(R.string.move_down))
                }
                IconButton(onClick = { showInfoDialog = true }) {
                    Icon(Icons.Filled.Info, contentDescription = "Exercise info", modifier = Modifier.size(UiConstants.SMALL_ICON_SIZE))
                }
            }
            Text(
                text = stringResource(R.string.sets_and_avg_rest, templateExercise.sets, templateExercise.restSec),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.rest_time, "${templateExercise.restSec}s"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sets
            Row(
                horizontalArrangement = Arrangement.spacedBy(UiConstants.SMALL_PADDING)
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
                            Spacer(modifier = Modifier.width(UiConstants.EXTRA_SMALL_PADDING))
                            Text("${setIndex + 1}")
                        }
                    } else {
                        Button(
                            onClick = { showRepsDialog = setIndex },
                            enabled = !isRestMode
                        ) {
                            Text(stringResource(R.string.set_number, setIndex + 1))
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
            title = { Text(stringResource(R.string.set_number, setIndex + 1) + " - ${exercise.name}") },
            text = {
                Column {
                    Text("How many reps did you complete?")
                    Spacer(modifier = Modifier.height(UiConstants.SMALL_PADDING))
                    OutlinedTextField(
                        value = repsInput,
                        onValueChange = { repsInput = it },
                        label = { Text(stringResource(R.string.reps_completed)) },
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
                    Text(stringResource(R.string.complete_set))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRepsDialog = null }) {
                    Text(stringResource(R.string.cancel))
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