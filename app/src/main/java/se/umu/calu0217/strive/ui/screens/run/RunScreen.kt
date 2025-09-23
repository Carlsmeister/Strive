package se.umu.calu0217.strive.ui.screens.run

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.umu.calu0217.strive.core.utils.FitnessUtils
import se.umu.calu0217.strive.core.utils.PermissionUtils

@Composable
fun RunScreen(
    onNavigateToRunDetail: (Long) -> Unit,
    viewModel: RunViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.startRun()
        } else {
            // Handle permission denied
            viewModel.showPermissionError()
        }
    }

    // Check permissions status on launch (but don't auto-start run)
    LaunchedEffect(Unit) {
        val hasPermissions = PermissionUtils.hasLocationPermissions(context)
        if (hasPermissions) {
            viewModel.setGpsReady()
        } else {
            viewModel.setGpsNotReady()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // GPS Status Indicator
        GpsStatusCard(gpsStatus = uiState.gpsStatus)

        Spacer(modifier = Modifier.height(24.dp))

        // KPI Panel with large numbers
        if (uiState.isRunning) {
            RunningStatsPanel(
                distance = uiState.distance,
                elapsedTime = uiState.elapsedTime,
                pace = uiState.pace
            )
        } else {
            ReadyToRunPanel()
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Intensity Selector (only show when not running)
        if (!uiState.isRunning) {
            IntensitySelector(
                selectedIntensity = uiState.selectedIntensity,
                onIntensitySelected = viewModel::setIntensity
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Start/Stop Button
        RunControlButton(
            isRunning = uiState.isRunning,
            onStartRun = {
                val hasPermissions = PermissionUtils.hasLocationPermissions(context)
                if (hasPermissions) {
                    viewModel.startRun()
                } else {
                    permissionLauncher.launch(PermissionUtils.LOCATION_PERMISSIONS)
                }
            },
            onStopRun = { viewModel.stopRun() },
            gpsReady = uiState.gpsStatus == GpsStatus.READY
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
        }
    }

    // Show summary dialog after run completion
    if (uiState.showSummary) {
        RunSummaryDialog(
            distance = uiState.distance,
            elapsedTime = uiState.elapsedTime,
            pace = uiState.pace,
            onDismiss = viewModel::dismissSummary,
            onViewDetails = { /* Navigate to run detail */ }
        )
    }
}

@Composable
fun GpsStatusCard(gpsStatus: GpsStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (gpsStatus) {
                GpsStatus.READY -> MaterialTheme.colorScheme.primaryContainer
                GpsStatus.SEARCHING -> MaterialTheme.colorScheme.secondaryContainer
                GpsStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (gpsStatus) {
                    GpsStatus.READY -> Icons.Default.GpsFixed
                    GpsStatus.SEARCHING -> Icons.Default.GpsNotFixed
                    GpsStatus.ERROR -> Icons.Default.GpsOff
                },
                contentDescription = null,
                tint = when (gpsStatus) {
                    GpsStatus.READY -> MaterialTheme.colorScheme.onPrimaryContainer
                    GpsStatus.SEARCHING -> MaterialTheme.colorScheme.onSecondaryContainer
                    GpsStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = when (gpsStatus) {
                    GpsStatus.READY -> "GPS Ready"
                    GpsStatus.SEARCHING -> "Searching for GPS..."
                    GpsStatus.ERROR -> "GPS Error"
                },
                style = MaterialTheme.typography.titleMedium,
                color = when (gpsStatus) {
                    GpsStatus.READY -> MaterialTheme.colorScheme.onPrimaryContainer
                    GpsStatus.SEARCHING -> MaterialTheme.colorScheme.onSecondaryContainer
                    GpsStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                }
            )
        }
    }
}

@Composable
fun RunningStatsPanel(
    distance: Double,
    elapsedTime: Int,
    pace: Double
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Time (largest)
        StatItem(
            label = "TIME",
            value = FitnessUtils.formatTime(elapsedTime),
            textSize = 48.sp
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Distance
            StatItem(
                label = "DISTANCE",
                value = FitnessUtils.formatDistance(distance),
                textSize = 32.sp
            )

            // Pace
            StatItem(
                label = "PACE",
                value = FitnessUtils.formatPace(pace),
                textSize = 32.sp
            )
        }
    }
}

@Composable
fun ReadyToRunPanel() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ready to run?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select your intensity and start tracking your run",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    textSize: androidx.compose.ui.unit.TextUnit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = textSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun IntensitySelector(
    selectedIntensity: RunIntensity,
    onIntensitySelected: (RunIntensity) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Intensity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RunIntensity.entries.forEach { intensity ->
                FilterChip(
                    selected = selectedIntensity == intensity,
                    onClick = { onIntensitySelected(intensity) },
                    label = { Text(intensity.displayName) }
                )
            }
        }
    }
}

@Composable
fun RunControlButton(
    isRunning: Boolean,
    onStartRun: () -> Unit,
    onStopRun: () -> Unit,
    gpsReady: Boolean
) {
    Button(
        onClick = if (isRunning) onStopRun else onStartRun,
        enabled = !isRunning || gpsReady,
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = if (isRunning) "Stop" else "Start",
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun RunSummaryDialog(
    distance: Double,
    elapsedTime: Int,
    pace: Double,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Run Complete!") },
        text = {
            Column {
                Text("Great job! Here's your summary:")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Distance: ${FitnessUtils.formatDistance(distance)}")
                Text("Time: ${FitnessUtils.formatTime(elapsedTime)}")
                Text("Pace: ${FitnessUtils.formatPace(pace)}")
            }
        },
        confirmButton = {
            TextButton(onClick = onViewDetails) {
                Text("View Details")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Add enum definitions at the end of the file
enum class GpsStatus {
    SEARCHING,
    READY,
    ERROR
}

enum class RunIntensity(val displayName: String, val metValue: Double) {
    SLOW("Slow", 5.5),
    MEDIUM("Medium", 7.0),
    FAST("Fast", 10.0)
}
