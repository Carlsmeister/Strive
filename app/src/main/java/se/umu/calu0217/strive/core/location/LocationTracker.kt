package se.umu.calu0217.strive.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton service for GPS location tracking using Google Play Services.
 * Provides real-time location updates via Flow and one-time location retrieval.
 * Manages location callbacks and ensures only one active subscription at a time.
 * @param context Application context for location services.
 * @author Carl Lundholm
 */
@Singleton
class LocationTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        2000L // 2 seconds interval
    ).apply {
        setMinUpdateDistanceMeters(5f) // 5 meters minimum distance
        setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
        setWaitForAccurateLocation(false)
    }.build()

    // Keep reference to remove later
    @Volatile
    private var activeCallback: LocationCallback? = null

    /**
     * Provides a continuous flow of location updates.
     * Updates every 2 seconds or when device moves 5+ meters.
     * Automatically stops previous subscriptions to ensure single active listener.
     * @return Flow of Location objects with GPS coordinates.
     * @throws SecurityException if location permissions are missing or revoked.
     * @author Carl Lundholm
     */
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    trySend(location)
                }
            }
        }
        activeCallback = callback
        try {
            // Enforce a single active subscription
            stopLocationUpdates()
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Permissions missing or revoked
            close(e)
            return@callbackFlow
        } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
            if (activeCallback === callback) activeCallback = null
        }
    }

    /**
     * Gets the device's current location as a one-time request.
     * Uses high accuracy priority for best GPS precision.
     * @return Current Location if available, null if unavailable or on error.
     * @author Carl Lundholm
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                try {
                    fusedLocationClient
                        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { loc ->
                            if (cont.isActive) cont.resume(loc, onCancellation = {})
                        }
                        .addOnFailureListener { _ ->
                            if (cont.isActive) cont.resume(null, onCancellation = {})
                        }
                } catch (e: Exception) {
                    if (cont.isActive) cont.resume(null, onCancellation = {})
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Stops all active location updates and clears callbacks.
     * Called automatically when location flow is closed or manually to stop tracking.
     * @author Carl Lundholm
     */
    fun stopLocationUpdates() {
        activeCallback?.let { cb ->
            fusedLocationClient.removeLocationUpdates(cb)
            activeCallback = null
        }
    }
}
