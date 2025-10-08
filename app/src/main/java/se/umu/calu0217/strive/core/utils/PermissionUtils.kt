package se.umu.calu0217.strive.core.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Utility for checking Android runtime permissions.
 * Provides helper functions for location permission verification.
 * @author Carl Lundholm
 */
object PermissionUtils {

    /**
     * Checks if both fine and coarse location permissions are granted.
     * Required for GPS tracking functionality in the app.
     * @param context Application or activity context.
     * @return True if both ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION are granted, false otherwise.
     * @author Carl Lundholm
     */
    fun hasLocationPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Array of location permissions required for GPS tracking.
     * Includes ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION.
     * Used with ActivityResultContracts.RequestMultiplePermissions.
     * @author Carl Lundholm
     */
    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
}
