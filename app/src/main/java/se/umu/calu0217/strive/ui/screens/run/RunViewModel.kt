package se.umu.calu0217.strive.ui.screens.run

import android.Manifest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import se.umu.calu0217.strive.core.location.LocationTracker
import se.umu.calu0217.strive.core.utils.FitnessUtils
import se.umu.calu0217.strive.domain.usecases.*
import javax.inject.Inject

@HiltViewModel
class RunViewModel @Inject constructor(
    private val locationTracker: LocationTracker,
    private val startRunUseCase: StartRunUseCase,
    private val getActiveRunSessionUseCase: GetActiveRunSessionUseCase,
    private val addRunPointUseCase: AddRunPointUseCase,
    private val updateRunSessionUseCase: UpdateRunSessionUseCase,
    private val finishRunUseCase: FinishRunUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RunUiState())
    val uiState: StateFlow<RunUiState> = _uiState.asStateFlow()

    private var activeRunId: Long? = null
    private var startTime: Long = 0
    private var lastLocation: Pair<Double, Double>? = null
    private var totalDistance: Double = 0.0

    init {
        checkForActiveRun()
    }

    private fun checkForActiveRun() {
        viewModelScope.launch {
            val activeSession = getActiveRunSessionUseCase()
            if (activeSession != null) {
                activeRunId = activeSession.id
                startTime = activeSession.startedAt
                totalDistance = activeSession.distance
                _uiState.value = _uiState.value.copy(
                    isRunning = true,
                    distance = totalDistance,
                    elapsedTime = ((System.currentTimeMillis() - startTime) / 1000).toInt(),
                    pace = activeSession.pace
                )
                startLocationTracking()
            }
        }
    }

    fun startRun() {
        viewModelScope.launch {
            try {
                val runId = startRunUseCase()
                activeRunId = runId
                startTime = System.currentTimeMillis()
                totalDistance = 0.0
                lastLocation = null

                _uiState.value = _uiState.value.copy(
                    isRunning = true,
                    distance = 0.0,
                    elapsedTime = 0,
                    pace = 0.0,
                    error = null
                )

                startLocationTracking()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error starting run"
                )
            }
        }
    }

    fun stopRun(userWeight: Double = 70.0) {
        viewModelScope.launch {
            try {
                val runId = activeRunId ?: return@launch
                val currentState = _uiState.value

                // Calculate calories based on pace and duration
                val timeHours = currentState.elapsedTime / 3600.0
                val metValue = FitnessUtils.getMetFromPace(currentState.pace)
                val calories = FitnessUtils.calculateCalories(metValue, userWeight, timeHours)

                finishRunUseCase(runId, calories)

                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    showSummary = true
                )

                locationTracker.stopLocationUpdates()
                activeRunId = null
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error stopping run"
                )
            }
        }
    }

    private fun startLocationTracking() {
        viewModelScope.launch {
            locationTracker.getLocationUpdates()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        error = "GPS error: ${e.message}",
                        gpsStatus = GpsStatus.ERROR
                    )
                }
                .collect { location ->
                    _uiState.value = _uiState.value.copy(gpsStatus = GpsStatus.READY)

                    activeRunId?.let { runId ->
                        // Add run point
                        addRunPointUseCase(runId, location.latitude, location.longitude)

                        // Calculate distance if we have a previous location
                        lastLocation?.let { (prevLat, prevLng) ->
                            val segmentDistance = FitnessUtils.calculateDistance(
                                prevLat, prevLng, location.latitude, location.longitude
                            )
                            totalDistance += segmentDistance
                        }

                        lastLocation = location.latitude to location.longitude

                        // Update elapsed time
                        val elapsedSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()

                        // Calculate pace
                        val pace = FitnessUtils.calculatePace(totalDistance, elapsedSeconds)

                        // Update UI state
                        _uiState.value = _uiState.value.copy(
                            distance = totalDistance,
                            elapsedTime = elapsedSeconds,
                            pace = pace
                        )

                        // Update database
                        updateRunSessionUseCase(runId, totalDistance, elapsedSeconds, pace)
                    }
                }
        }
    }

    fun setIntensity(intensity: RunIntensity) {
        _uiState.value = _uiState.value.copy(selectedIntensity = intensity)
    }

    fun dismissSummary() {
        _uiState.value = _uiState.value.copy(showSummary = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun showPermissionError() {
        _uiState.value = _uiState.value.copy(
            error = "Location permissions are required to track your run. Please grant permissions in Settings.",
            gpsStatus = GpsStatus.ERROR
        )
    }

    fun setGpsReady() {
        _uiState.value = _uiState.value.copy(gpsStatus = GpsStatus.READY)
    }

    fun setGpsNotReady() {
        _uiState.value = _uiState.value.copy(gpsStatus = GpsStatus.SEARCHING)
    }
}

// Data class for RunViewModel state
data class RunUiState(
    val isRunning: Boolean = false,
    val distance: Double = 0.0, // in meters
    val elapsedTime: Int = 0, // in seconds
    val pace: Double = 0.0, // min/km
    val selectedIntensity: RunIntensity = RunIntensity.MEDIUM,
    val gpsStatus: GpsStatus = GpsStatus.SEARCHING,
    val showSummary: Boolean = false,
    val error: String? = null
)
