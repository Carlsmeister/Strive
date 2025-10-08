package se.umu.calu0217.strive

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for the Strive fitness app.
 * Enables Hilt dependency injection throughout the application.
 * @author Carl Lundholm
 */
@HiltAndroidApp
class StriveApplication : Application()
