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

/**
 * Main activity for the Strive fitness app.
 * Handles GPS initialization, location permissions, and edge-to-edge UI setup.
 * @author Carl Lundholm
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var locationTracker: LocationTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                lifecycleScope.launch {
                    try {
                        locationTracker.getLocationUpdates().first()
                    } catch (_: Exception) {
                    } finally {
                        locationTracker.stopLocationUpdates()
                    }
                }
            }
        }

        if (PreferencesUtils.isAutoStartGpsEnabled(this)) {
            if (PermissionUtils.hasLocationPermissions(this)) {
                lifecycleScope.launch {
                    try {
                        locationTracker.getLocationUpdates().first()
                    } catch (_: Exception) {
                    } finally {
                        locationTracker.stopLocationUpdates()
                    }
                }
            } else {
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