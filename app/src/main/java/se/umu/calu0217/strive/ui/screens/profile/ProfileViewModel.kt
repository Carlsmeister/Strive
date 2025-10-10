package se.umu.calu0217.strive.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import se.umu.calu0217.strive.domain.repository.WorkoutRepository
import se.umu.calu0217.strive.domain.repository.RunRepository
import javax.inject.Inject

/**
 * ViewModel for the profile screen.
 * Handles data deletion operations.
 * @param workoutRepository Repository for workout data operations.
 * @param runRepository Repository for run data operations.
 * @author Carl Lundholm
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val runRepository: RunRepository
) : ViewModel() {

    /**
     * Deletes all user workout and run data from the database.
     * @author Carl Lundholm
     */
    fun deleteAllData() {
        viewModelScope.launch {
            runRepository.deleteAllRunData()
            workoutRepository.deleteAllWorkoutData()
        }
    }
}

