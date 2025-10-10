package se.umu.calu0217.strive.ui.screens.run

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.umu.calu0217.strive.R
import se.umu.calu0217.strive.core.utils.PermissionUtils
import android.content.Context
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import se.umu.calu0217.strive.core.constants.UiConstants
import se.umu.calu0217.strive.core.utils.isLandscape
import se.umu.calu0217.strive.ui.components.ConfirmationDialog

/**
 * Screen for GPS-tracked running/cycling/walking activities.
 * Displays a map with the current route, real-time stats, and activity controls.
 *
 * @param viewModel The view model managing run session and GPS tracking (injected via Hilt).
 */
@Composable
fun RunScreen(
    viewModel: RunViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.startRun()
        } else {
            viewModel.showPermissionError()
        }
    }

    PermissionStatusEffect(viewModel = viewModel, context = context)
    InitialLocationEffect(viewModel = viewModel)

    val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState()
    var showStopRunConfirmDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = uiState.isRunning) {
        showStopRunConfirmDialog = true
    }
    var hasCentered by rememberSaveable { mutableStateOf(false) }

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

        val isLandscape = isLandscape()

        if (uiState.isRunning) {
            RunningStatsPanel(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(UiConstants.STANDARD_PADDING)
                    .then(
                        if (isLandscape) {
                            Modifier.wrapContentSize()
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    ),
                distance = uiState.distance,
                elapsedTime = uiState.elapsedTime,
                pace = uiState.pace
            )
        } else {
            ReadyToRunPanel(
                modifier = Modifier
                    .align(if (isLandscape) Alignment.TopStart else Alignment.TopCenter)
                    .padding(UiConstants.STANDARD_PADDING)
                    .then(
                        if (isLandscape) {
                            Modifier.fillMaxWidth(0.3f) // Take up half the width in landscape
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    ),
                gpsStatus = uiState.gpsStatus
            )
        }

        FloatingRunControls(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = UiConstants.STANDARD_PADDING, vertical = UiConstants.LARGE_PADDING),
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }

    if (uiState.showSummary) {
        RunSummaryDialog(
            distance = uiState.distance,
            elapsedTime = uiState.elapsedTime,
            pace = uiState.pace,
            onDismiss = viewModel::dismissSummary,
            onViewDetails = {}
        )
    }

    if (showStopRunConfirmDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.stop_run_title),
            message = stringResource(R.string.stop_run_message),
            confirmText = stringResource(R.string.stop),
            dismissText = stringResource(R.string.cancel),
            onDismiss = { showStopRunConfirmDialog = false },
            onConfirm = {
                showStopRunConfirmDialog = false
                viewModel.stopRun()
            }
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

enum class GpsStatus {
    SEARCHING,
    READY,
    ERROR
}
