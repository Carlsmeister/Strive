package se.umu.calu0217.strive.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Small date/time formatting helpers consolidated from UI components.
 */
object DateTimeUtils {
    /**
     * Formats a timestamp to time-of-day like "HH:mm".
     */
    fun formatTimeOfDay(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(date)
    }

    /**
     * Formats a timestamp to a short date-time like "MMM dd, HH:mm".
     */
    fun formatShortDateTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
}
