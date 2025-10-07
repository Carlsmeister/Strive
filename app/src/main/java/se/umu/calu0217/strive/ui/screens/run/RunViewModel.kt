package se.umu.calu0217.strive.ui.screens.run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
    private var timerJob: Job? = null

    init {
        checkForActiveRun()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            tickerFlow(1000L).collect {
                if (activeRunId != null) {
                    val elapsedSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                    val pace = FitnessUtils.calculatePace(totalDistance, elapsedSeconds)
                    _uiState.value = _uiState.value.copy(
                        elapsedTime = elapsedSeconds,
                        pace = pace
                    )
                }
            }
        }
    }

    private fun tickerFlow(periodMs: Long) = flow {
        while (kotlinx.coroutines.currentCoroutineContext().isActive) {
            emit(Unit)
            delay(periodMs)
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
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
                startTimer()
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

                startTimer()
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

                // Calculate calories based on selected activity, pace and duration
                val timeHours = currentState.elapsedTime / 3600.0
                val metValue = getMetForActivity(currentState.selectedActivity, currentState.pace)
                val calories = FitnessUtils.calculateCalories(metValue, userWeight, timeHours)

                finishRunUseCase(runId, calories)

                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    showSummary = true
                )

                stopTimer()
                locationTracker.stopLocationUpdates()
                activeRunId = null
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error stopping run"
                )
            }
        }
    }

    private fun getMetForActivity(activity: ActivityType, paceMinPerKm: Double): Double {
        // Convert pace (min/km) to speed (km/h)
        val speedKmh = if (paceMinPerKm > 0) 60.0 / paceMinPerKm else 0.0
        return when (activity) {
            ActivityType.RUNNING -> FitnessUtils.getMetFromPace(paceMinPerKm)
            ActivityType.WALKING -> when {
                speedKmh < 4.0 -> 2.8
                speedKmh < 5.5 -> 3.5
                speedKmh < 6.5 -> 4.3
                else -> 5.0
            }
            ActivityType.CYCLING -> when {
                speedKmh < 16.0 -> 4.0
                speedKmh < 19.0 -> 6.0
                speedKmh < 22.0 -> 8.0
                speedKmh < 25.0 -> 10.0
                else -> 12.0
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
                    _uiState.value = _uiState.value.copy(
                        gpsStatus = GpsStatus.READY,
                        currentLatitude = location.latitude,
                        currentLongitude = location.longitude
                    )

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

    fun setActivity(activity: ActivityType) {
        _uiState.value = _uiState.value.copy(selectedActivity = activity)
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

    fun refreshCurrentLocation() {
        viewModelScope.launch {
            try {
                val loc = locationTracker.getCurrentLocation()
                if (loc != null) {
                    _uiState.value = _uiState.value.copy(
                        currentLatitude = loc.latitude,
                        currentLongitude = loc.longitude,
                        gpsStatus = GpsStatus.READY
                    )
                }
            } catch (_: Exception) {
                // ignore, leave state as is
            }
        }
    }
}

// Data class for RunViewModel state
data class RunUiState(
    val isRunning: Boolean = false,
    val distance: Double = 0.0, // in meters
    val elapsedTime: Int = 0, // in seconds
    val pace: Double = 0.0, // min/km
    val selectedActivity: ActivityType = ActivityType.RUNNING,
    val gpsStatus: GpsStatus = GpsStatus.SEARCHING,
    val showSummary: Boolean = false,
    val error: String? = null,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null
)

enum class ActivityType { RUNNING, CYCLING, WALKING }
