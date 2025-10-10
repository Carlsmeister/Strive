package se.umu.calu0217.strive.core.utils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Utilities for responsive design across different screen sizes and orientations.
 */

/**
 * Screen orientation states
 */
enum class ScreenOrientation {
    PORTRAIT,
    LANDSCAPE
}

/**
 * Screen size categories based on smallest width
 */
enum class ScreenSize {
    SMALL,      // < 360dp (compact phones)
    NORMAL,     // 360-600dp (standard phones)
    LARGE,      // 600-840dp (large phones, small tablets)
    XLARGE      // > 840dp (tablets)
}

/**
 * Composable to get current screen orientation
 */
@Composable
fun rememberScreenOrientation(): ScreenOrientation {
    val configuration = LocalConfiguration.current
    return when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> ScreenOrientation.LANDSCAPE
        else -> ScreenOrientation.PORTRAIT
    }
}

/**
 * Composable to get current screen size category
 */
@Composable
fun rememberScreenSize(): ScreenSize {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    val smallestWidthDp = minOf(screenWidthDp, screenHeightDp)

    return when {
        smallestWidthDp < 360 -> ScreenSize.SMALL
        smallestWidthDp < 600 -> ScreenSize.NORMAL
        smallestWidthDp < 840 -> ScreenSize.LARGE
        else -> ScreenSize.XLARGE
    }
}

/**
 * Get adaptive padding based on screen size
 */
@Composable
fun getAdaptivePadding(
    small: Dp,
    normal: Dp,
    large: Dp = normal,
    xlarge: Dp = large
): Dp {
    return when (rememberScreenSize()) {
        ScreenSize.SMALL -> small
        ScreenSize.NORMAL -> normal
        ScreenSize.LARGE -> large
        ScreenSize.XLARGE -> xlarge
    }
}

/**
 * Get adaptive text/icon size multiplier based on screen size
 */
@Composable
fun getAdaptiveSizeMultiplier(): Float {
    return when (rememberScreenSize()) {
        ScreenSize.SMALL -> 0.9f
        ScreenSize.NORMAL -> 1.0f
        ScreenSize.LARGE -> 1.1f
        ScreenSize.XLARGE -> 1.2f
    }
}

/**
 * Check if screen is in landscape orientation
 */
@Composable
fun isLandscape(): Boolean {
    return rememberScreenOrientation() == ScreenOrientation.LANDSCAPE
}

/**
 * Check if screen is compact (small height in landscape or small width in portrait)
 */
@Composable
fun isCompactScreen(): Boolean {
    val configuration = LocalConfiguration.current
    val isLandscape = rememberScreenOrientation() == ScreenOrientation.LANDSCAPE

    return if (isLandscape) {
        configuration.screenHeightDp < 480
    } else {
        configuration.screenWidthDp < 360
    }
}

/**
 * Get number of columns for grid layouts based on screen size and orientation
 */
@Composable
fun getGridColumns(): Int {
    val screenSize = rememberScreenSize()
    val isLandscape = isLandscape()

    return when {
        screenSize == ScreenSize.XLARGE && isLandscape -> 4
        screenSize == ScreenSize.XLARGE -> 3
        screenSize == ScreenSize.LARGE && isLandscape -> 3
        screenSize == ScreenSize.LARGE -> 2
        isLandscape -> 2
        else -> 1
    }
}

/**
 * Adaptive spacing values that adjust based on screen size
 */
object AdaptiveSpacing {
    val extraSmall: Dp
        @Composable get() = getAdaptivePadding(2.dp, 4.dp, 6.dp, 8.dp)

    val small: Dp
        @Composable get() = getAdaptivePadding(4.dp, 8.dp, 10.dp, 12.dp)

    val medium: Dp
        @Composable get() = getAdaptivePadding(8.dp, 12.dp, 14.dp, 16.dp)

    val standard: Dp
        @Composable get() = getAdaptivePadding(12.dp, 16.dp, 20.dp, 24.dp)

    val large: Dp
        @Composable get() = getAdaptivePadding(16.dp, 24.dp, 28.dp, 32.dp)
}

/**
 * Adaptive icon sizes that adjust based on screen size
 */
object AdaptiveIconSize {
    val small: Dp
        @Composable get() = (24.dp * getAdaptiveSizeMultiplier())

    val medium: Dp
        @Composable get() = (32.dp * getAdaptiveSizeMultiplier())

    val large: Dp
        @Composable get() = (48.dp * getAdaptiveSizeMultiplier())
}

