package se.umu.calu0217.strive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import se.umu.calu0217.strive.core.utils.PermissionUtils
import se.umu.calu0217.strive.core.utils.PreferencesUtils
import se.umu.calu0217.strive.core.location.LocationTracker
import se.umu.calu0217.strive.ui.theme.StriveTheme
import se.umu.calu0217.strive.ui.StriveApp
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var locationTracker: LocationTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        // Register a permission launcher for startup permissions
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                // Warm up GPS once permissions are granted
                lifecycleScope.launch {
                    try {
                        locationTracker.getLocationUpdates().first()
                    } catch (_: Exception) {
                    } finally {
                        locationTracker.stopLocationUpdates()
                    }
                }
            }
            // If denied, proceed normally; UI will handle further prompts
        }

        // Auto-start a short GPS warm-up, requesting permission if needed
        if (PreferencesUtils.isAutoStartGpsEnabled(this)) {
            if (PermissionUtils.hasLocationPermissions(this)) {
                lifecycleScope.launch {
                    try {
                        // Start updates and take the first location to warm up the provider
                        locationTracker.getLocationUpdates().first()
                    } catch (_: Exception) {
                        // Ignore errors here; Run screen will handle UI/permissions
                    } finally {
                        locationTracker.stopLocationUpdates()
                    }
                }
            } else {
                // Ask for location permission on first launch if user opted-in to auto-start GPS
                permissionLauncher.launch(PermissionUtils.LOCATION_PERMISSIONS)
            }
        }

        setContent {
            StriveTheme {
                StriveApp()
            }
        }
    }
}