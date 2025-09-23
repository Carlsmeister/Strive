package se.umu.calu0217.strive.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RunCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import se.umu.calu0217.strive.ui.screens.explore.ExploreScreen
import se.umu.calu0217.strive.ui.screens.history.HistoryScreen
import se.umu.calu0217.strive.ui.screens.profile.ProfileScreen
import se.umu.calu0217.strive.ui.screens.run.RunScreen
import se.umu.calu0217.strive.ui.screens.templates.TemplatesScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StriveApp() {
    val navController = rememberNavController()

    val items = listOf(
        BottomDestination("explore", "Explore", Icons.Outlined.Explore),
        BottomDestination("templates", "Templates", Icons.Outlined.FitnessCenter),
        BottomDestination("run", "Run", Icons.Outlined.RunCircle),
        BottomDestination("history", "History", Icons.Outlined.History),
        BottomDestination("profile", "Profile", Icons.Outlined.Person)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("STRIVE", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { dest ->
                    NavigationBarItem(
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
            composable("templates") {
                TemplatesScreen(
                    onNavigateToWorkout = { sessionId ->
                        // Navigate to workout session (to be implemented)
                    },
                    onEditTemplate = { templateId ->
                        // Navigate to template editor (to be implemented)
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
                ProfileScreen()
            }
        }
    }
}

data class BottomDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private fun NavDestination?.isInHierarchy(route: String): Boolean {
    var current = this
    while (current != null) {
        if (current.route == route) return true
        current = current.parent
    }
    return false
}
