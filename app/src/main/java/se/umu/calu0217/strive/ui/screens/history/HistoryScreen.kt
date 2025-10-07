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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val workoutSessions by viewModel.workoutSessions.collectAsStateWithLifecycle()
    val runSessions by viewModel.runSessions.collectAsStateWithLifecycle()
    val weeklyStats by viewModel.weeklyStats.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Activity History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Weekly Stats Card
        WeeklyStatsCard(weeklyStats = weeklyStats)

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab.ordinal
        ) {
            Tab(
                selected = selectedTab == HistoryTab.WORKOUTS,
                onClick = { viewModel.selectTab(HistoryTab.WORKOUTS) },
                text = { Text("Workouts") },
                icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == HistoryTab.RUNS,
                onClick = { viewModel.selectTab(HistoryTab.RUNS) },
                text = { Text("Runs") },
                icon = { Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on selected tab
        when (selectedTab) {
            HistoryTab.WORKOUTS -> {
                if (workoutSessions.isEmpty()) {
                    EmptyStateMessage(
                        icon = Icons.Default.FitnessCenter,
                        message = "No workouts yet",
                        description = "Complete a workout to see it here"
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(workoutSessions) { session ->
                            WorkoutSessionCard(
                                session = session,
                                onClick = { /* No action defined */ }
                            )
                        }
                    }
                }
            }
            HistoryTab.RUNS -> {
                if (runSessions.isEmpty()) {
                    EmptyStateMessage(
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        message = "No runs yet",
                        description = "Complete a run to see it here"
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
