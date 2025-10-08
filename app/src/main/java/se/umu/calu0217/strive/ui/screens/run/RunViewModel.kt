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

/**
 * Manages running/cycling/walking activity tracking with GPS.
 * Handles location tracking, distance calculation, pace monitoring, and calorie estimation.
 * @param locationTracker Service for GPS location updates.
 * @param startRunUseCase Use case for starting a new run session.
 * @param getActiveRunSessionUseCase Use case for retrieving active run session.
 * @param addRunPointUseCase Use case for adding GPS points to a run.
 * @param updateRunSessionUseCase Use case for updating run session data.
 * @param finishRunUseCase Use case for completing a run session.
 * @author Carl Lundholm
 */
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

    /**
     * Starts a timer that updates elapsed time and pace every second.
     * @author Carl Lundholm
     */
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

    /**
     * Creates a flow that emits at regular intervals.
     * @param periodMs Period between emissions in milliseconds.
     * @author Carl Lundholm
     */
    private fun tickerFlow(periodMs: Long) = flow {
        while (kotlinx.coroutines.currentCoroutineContext().isActive) {
            emit(Unit)
            delay(periodMs)
        }
    }

    /**
     * Stops the active timer.
     * @author Carl Lundholm
     */
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    /**
     * Checks for an active run session and resumes it if found.
     * @author Carl Lundholm
     */
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

    /**
     * Starts a new run/cycling/walking session.
     * Initializes tracking, starts timer, and begins GPS location updates.
     * @author Carl Lundholm
     */
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

    /**
     * Stops the active run session and calculates final statistics.
     * @param userWeight User's weight in kg for calorie calculation (default 70kg).
     * @author Carl Lundholm
     */
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

    /**
     * Calculates MET value based on activity type and pace.
     * @param activity Type of activity (running, walking, or cycling).
     * @param paceMinPerKm Current pace in minutes per kilometer.
     * @return MET (Metabolic Equivalent of Task) value for calorie calculation.
     * @author Carl Lundholm
     */
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

    /**
     * Starts GPS location tracking and updates distance/pace in real-time.
     * @author Carl Lundholm
     */
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

    /**
     * Sets the activity type (running, cycling, or walking).
     * @param activity The activity type to set.
     * @author Carl Lundholm
     */
    fun setActivity(activity: ActivityType) {
        _uiState.value = _uiState.value.copy(selectedActivity = activity)
    }

    /**
     * Dismisses the run summary dialog.
     * @author Carl Lundholm
     */
    fun dismissSummary() {
        _uiState.value = _uiState.value.copy(showSummary = false)
    }

    /**
     * Clears the current error message from the UI state.
     * @author Carl Lundholm
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Shows an error message when location permissions are denied.
     * @author Carl Lundholm
     */
    fun showPermissionError() {
        _uiState.value = _uiState.value.copy(
            error = "Location permissions are required to track your run. Please grant permissions in Settings.",
            gpsStatus = GpsStatus.ERROR
        )
    }

    /**
     * Sets GPS status to ready.
     * @author Carl Lundholm
     */
    fun setGpsReady() {
        _uiState.value = _uiState.value.copy(gpsStatus = GpsStatus.READY)
    }

    /**
     * Sets GPS status to searching.
     * @author Carl Lundholm
     */
    fun setGpsNotReady() {
        _uiState.value = _uiState.value.copy(gpsStatus = GpsStatus.SEARCHING)
    }

    /**
     * Refreshes the current GPS location.
     * @author Carl Lundholm
     */
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

/**
 * UI state for run/cycling/walking tracking.
 * @property isRunning Whether a session is currently active.
 * @property distance Distance covered in meters.
 * @property elapsedTime Time elapsed in seconds.
 * @property pace Current pace in minutes per kilometer.
 * @property selectedActivity Type of activity being tracked.
 * @property gpsStatus Current GPS signal status.
 * @property showSummary Whether to show the session summary.
 * @property error Error message to display, if any.
 * @property currentLatitude Current GPS latitude.
 * @property currentLongitude Current GPS longitude.
 * @author Carl Lundholm
 */
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

/**
 * Types of activities that can be tracked.
 * @author Carl Lundholm
 */
enum class ActivityType { RUNNING, CYCLING, WALKING }
