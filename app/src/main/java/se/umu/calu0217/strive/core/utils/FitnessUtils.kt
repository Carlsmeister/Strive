package se.umu.calu0217.strive.core.utils

import kotlin.math.*

/**
 * Utility functions for distance calculations and fitness metrics
 */
object FitnessUtils {

    /**
     * Calculate distance between two GPS coordinates using Haversine formula
     * @param lat1 Latitude of first point in degrees
     * @param lng1 Longitude of first point in degrees
     * @param lat2 Latitude of second point in degrees
     * @param lng2 Longitude of second point in degrees
     * @return Distance in meters
     */
    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371000.0 // Earth radius in meters

        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    /**
     * Calculate pace in minutes per kilometer
     * @param distanceMeters Distance in meters
     * @param timeSeconds Time in seconds
     * @return Pace in minutes per kilometer
     */
    fun calculatePace(distanceMeters: Double, timeSeconds: Int): Double {
        if (distanceMeters <= 0 || timeSeconds <= 0) return 0.0

        val distanceKm = distanceMeters / 1000.0
        val timeMinutes = timeSeconds / 60.0

        return timeMinutes / distanceKm
    }

    /**
     * Calculate calories burned during running
     * @param metValue MET value for the activity intensity
     * @param weightKg User weight in kilograms
     * @param timeHours Activity duration in hours
     * @return Estimated calories burned
     */
    fun calculateCalories(metValue: Double, weightKg: Double, timeHours: Double): Int {
        return (metValue * weightKg * timeHours).roundToInt()
    }

    /**
     * Get MET value based on pace (min/km)
     */
    fun getMetFromPace(paceMinPerKm: Double): Double {
        return when {
            paceMinPerKm <= 4.0 -> 12.0  // Very fast (< 4 min/km)
            paceMinPerKm <= 5.0 -> 10.0  // Fast (4-5 min/km)
            paceMinPerKm <= 6.0 -> 8.5   // Moderate (5-6 min/km)
            paceMinPerKm <= 7.0 -> 7.0   // Light jog (6-7 min/km)
            else -> 5.5                  // Walking/slow jog (> 7 min/km)
        }
    }

    /**
     * Format pace for display (MM:SS per km)
     */
    fun formatPace(paceMinPerKm: Double): String {
        if (paceMinPerKm <= 0) return "--:--"

        val minutes = paceMinPerKm.toInt()
        val seconds = ((paceMinPerKm - minutes) * 60).roundToInt()

        return "${minutes}:${seconds.toString().padStart(2, '0')}"
    }

    /**
     * Format distance for display
     */
    fun formatDistance(meters: Double): String {
        return when {
            meters < 1000 -> "${meters.roundToInt()}m"
            else -> "${(meters / 1000).format(2)}km"
        }
    }

    /**
     * Format time duration for display
     */
    fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return if (hours > 0) {
            "${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
        } else {
            "${minutes}:${secs.toString().padStart(2, '0')}"
        }
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)
}
