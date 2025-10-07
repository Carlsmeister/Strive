package se.umu.calu0217.strive.ui.screens.run

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.umu.calu0217.strive.core.utils.PermissionUtils
import android.content.Context

@Composable
fun RunScreen(
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

    // Effects extracted for clarity
    PermissionStatusEffect(viewModel = viewModel, context = context)
    InitialLocationEffect(viewModel = viewModel)

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
        RunMapContainer(
            modifier = Modifier.fillMaxSize(),
            context = context,
            cameraPositionState = cameraPositionState,
            hasLocationPermission = PermissionUtils.hasLocationPermissions(context),
            currentLat = uiState.currentLatitude,
            currentLng = uiState.currentLongitude
        )

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
            onViewDetails = {}
        )
    }
}











@Composable
private fun RunMapContainer(
    modifier: Modifier = Modifier,
    context: Context,
    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
    hasLocationPermission: Boolean,
    currentLat: Double?,
    currentLng: Double?
) {
    // Only render the map if a valid Google Maps API key is configured
    val mapsConfigured = remember { isMapsApiKeyConfigured(context) }
    if (mapsConfigured) {
        MapContent(
            modifier = modifier,
            cameraPositionState = cameraPositionState,
            hasLocationPermission = hasLocationPermission,
            currentLat = currentLat,
            currentLng = currentLng
        )
    } else {
        MapsApiKeyMissingNotice()
    }
}

// Small effect helpers to keep RunScreen readable
@Composable
private fun PermissionStatusEffect(viewModel: RunViewModel, context: Context) {
    LaunchedEffect(Unit) {
        val hasPermissions = PermissionUtils.hasLocationPermissions(context)
        if (hasPermissions) {
            viewModel.setGpsReady()
        } else {
            viewModel.setGpsNotReady()
        }
    }
}

@Composable
private fun InitialLocationEffect(viewModel: RunViewModel) {
    LaunchedEffect(Unit) {
        viewModel.refreshCurrentLocation()
    }
}

// Add enum definitions at the end of the file
enum class GpsStatus {
    SEARCHING,
    READY,
    ERROR
}
