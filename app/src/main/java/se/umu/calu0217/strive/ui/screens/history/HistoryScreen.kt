package se.umu.calu0217.strive.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.umu.calu0217.strive.R

/**
 * Screen displaying workout and run history with weekly statistics.
 * Provides tabs to switch between workout sessions and run sessions.
 *
 * @param viewModel The view model managing history data (injected via Hilt).
 */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val workoutSessions by viewModel.workoutSessions.collectAsStateWithLifecycle()
    val runSessions by viewModel.runSessions.collectAsStateWithLifecycle()
    val weeklyStats by viewModel.weeklyStats.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Activity History",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                WeeklyStatsCard(weeklyStats = weeklyStats)
            }

            item {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal
                ) {
                    Tab(
                        selected = selectedTab == HistoryTab.WORKOUTS,
                        onClick = { viewModel.selectTab(HistoryTab.WORKOUTS) },
                        text = { Text(stringResource(R.string.workouts_tab)) },
                        icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == HistoryTab.RUNS,
                        onClick = { viewModel.selectTab(HistoryTab.RUNS) },
                        text = { Text(stringResource(R.string.runs_tab)) },
                        icon = { Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null) }
                    )
                }
            }

            when (selectedTab) {
                HistoryTab.WORKOUTS -> {
                    if (workoutSessions.isEmpty()) {
                        item {
                            EmptyStateMessage(
                                icon = Icons.Default.FitnessCenter,
                                message = "No workouts yet",
                                description = "Complete a workout to see it here"
                            )
                        }
                    } else {
                        items(workoutSessions) { session ->
                            WorkoutSessionCard(
                                session = session,
                                onClick = { /* No action defined */ }
                            )
                        }
                    }
                }
                HistoryTab.RUNS -> {
                    if (runSessions.isEmpty()) {
                        item {
                            EmptyStateMessage(
                                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                                message = "No runs yet",
                                description = "Complete a run to see it here"
                            )
                        }
                    } else {
                        items(runSessions) { session ->
                            RunSessionCard(
                                session = session,
                                onClick = { /* No action defined */ }
                            )
                        }
                    }
                }
            }
        }
    }
}
