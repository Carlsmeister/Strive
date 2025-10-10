package se.umu.calu0217.strive.ui.screens.run

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.umu.calu0217.strive.R
import se.umu.calu0217.strive.core.constants.UiConstants
import se.umu.calu0217.strive.core.utils.FitnessUtils
import se.umu.calu0217.strive.ui.components.StatItem
import se.umu.calu0217.strive.core.utils.isLandscape
import se.umu.calu0217.strive.core.utils.isCompactScreen
import se.umu.calu0217.strive.core.utils.AdaptiveSpacing
import se.umu.calu0217.strive.core.utils.getAdaptiveSizeMultiplier

/**
 * Displays a notice when Google Maps API key is missing.
 * Provides instructions for configuring the API key.
 */
@Composable
fun MapsApiKeyMissingNotice() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Column(
                modifier = Modifier.padding(UiConstants.STANDARD_PADDING),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.maps_api_key_missing_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(UiConstants.SMALL_PADDING))
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
    pace: Double,
    modifier: Modifier = Modifier
) {
    val bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
    val isLandscape = isLandscape()
    val isCompact = isCompactScreen()
    val sizeMultiplier = getAdaptiveSizeMultiplier()

    Card (
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bg),
    ) {
        if (isLandscape) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(
                        vertical = if (isCompact) AdaptiveSpacing.small else AdaptiveSpacing.medium,
                        horizontal = AdaptiveSpacing.standard
                    ),
            ) {
                StatItem(
                    label = "TIME",
                    value = FitnessUtils.formatTime(elapsedTime),
                    textSize = minOf(32.sp.value * sizeMultiplier, 40f).sp
                )

                StatItem(
                    label = "DISTANCE",
                    value = FitnessUtils.formatDistance(distance),
                    textSize = minOf(28.sp.value * sizeMultiplier, 36f).sp
                )

                StatItem(
                    label = "PACE",
                    value = FitnessUtils.formatPace(pace),
                    textSize = minOf(28.sp.value * sizeMultiplier, 36f).sp
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = if (isCompact) AdaptiveSpacing.small else AdaptiveSpacing.medium),
            ) {
                StatItem(
                    label = "TIME",
                    value = FitnessUtils.formatTime(elapsedTime),
                    textSize = minOf(48.sp.value * sizeMultiplier, 56f).sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(if (isCompact) 16.dp else 32.dp)
                ) {
                    StatItem(
                        label = "DISTANCE",
                        value = FitnessUtils.formatDistance(distance),
                        textSize = minOf(32.sp.value * sizeMultiplier, 40f).sp
                    )

                    StatItem(
                        label = "PACE",
                        value = FitnessUtils.formatPace(pace),
                        textSize = minOf(32.sp.value * sizeMultiplier, 40f).sp
                    )
                }
            }
        }
    }
}

@Composable
fun ReadyToRunPanel(
    gpsStatus: GpsStatus,
    modifier: Modifier = Modifier
) {
    val bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
    val isCompact = isCompactScreen()
    val isLandscape = isLandscape()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bg),
    ) {
        if (isLandscape) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AdaptiveSpacing.small)
            ) {
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
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = AdaptiveSpacing.small,
                            vertical = AdaptiveSpacing.extraSmall
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(badgeIcon, contentDescription = null, tint = badgeContentColor)
                        Spacer(Modifier.width(UiConstants.HALF_SMALL_PADDING))
                        Text(badgeText, style = MaterialTheme.typography.labelMedium, color = badgeContentColor)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AdaptiveSpacing.small),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = stringResource(R.string.ready_to_run),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.choose_activity_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = if (isCompact) 32.dp else 46.dp,
                            bottom = if (isCompact) 16.dp else 26.dp,
                            start = AdaptiveSpacing.standard,
                            end = AdaptiveSpacing.standard
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.ready_to_run),
                        style = if (isCompact)
                            MaterialTheme.typography.titleLarge
                        else
                            MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(if (isCompact) 4.dp else AdaptiveSpacing.small))
                    Text(
                        text = stringResource(R.string.choose_activity_hint),
                        style = if (isCompact)
                            MaterialTheme.typography.bodyMedium
                        else
                            MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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
                        .padding(AdaptiveSpacing.small)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = AdaptiveSpacing.small,
                            vertical = AdaptiveSpacing.extraSmall
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(badgeIcon, contentDescription = null, tint = badgeContentColor)
                        Spacer(Modifier.width(UiConstants.HALF_SMALL_PADDING))
                        Text(badgeText, style = MaterialTheme.typography.labelMedium, color = badgeContentColor)
                    }
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
            ActivityType.entries.forEach { type ->
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
    val isLandscape = isLandscape()

    Card(
        modifier = modifier.padding(
            bottom = if (isLandscape) 6.dp else 10.dp,
            end = if (isLandscape) 6.dp else 10.dp
        ),
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(if (isLandscape) 16.dp else 24.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (isLandscape) 8.dp else UiConstants.MEDIUM_PADDING,
                vertical = if (isLandscape) 6.dp else 10.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else UiConstants.SMALL_PADDING)
        ) {
            if (!isRunning) {
                if (!isLandscape) {
                    Text(
                        text = stringResource(R.string.start),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = fg
                    )
                }
                ActivitySelectorDropdown(
                    selectedActivity = selectedActivity,
                    onActivitySelected = onActivitySelected,
                    borderColor = fg
                )
                FilledIconButton(
                    onClick = onStartRun,
                    enabled = gpsReady,
                    modifier = if (isLandscape) Modifier.size(36.dp) else Modifier
                ) {
                    if (gpsReady) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Start",
                            modifier = if (isLandscape) Modifier.size(20.dp) else Modifier
                        )
                    } else {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = fg,
                            modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = selectedActivity.label(),
                    style = if (isLandscape) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                    color = fg
                )
                FilledIconButton(
                    onClick = onStopRun,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = if (isLandscape) Modifier.size(36.dp) else Modifier
                ) {
                    Icon(
                        Icons.Filled.Stop,
                        contentDescription = "Stop",
                        modifier = if (isLandscape) Modifier.size(20.dp) else Modifier
                    )
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
    ActivityType.RUNNING -> Icons.AutoMirrored.Outlined.DirectionsRun
    ActivityType.CYCLING -> Icons.AutoMirrored.Outlined.DirectionsBike
    ActivityType.WALKING -> Icons.AutoMirrored.Outlined.DirectionsWalk
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
        title = { Text(stringResource(R.string.run_complete_title)) },
        text = {
            Column {
                Text(stringResource(R.string.great_job_summary))
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.distance_value, FitnessUtils.formatDistance(distance)))
                Text(stringResource(R.string.time_value, FitnessUtils.formatTime(elapsedTime)))
                Text(stringResource(R.string.pace_value, FitnessUtils.formatPace(pace)))
            }
        },
        confirmButton = {
            TextButton(onClick = onViewDetails) {
                Text(stringResource(R.string.view_details))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
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
