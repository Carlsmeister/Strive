package se.umu.calu0217.strive.core.validation

import se.umu.calu0217.strive.core.constants.UiConstants

/**
 * Input validation utilities for user inputs.
 * Returns error message string if invalid, null if valid.
 */
object InputValidator {
    
    /**
     * Validates weight input.
     * @return Error message if invalid, null if valid
     */
    fun validateWeight(input: String): String? {
        if (input.isBlank()) return "Weight cannot be empty"

        val weight = input.toDoubleOrNull()
        return when {
            weight == null -> "Please enter a valid number"
            weight <= UiConstants.MIN_WEIGHT -> "Weight must be positive"
            weight > UiConstants.MAX_WEIGHT -> "Please enter a realistic weight"
            else -> null
        }
    }

    /**
     * Validates height input.
     * @return Error message if invalid, null if valid
     */
    fun validateHeight(input: String): String? {
        if (input.isBlank()) return "Height cannot be empty"

        val height = input.toDoubleOrNull()
        return when {
            height == null -> "Please enter a valid number"
            height < UiConstants.MIN_HEIGHT -> "Height is too low"
            height > UiConstants.MAX_HEIGHT -> "Please enter a realistic height"
            else -> null
        }
    }

    /**
     * Validates sets input.
     * @return Error message if invalid, null if valid
     */
    fun validateSets(input: String): String? {
        val sets = input.toIntOrNull()
        return when {
            sets == null && input.isNotBlank() -> "Please enter a valid number"
            sets != null && sets < UiConstants.MIN_SETS -> "Sets must be at least ${UiConstants.MIN_SETS}"
            sets != null && sets > UiConstants.MAX_SETS -> "Sets cannot exceed ${UiConstants.MAX_SETS}"
            else -> null
        }
    }
    
    /**
     * Validates reps input.
     * @return Error message if invalid, null if valid
     */
    fun validateReps(input: String): String? {
        val reps = input.toIntOrNull()
        return when {
            reps == null && input.isNotBlank() -> "Please enter a valid number"
            reps != null && reps < UiConstants.MIN_REPS -> "Reps must be at least ${UiConstants.MIN_REPS}"
            reps != null && reps > UiConstants.MAX_REPS -> "Reps cannot exceed ${UiConstants.MAX_REPS}"
            else -> null
        }
    }
    
    /**
     * Validates rest time input.
     * @return Error message if invalid, null if valid
     */
    fun validateRestTime(input: String): String? {
        val rest = input.toIntOrNull()
        return when {
            rest == null && input.isNotBlank() -> "Please enter a valid number"
            rest != null && rest < UiConstants.MIN_REST_SEC -> "Rest time cannot be negative"
            rest != null && rest > UiConstants.MAX_REST_SEC -> "Rest time is too long"
            else -> null
        }
    }
    
    /**
     * Validates template name.
     * @return Error message if invalid, null if valid
     */
    fun validateTemplateName(input: String): String? {
        val trimmed = input.trim()
        return when {
            trimmed.isEmpty() -> "Template name cannot be empty"
            trimmed.length < 2 -> "Template name is too short"
            trimmed.length > 50 -> "Template name is too long"
            else -> null
        }
    }
}