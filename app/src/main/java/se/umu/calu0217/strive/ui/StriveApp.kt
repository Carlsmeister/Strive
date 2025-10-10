package se.umu.calu0217.strive.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.SpaceEvenly
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsGymnastics
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import se.umu.calu0217.strive.R
import se.umu.calu0217.strive.ui.screens.explore.ExploreScreen
import se.umu.calu0217.strive.ui.screens.history.HistoryScreen
import se.umu.calu0217.strive.ui.screens.profile.ProfileScreen
import se.umu.calu0217.strive.ui.screens.run.RunScreen
import se.umu.calu0217.strive.ui.screens.workout.WorkoutScreen
import se.umu.calu0217.strive.ui.screens.workout.ActiveWorkoutScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import se.umu.calu0217.strive.ui.theme.EnergeticOrange
import se.umu.calu0217.strive.ui.theme.White
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import se.umu.calu0217.strive.core.utils.isLandscape
import se.umu.calu0217.strive.core.utils.isCompactScreen
import se.umu.calu0217.strive.core.utils.AdaptiveIconSize
import se.umu.calu0217.strive.core.utils.AdaptiveSpacing

/**
 * Main application composable for the Strive fitness app.
 *
 * Sets up the navigation structure with a bottom navigation bar, top app bar with logo,
 * and a central floating action button for quick activity start.
 *
 * The app includes the following main screens:
 * - Explore: Browse and search exercises
 * - Workout: Manage workout templates and start gym sessions
 * - Run: GPS-tracked running/cycling/walking activities
 * - History: View past workout and run sessions
 * - Profile: User settings and preferences
 *
 * Navigation is handled with Jetpack Compose Navigation, supporting deep linking
 * to active workout sessions and maintaining navigation state across configuration changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StriveApp() {
    val navController = rememberNavController()
    var showStartDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isLandscape = isLandscape()
    val isCompact = isCompactScreen()

    val items = listOf(
        BottomDestination("explore", stringResource(R.string.nav_explore), Icons.Outlined.FitnessCenter),
        BottomDestination("workout", stringResource(R.string.nav_workout), Icons.Outlined.SportsGymnastics),
        BottomDestination("run", stringResource(R.string.run), Icons.AutoMirrored.Outlined.DirectionsRun),
        BottomDestination("history", stringResource(R.string.nav_history), Icons.Outlined.History)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = items.map { it.route }
    BackHandler(enabled = currentRoute in bottomNavRoutes) {
        // Exit the app when on a bottom nav screen
        (context as? ComponentActivity)?.finish()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // Logo with text - reduced size for more compact navbar
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo_with_no_bg),
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier.size(
                                if (isCompact || isLandscape) 28.dp else 36.dp  // Reduced from 32dp/48dp
                            ),
                        )
                        Text(
                            "TRIVE",
                            fontWeight = FontWeight.Bold,
                            style = if (isCompact || isLandscape)
                                MaterialTheme.typography.titleMedium
                            else
                                MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    // Profile icon in top right
                    IconButton(
                        onClick = {
                            navController.navigate("profile") {
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = stringResource(R.string.nav_profile),
                            modifier = Modifier.size(if (isCompact || isLandscape) 20.dp else 22.dp)  // Reduced from 24dp
                        )
                    }
                }
            )
        },
        bottomBar = {
            Box {
                NavigationBar(
                    modifier = Modifier.height(if (isCompact || isLandscape) 64.dp else 80.dp)
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    // Left side items (first two)
                    items.take(2).forEach { dest ->
                        NavigationBarItem(
                            modifier = Modifier.offset(y = if (isCompact || isLandscape) 15.dp else 15.dp),
                            icon = {
                                Icon(
                                    dest.icon,
                                    contentDescription = dest.label,
                                    modifier = Modifier.size(if (isCompact || isLandscape) 20.dp else AdaptiveIconSize.small)
                                )
                            },
                            label = {
                                Text(
                                    dest.label,
                                    style = if (isCompact || isLandscape) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    // Center placeholder to reserve space for the FAB
                    NavigationBarItem(
                        modifier = Modifier.offset(y = if (isCompact || isLandscape) 15.dp else 15.dp),
                        icon = { Spacer(modifier = Modifier.size(if (isCompact || isLandscape) 20.dp else AdaptiveIconSize.small)) },
                        label = { Text("") },
                        selected = false,
                        onClick = { },
                        enabled = false
                    )

                    // Right side items (last two)
                    items.takeLast(2).forEach { dest ->
                        NavigationBarItem(
                            modifier = Modifier.offset(y = if (isCompact || isLandscape) 15.dp else 15.dp),
                            icon = {
                                Icon(
                                    dest.icon,
                                    contentDescription = dest.label,
                                    modifier = Modifier.size(if (isCompact || isLandscape) 20.dp else AdaptiveIconSize.small)
                                )
                            },
                            label = {
                                Text(
                                    dest.label,
                                    style = if (isCompact || isLandscape) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { showStartDialog = true },
                    containerColor = EnergeticOrange,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = if (isCompact || isLandscape) 0.dp else (-5).dp)
                        .size(if (isCompact || isLandscape) 48.dp else 56.dp)
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.start),
                        modifier = Modifier.size(if (isCompact || isLandscape) 24.dp else 32.dp)
                    )
                }

                if (showStartDialog) {
                    AlertDialog(
                        onDismissRequest = { showStartDialog = false },
                        icon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo_with_no_bg),
                                    contentDescription = stringResource(R.string.app_name),
                                    modifier = Modifier.size(AdaptiveIconSize.large),
                                )
                                Text(
                                    "TRIVE",
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                    )
                            }
                        },
                        title = { Text(stringResource(R.string.start_activity), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    stringResource(R.string.choose_activity_hint),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(AdaptiveSpacing.standard))
                                Row ( modifier = Modifier.fillMaxWidth(), horizontalArrangement = SpaceEvenly) {
                                    Button(onClick = {
                                        showStartDialog = false
                                        navController.navigate("workout") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }) {
                                        Icon(Icons.Outlined.FitnessCenter, contentDescription = null)
                                        Spacer(modifier = Modifier.width(AdaptiveSpacing.small))
                                        Text(stringResource(R.string.gym))
                                    }
                                    Spacer(modifier = Modifier.height(AdaptiveSpacing.small))
                                    OutlinedButton(onClick = {
                                        showStartDialog = false
                                        navController.navigate("run") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }) {
                                        Icon(Icons.AutoMirrored.Outlined.DirectionsRun, contentDescription = null)
                                        Spacer(modifier = Modifier.width(AdaptiveSpacing.small))
                                        Text(stringResource(R.string.run))
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showStartDialog = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "explore",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("explore") {
                ExploreScreen()
            }
            composable("workout") {
                WorkoutScreen(
                    onNavigateToActiveWorkout = { sessionId ->
                        navController.navigate("active_workout/$sessionId")
                    }
                )
            }
            composable(
                route = "active_workout/{sessionId}",
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) {
                ActiveWorkoutScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("run") {
                RunScreen()
            }
            composable("history") {
                HistoryScreen()
            }
            composable("profile") {
                ProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

/**
 * Represents a bottom navigation bar destination.
 *
 * @property route The navigation route identifier used for navigation.
 * @property label The display label shown in the bottom navigation bar.
 * @property icon The icon displayed for this destination in the bottom navigation bar.
 */
data class BottomDestination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
