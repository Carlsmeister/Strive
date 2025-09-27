package se.umu.calu0217.strive.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.SpaceEvenly
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RunCircle
import androidx.compose.material.icons.outlined.SportsGymnastics
import androidx.compose.material.icons.outlined.TempleHindu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
import se.umu.calu0217.strive.ui.screens.templates.TemplatesScreen
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StriveApp() {
    val navController = rememberNavController()
    var showStartDialog by remember { mutableStateOf(false) }

    val items = listOf(
        BottomDestination("explore", "Exercises", Icons.Outlined.FitnessCenter),
        BottomDestination("workout", "Workout", Icons.Outlined.SportsGymnastics),
        BottomDestination("run", "Run", Icons.AutoMirrored.Outlined.DirectionsRun),
        BottomDestination("history", "History", Icons.Outlined.History)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // Logo with text
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo_with_no_bg),
                            contentDescription = "Strive Logo",
                            modifier = Modifier.size(40.dp),
                        )
                        Text(
                            "TRIVE",
                            fontWeight = FontWeight.Bold,
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
                            contentDescription = "Profile"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Box {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    // Left side items (first two)
                    items.take(2).forEach { dest ->
                        NavigationBarItem(
                            modifier = Modifier.offset(y = (15).dp),
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
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
                        modifier = Modifier.offset(y = (15).dp),
                        icon = { Spacer(modifier = Modifier.size(24.dp)) },
                        label = { Text("") },
                        selected = false,
                        onClick = { },
                        enabled = false
                    )

                    // Right side items (last two)
                    items.takeLast(2).forEach { dest ->
                        NavigationBarItem(
                            modifier = Modifier.offset(y = (15).dp),
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
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
                        .offset(y = (-15).dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Start")
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
                                    contentDescription = "Strive Logo",
                                    modifier = Modifier.size(40.dp),
                                )
                                Text(
                                    "TRIVE",
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                    )
                            }
                        },
                        title = { Text("Start activity", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "What workout would you like to do?",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
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
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Gym")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(onClick = {
                                        showStartDialog = false
                                        navController.navigate("run") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }) {
                                        Icon(Icons.Outlined.DirectionsRun, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Run")
                                    }
                                }

                            }
                        },
                        confirmButton = {},
                        dismissButton = {}
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "explore",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("explore") {
                ExploreScreen(
                    onExerciseClick = { exerciseId ->
                        // Navigate to exercise detail (to be implemented)
                    }
                )
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
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                ActiveWorkoutScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("run") {
                RunScreen(
                    onNavigateToRunDetail = { runId ->
                        // Navigate to run detail (to be implemented)
                    }
                )
            }
            composable("history") {
                HistoryScreen(
                    onWorkoutClick = { sessionId ->
                        // Navigate to workout session detail (to be implemented)
                    },
                    onRunClick = { runId ->
                        // Navigate to run detail (to be implemented)
                    }
                )
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

data class BottomDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
