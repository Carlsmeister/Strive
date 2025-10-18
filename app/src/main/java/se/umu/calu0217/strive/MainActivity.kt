package se.umu.calu0217.strive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import se.umu.calu0217.strive.core.location.LocationTracker
import se.umu.calu0217.strive.ui.theme.StriveTheme
import se.umu.calu0217.strive.ui.StriveApp
import javax.inject.Inject

/**
 * Main activity for the Strive fitness app.
 * Handles edge-to-edge UI setup.
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


        setContent {
            StriveTheme {
                StriveApp()
            }
        }
    }
}