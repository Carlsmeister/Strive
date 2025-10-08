package se.umu.calu0217.strive.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import se.umu.calu0217.strive.domain.models.RunSession
import se.umu.calu0217.strive.domain.models.WorkoutSession
import se.umu.calu0217.strive.domain.usecases.GetAllRunSessionsUseCase
import se.umu.calu0217.strive.domain.repository.WorkoutRepository
import javax.inject.Inject

/**
 * Manages workout and run history with weekly statistics.
 * Displays completed sessions and calculates weekly performance metrics.
 * @param getAllRunSessionsUseCase Use case for retrieving all run sessions.
 * @param workoutRepository Repository for workout data operations.
 * @author Carl Lundholm
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getAllRunSessionsUseCase: GetAllRunSessionsUseCase,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(HistoryTab.WORKOUTS)
    val selectedTab: StateFlow<HistoryTab> = _selectedTab.asStateFlow()

    val workoutSessions = workoutRepository.getAllWorkoutSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val runSessions = getAllRunSessionsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Calculate weekly statistics
    val weeklyStats = combine(workoutSessions, runSessions) { workouts, runs ->
        val currentWeekStart = getCurrentWeekStart()

        val weeklyWorkouts = workouts.filter {
            it.startedAt >= currentWeekStart && it.endedAt != null
        }
        val weeklyRuns = runs.filter {
            it.startedAt >= currentWeekStart && it.endedAt != null
        }

        WeeklyStats(
            totalWorkouts = weeklyWorkouts.size,
            totalRuns = weeklyRuns.size,
            totalWorkoutMinutes = weeklyWorkouts.sumOf {
                ((it.endedAt ?: it.startedAt) - it.startedAt) / 60000
            }.toInt(),
            totalRunMinutes = weeklyRuns.sumOf { it.elapsedSec / 60 },
            totalDistance = weeklyRuns.sumOf { it.distance / 1000.0 }, // Convert to km
            totalCalories = weeklyWorkouts.sumOf { it.kcal } + weeklyRuns.sumOf { it.kcal }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WeeklyStats()
    )

    /**
     * Selects which tab to display (workouts or runs).
     * @param tab The tab to select.
     * @author Carl Lundholm
     */
    fun selectTab(tab: HistoryTab) {
        _selectedTab.value = tab
    }

    /**
     * Gets the start of the current week (Monday at 00:00:00).
     * @return Timestamp in milliseconds.
     * @author Carl Lundholm
     */
    private fun getCurrentWeekStart(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.firstDayOfWeek = java.util.Calendar.MONDAY
        calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

/**
 * History tabs for switching between workouts and runs.
 * @author Carl Lundholm
 */
enum class HistoryTab {
    WORKOUTS, RUNS
}

/**
 * Weekly statistics for workouts and runs.
 * @property totalWorkouts Number of completed workouts this week.
 * @property totalRuns Number of completed runs this week.
 * @property totalWorkoutMinutes Total workout minutes this week.
 * @property totalRunMinutes Total run minutes this week.
 * @property totalDistance Total distance in kilometers this week.
 * @property totalCalories Total calories burned this week.
 * @author Carl Lundholm
 */
data class WeeklyStats(
    val totalWorkouts: Int = 0,
    val totalRuns: Int = 0,
    val totalWorkoutMinutes: Int = 0,
    val totalRunMinutes: Int = 0,
    val totalDistance: Double = 0.0, // in km
    val totalCalories: Int = 0
)
