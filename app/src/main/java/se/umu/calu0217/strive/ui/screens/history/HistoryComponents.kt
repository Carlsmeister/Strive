package se.umu.calu0217.strive.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.umu.calu0217.strive.core.utils.DateTimeUtils
import se.umu.calu0217.strive.core.utils.FitnessUtils
import se.umu.calu0217.strive.domain.models.RunSession
import se.umu.calu0217.strive.domain.models.WorkoutSession

/**
 * Displays a card with weekly workout and run statistics.
 * Shows total activities, minutes exercised, and distance covered.
 *
 * @param weeklyStats The weekly statistics data to display.
 */
@Composable
fun WeeklyStatsCard(weeklyStats: WeeklyStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "This Week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(
                    value = "${weeklyStats.totalWorkouts + weeklyStats.totalRuns}",
                    label = "Activities"
                )
                StatColumn(
                    value = "${weeklyStats.totalWorkoutMinutes + weeklyStats.totalRunMinutes}",
                    label = "Minutes"
                )
                StatColumn(
                    value = String.format("%.1fkm", weeklyStats.totalDistance),
                    label = "Distance"
                )
                StatColumn(
                    value = "${weeklyStats.totalCalories}",
                    label = "Calories"
                )
            }
        }
    }
}

/**
 * Displays a column showing a single statistic value with its label.
 * Used within the weekly stats card to show activities, minutes, distance, etc.
 *
 * @param value The statistic value to display (e.g., "5", "120", "10.5km").
 * @param label The label describing the statistic (e.g., "Activities", "Minutes").
 * @author Carl Lundholm
 */
@Composable
fun StatColumn(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Displays a clickable card for a completed workout session.
 * Shows workout date, duration, calories burned, and number of sets completed.
 * Clicking the card opens a detailed view of the workout.
 *
 * @param session The workout session data to display.
 * @param onClick Callback invoked when the card is clicked.
 * @author Carl Lundholm
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionCard(
    session: WorkoutSession,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Workout Session",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = DateTimeUtils.formatShortDateTime(session.startedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    session.endedAt?.let { endTime ->
                        val duration = ((endTime - session.startedAt) / 60000).toInt()
                        Text(
                            text = "${duration}m • ${session.kcal} cal • ${session.completedSets.size} sets",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Dialog displaying detailed information about a workout session.
 * Shows all exercises performed with sets, reps, and weights.
 *
 * @param session The workout session to display.
 * @param exercises Map of exercise IDs to Exercise objects.
 * @param onDismiss Callback when dialog is dismissed.
 */
@Composable
fun WorkoutDetailsDialog(
    session: WorkoutSession,
    exercises: Map<Long, se.umu.calu0217.strive.domain.models.Exercise>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Workout Details")
                Text(
                    text = DateTimeUtils.formatShortDateTime(session.startedAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val setsByExercise = session.completedSets.groupBy { it.exerciseId }

                    items(setsByExercise.entries.toList()) { (exerciseId, sets) ->
                        val exercise = exercises[exerciseId]
                        if (exercise != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = exercise.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    sets.sortedBy { it.setIndex }.forEach { set ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Set ${set.setIndex + 1}:",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = if (set.weightKg != null) {
                                                    "${set.repsDone} reps @ ${set.weightKg} kg"
                                                } else {
                                                    "${set.repsDone} reps (BW)"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                session.endedAt?.let { endTime ->
                    val duration = ((endTime - session.startedAt) / 60000).toInt()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(
                                text = "Duration: ${duration}m",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Calories: ${session.kcal}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Displays a clickable card for a completed run session.
 * Shows run date, distance, time, pace, and calories burned.
 * Clicking the card can open a detailed view (if implemented).
 *
 * @param session The run session data to display.
 * @param onClick Callback invoked when the card is clicked.
 * @author Carl Lundholm
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunSessionCard(
    session: RunSession,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Run",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = DateTimeUtils.formatShortDateTime(session.startedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (session.endedAt != null) {
                        Text(
                            text = "${FitnessUtils.formatDistance(session.distance)} • ${FitnessUtils.formatTime(session.elapsedSec)} • ${session.kcal} cal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Pace: ${FitnessUtils.formatPace(session.pace)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Displays an empty state message with an icon and descriptive text.
 * Used when there are no workouts or runs to display in the history.
 *
 * @param icon The icon to display in the empty state.
 * @param message The main message to display (e.g., "No workouts yet").
 * @param description Additional descriptive text (e.g., "Complete a workout to see it here").
 * @author Carl Lundholm
 */
@Composable
fun EmptyStateMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    description: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
