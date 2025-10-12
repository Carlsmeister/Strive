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
import se.umu.calu0217.strive.core.utils.isLandscape
import se.umu.calu0217.strive.core.utils.isCompactScreen
import se.umu.calu0217.strive.core.utils.AdaptiveSpacing
import se.umu.calu0217.strive.core.utils.AdaptiveIconSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

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
    val isCompact = isCompactScreen()
    val isLandscape = isLandscape()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AdaptiveSpacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = templateName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AdaptiveSpacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.sets_progress, doneSets, totalSets),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        ElapsedTimeClock(startTime = startTime, isPaused = isPaused, compact = true)
                    }
                }
                Button(
                    onClick = onPauseToggle,
                    contentPadding = PaddingValues(
                        horizontal = AdaptiveSpacing.medium,
                        vertical = AdaptiveSpacing.small
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    if (isPaused) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.resume), style = MaterialTheme.typography.bodySmall)
                    } else {
                        Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.pause), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(AdaptiveSpacing.standard)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = templateName,
                        style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(AdaptiveSpacing.small))
                    Text(
                        text = stringResource(R.string.sets_progress, doneSets, totalSets),
                        style = if (isCompact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
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
                        .padding(top = AdaptiveSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ElapsedTimeClock(startTime = startTime, isPaused = isPaused)
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = onPauseToggle,
                        contentPadding = PaddingValues(
                            horizontal = AdaptiveSpacing.medium,
                            vertical = AdaptiveSpacing.small
                        )
                    ) {
                        if (isPaused) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(UiConstants.EXTRA_SMALL_PADDING))
                            Text(stringResource(R.string.resume))
                        } else {
                            Icon(Icons.Filled.Pause, contentDescription = null)
                            Spacer(modifier = Modifier.width(UiConstants.EXTRA_SMALL_PADDING))
                            Text(stringResource(R.string.pause))
                        }
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
    val isLandscape = isLandscape()
    val isCompact = isCompactScreen()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = if (isLandscape || isCompact) AdaptiveSpacing.small else AdaptiveSpacing.medium,
                    horizontal = AdaptiveSpacing.standard
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(if (isLandscape || isCompact) 20.dp else AdaptiveIconSize.small)
                )
                Spacer(modifier = Modifier.width(AdaptiveSpacing.small))
                Text(
                    text = stringResource(R.string.rest_time, formatRestTime(timeRemaining)),
                    style = if (isLandscape || isCompact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
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
    isPaused: Boolean,
    compact: Boolean = false
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
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(if (compact) 16.dp else AdaptiveIconSize.small)
        )
        Spacer(modifier = Modifier.width(if (compact) 4.dp else AdaptiveSpacing.small))
        Text(
            text = if (compact) FitnessUtils.formatTime(elapsedSec.toInt()) else "Elapsed: ${FitnessUtils.formatTime(elapsedSec.toInt())}",
            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.titleMedium,
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
    val isCompact = isCompactScreen()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .height(if (isCompact) 48.dp else 56.dp)
                .padding(horizontal = AdaptiveSpacing.medium),
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
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(if (isCompact) 20.dp else 24.dp))
                        Spacer(modifier = Modifier.width(UiConstants.HALF_SMALL_PADDING))
                        Text(
                            stringResource(R.string.complete_set),
                            style = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
                        )
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
                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(if (isCompact) 20.dp else 24.dp))
                        Spacer(modifier = Modifier.width(UiConstants.HALF_SMALL_PADDING))
                        Text(
                            stringResource(R.string.stop),
                            style = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
                        )
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
    onCompleteSet: (Int, Int, Double?) -> Unit,
    lastWeightForExercise: Double? = null
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

            Spacer(modifier = Modifier.height(UiConstants.MEDIUM_PADDING))

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

    if (showInfoDialog) {
        ExerciseDetailDialog(exercise = exercise, onDismiss = { showInfoDialog = false })
    }

    showRepsDialog?.let { setIndex ->
        var repsInput by remember { mutableStateOf(templateExercise.reps.toString()) }
        var weightInput by remember {
            mutableStateOf(
                lastWeightForExercise?.toString() ?: ""
            )
        }

        AlertDialog(
            onDismissRequest = { showRepsDialog = null },
            title = { Text(stringResource(R.string.set_number, setIndex + 1) + " - ${exercise.name}") },
            text = {
                Column {
                    Text(stringResource(R.string.reps_question))
                    Spacer(modifier = Modifier.height(UiConstants.SMALL_PADDING))
                    OutlinedTextField(
                        value = repsInput,
                        onValueChange = { repsInput = it },
                        label = { Text(stringResource(R.string.reps_completed)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    if (exercise.usesWeight) {
                        Spacer(modifier = Modifier.height(UiConstants.MEDIUM_PADDING))
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Weight (kg)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            placeholder = { Text("Enter weight") }
                        )
                        if (lastWeightForExercise != null) {
                            Text(
                                text = "Last: $lastWeightForExercise kg",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isRestMode,
                    onClick = {
                        val reps = repsInput.toIntOrNull() ?: templateExercise.reps
                        val weight = if (exercise.usesWeight) weightInput.toDoubleOrNull() else null
                        onCompleteSet(setIndex, reps, weight)
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