package se.umu.calu0217.strive.ui.screens.run

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.umu.calu0217.strive.R
import se.umu.calu0217.strive.core.utils.FitnessUtils
import se.umu.calu0217.strive.ui.components.StatItem

/**
 * Displays a notice when Google Maps API key is missing.
 * Provides instructions for configuring the API key.
 */
@Composable
fun MapsApiKeyMissingNotice() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.maps_api_key_missing_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.maps_api_key_missing),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * Displays real-time running statistics during an active run session.
 * Shows distance, elapsed time, pace, and estimated calories burned.
 *
 * @param distance Distance covered in kilometers.
 * @param elapsedTime Time elapsed in seconds.
 * @param pace Current pace in minutes per kilometer.
 */
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
                    text = stringResource(R.string.ready_to_run),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.choose_activity_hint),
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
                GpsStatus.READY -> Icons.Filled.GpsFixed
                GpsStatus.SEARCHING -> Icons.Filled.GpsNotFixed
                GpsStatus.ERROR -> Icons.Filled.GpsOff
            }
            val badgeText = when (gpsStatus) {
                GpsStatus.READY -> stringResource(R.string.gps_ready)
                GpsStatus.SEARCHING -> stringResource(R.string.gps_searching)
                GpsStatus.ERROR -> stringResource(R.string.gps_error)
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
                tint = androidx.compose.ui.graphics.Color.Black
            )
            Spacer(Modifier.width(4.dp))
            Text(selectedActivity.label(), color = androidx.compose.ui.graphics.Color.Black)
            Spacer(Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.Black
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
                    text = stringResource(R.string.start),
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
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Start")
                }
            } else {
                Text(
                    text = selectedActivity.label(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = fg
                )
                FilledIconButton(onClick = onStopRun, colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Filled.Stop, contentDescription = "Stop")
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

@Composable
fun MapContent(
    modifier: Modifier = Modifier,
    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
    hasLocationPermission: Boolean,
    currentLat: Double?,
    currentLng: Double?
) {
    com.google.maps.android.compose.GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = com.google.maps.android.compose.MapProperties(
            isMyLocationEnabled = hasLocationPermission
        ),
        uiSettings = com.google.maps.android.compose.MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
        )
    ) {
        if (currentLat != null && currentLng != null) {
            val markerState = remember(currentLat, currentLng) {
                com.google.maps.android.compose.MarkerState(
                    position = com.google.android.gms.maps.model.LatLng(currentLat, currentLng)
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
}

fun isMapsApiKeyConfigured(context: android.content.Context): Boolean {
    return try {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            android.content.pm.PackageManager.GET_META_DATA
        )
        val key = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
        !key.isNullOrBlank()
    } catch (_: Exception) {
        false
    }
}
