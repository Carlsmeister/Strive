package se.umu.calu0217.strive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import se.umu.calu0217.strive.ui.theme.StriveTheme
import se.umu.calu0217.strive.ui.StriveApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StriveTheme {
                StriveApp()
            }
        }
    }
}