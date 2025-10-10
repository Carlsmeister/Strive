package se.umu.calu0217.strive.core.constants

import androidx.compose.ui.unit.dp

/**
 * Centralized UI constants to avoid magic numbers throughout the app.
 */
object UiConstants {
    val STANDARD_PADDING = 16.dp
    val LARGE_PADDING = 24.dp
    val MEDIUM_PADDING = 12.dp
    val SMALL_PADDING = 8.dp
    val HALF_SMALL_PADDING = 6.dp
    val EXTRA_SMALL_PADDING = 4.dp

    const val MAX_SEARCH_RESULTS = 100
    const val MAX_INITIAL_RESULTS = 300
    val DIALOG_LIST_MAX_HEIGHT = 200.dp

    val SMALL_ICON_SIZE = 24.dp
    val MEDIUM_ICON_SIZE = 32.dp
    val LARGE_ICON_SIZE = 48.dp

    const val MAX_SETS_DIGITS = 2
    const val MAX_REPS_DIGITS = 3
    const val MAX_REST_DIGITS = 4

    const val MIN_SETS = 1
    const val MAX_SETS = 99
    const val MIN_REPS = 1
    const val MAX_REPS = 999
    const val MIN_REST_SEC = 0
    const val MAX_REST_SEC = 9999

    const val MIN_WEIGHT = 1.0
    const val MAX_WEIGHT = 500.0
    const val MIN_HEIGHT = 50.0
    const val MAX_HEIGHT = 300.0
}
