package se.umu.calu0217.strive.core.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE

/**
 * Utility for managing app preferences using SharedPreferences.
 * Handles persistent settings for the Strive fitness app.
 * @author Carl Lundholm
 */
object PreferencesUtils {
    private const val PREFS_FILE = "strive_prefs"
    private const val KEY_AUTO_START_GPS = "auto_start_gps"

    /**
     * Checks if GPS auto-start on app launch is enabled.
     * @param context Application context for accessing SharedPreferences.
     * @return True if GPS should auto-start, false otherwise (default: true).
     * @author Carl Lundholm
     */
    fun isAutoStartGpsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_FILE, MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_START_GPS, true)
    }

    /**
     * Sets whether GPS should auto-start on app launch.
     * @param context Application context for accessing SharedPreferences.
     * @param enabled True to enable GPS auto-start, false to disable.
     * @author Carl Lundholm
     */
    fun setAutoStartGpsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_FILE, MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_START_GPS, enabled).apply()
    }
}