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

    fun stopLocationUpdates() {
        activeCallback?.let { cb ->
            fusedLocationClient.removeLocationUpdates(cb)
            activeCallback = null
        }
    }
}
