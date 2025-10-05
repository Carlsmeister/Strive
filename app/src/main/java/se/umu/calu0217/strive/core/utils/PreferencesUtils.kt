package se.umu.calu0217.strive.core.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE

object PreferencesUtils {
    private const val PREFS_FILE = "strive_prefs"
    private const val KEY_AUTO_START_GPS = "auto_start_gps"

    fun isAutoStartGpsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_FILE, MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_START_GPS, true)
    }

    fun setAutoStartGpsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_FILE, MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_START_GPS, enabled).apply()
    }
}