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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.foundation.shape.RoundedCornerShape
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
import android.content.Context
import android.content.pm.PackageManager
import se.umu.calu0217.strive.ui.theme.Black

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

    // One-shot location refresh for initial map position
    LaunchedEffect(Unit) {
        viewModel.refreshCurrentLocation()
    }

    val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState()
    var hasCentered by remember { mutableStateOf(false) }

    // Center camera on first valid location only
    LaunchedEffect(uiState.currentLatitude, uiState.currentLongitude) {
        val lat = uiState.currentLatitude
        val lng = uiState.currentLongitude
        if (!hasCentered && lat != null && lng != null) {
            try {
                cameraPositionState.animate(
                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                        com.google.android.gms.maps.model.LatLng(lat, lng),
                        15f
                    )
                )
                hasCentered = true
            } catch (_: Exception) { }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Only render the map if a valid Google Maps API key is configured
        val mapsConfigured = remember { isMapsApiKeyConfigured(context) }
        if (mapsConfigured) {
            // Google Map background
            com.google.maps.android.compose.GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = com.google.maps.android.compose.MapProperties(
                    isMyLocationEnabled = PermissionUtils.hasLocationPermissions(context)
                ),
                uiSettings = com.google.maps.android.compose.MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                )
            ) {
                val lat = uiState.currentLatitude
                val lng = uiState.currentLongitude
                if (lat != null && lng != null) {
                    val markerState = remember(lat, lng) {
                        com.google.maps.android.compose.MarkerState(
                            position = com.google.android.gms.maps.model.LatLng(lat, lng)
                        )
                    }
                    com.google.maps.android.compose.Marker(
                        state = markerState,
                        icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                            com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE
                        ),
                        title = null,
                        snippet = null
                    )
                }
            }
        } else {
            // Fallback UI when API key is missing: show a helpful message instead of a blank screen
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Google Maps API key missing",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add MAPS_API_KEY to local.properties and rebuild.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Foreground UI overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // KPI Panel with large numbers
            if (uiState.isRunning) {
                RunningStatsPanel(
                    distance = uiState.distance,
                    elapsedTime = uiState.elapsedTime,
                    pace = uiState.pace
                )
            } else {
                ReadyToRunPanel(gpsStatus = uiState.gpsStatus)
            }

            Spacer(modifier = Modifier.height(32.dp))


            Spacer(modifier = Modifier.weight(1f))


            Spacer(modifier = Modifier.height(16.dp))
        }
        FloatingRunControls(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            isRunning = uiState.isRunning,
            selectedActivity = uiState.selectedActivity,
            onActivitySelected = viewModel::setActivity,
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
                GpsStatus.READY -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
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

    val bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)

    Card (
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bg),

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
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
}

@Composable
fun ReadyToRunPanel(gpsStatus: GpsStatus) {

    val bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bg),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Main content centered horizontally (keeps same inner padding as before)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 46.dp, bottom = 26.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ready to run?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Choose activity and press Start to begin",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // GPS status badge at the actual top-left corner of the card
            val badgeContainerColor = when (gpsStatus) {
                GpsStatus.READY -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                GpsStatus.SEARCHING -> MaterialTheme.colorScheme.secondaryContainer
                GpsStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
            }
            val badgeContentColor = when (gpsStatus) {
                GpsStatus.READY -> MaterialTheme.colorScheme.onPrimaryContainer
                GpsStatus.SEARCHING -> MaterialTheme.colorScheme.onSecondaryContainer
                GpsStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
            }
            val badgeIcon = when (gpsStatus) {
                GpsStatus.READY -> Icons.Default.GpsFixed
                GpsStatus.SEARCHING -> Icons.Default.GpsNotFixed
                GpsStatus.ERROR -> Icons.Default.GpsOff
            }
            val badgeText = when (gpsStatus) {
                GpsStatus.READY -> "GPS Ready"
                GpsStatus.SEARCHING -> "Searching for GPS..."
                GpsStatus.ERROR -> "GPS Error"
            }

            Surface(
                color = badgeContainerColor,
                contentColor = badgeContentColor,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(badgeIcon, contentDescription = null, tint = badgeContentColor)
                    Spacer(Modifier.width(6.dp))
                    Text(badgeText, style = MaterialTheme.typography.labelMedium, color = badgeContentColor)
                }
            }
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
fun ActivitySelectorDropdown(
    selectedActivity: ActivityType,
    onActivitySelected: (ActivityType) -> Unit,
    borderColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.outline
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        ){
            Icon(
                imageVector = activityIcon(selectedActivity),
                contentDescription = null,
                tint = Black
            )
            Spacer(Modifier.width(4.dp))
            Text(selectedActivity.label(), color = Black)
            Spacer(Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Black
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ActivityType.values().forEach { type ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(activityIcon(type), contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(type.label())
                        }
                    },
                    onClick = {
                        onActivitySelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FloatingRunControls(
    modifier: Modifier = Modifier,
    isRunning: Boolean,
    selectedActivity: ActivityType,
    onActivitySelected: (ActivityType) -> Unit,
    onStartRun: () -> Unit,
    onStopRun: () -> Unit,
    gpsReady: Boolean
) {
    val bg = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
    val fg = MaterialTheme.colorScheme.onPrimary
    Card(
        modifier = modifier.padding(bottom = 10.dp, end = 10.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isRunning) {
                Text(
                    text = "Start ",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = fg
                )
                ActivitySelectorDropdown(
                    selectedActivity = selectedActivity,
                    onActivitySelected = onActivitySelected,
                    borderColor = fg
                )
                FilledIconButton(
                    onClick = onStartRun
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                }
            } else {
                Text(
                    text = selectedActivity.label(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = fg
                )
                FilledIconButton(onClick = onStopRun, colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                }
            }
        }
    }
}

private fun ActivityType.label(): String = when (this) {
    ActivityType.RUNNING -> "Running"
    ActivityType.CYCLING -> "Cycling"
    ActivityType.WALKING -> "Walking"
}

@Composable
private fun activityIcon(activity: ActivityType) = when (activity) {
    ActivityType.RUNNING -> Icons.Outlined.DirectionsRun
    ActivityType.CYCLING -> Icons.Outlined.DirectionsBike
    ActivityType.WALKING -> Icons.Outlined.DirectionsWalk
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
        enabled = true,
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

private fun isMapsApiKeyConfigured(context: Context): Boolean {
    return try {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        val key = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
        !key.isNullOrBlank()
    } catch (_: Exception) {
        false
    }
}

// Add enum definitions at the end of the file
enum class GpsStatus {
    SEARCHING,
    READY,
    ERROR
}

