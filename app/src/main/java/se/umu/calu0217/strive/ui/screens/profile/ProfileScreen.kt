@file:OptIn(ExperimentalMaterial3Api::class)
package se.umu.calu0217.strive.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.umu.calu0217.strive.ui.screens.history.HistoryViewModel
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import se.umu.calu0217.strive.core.utils.digits
import se.umu.calu0217.strive.ui.components.ConfirmationDialog

// DataStore for profile settings
private val Context.profileDataStore by preferencesDataStore(name = "profile_prefs")

private object ProfilePrefsKeys {
    val WEIGHT = stringPreferencesKey("weight")
    val AGE = stringPreferencesKey("age")
    val HEIGHT = stringPreferencesKey("height")
    val SEX = stringPreferencesKey("sex")
    val ACTIVITY = stringPreferencesKey("activity")
    val GOAL = stringPreferencesKey("goal")
    val PROTEIN = stringPreferencesKey("protein_pct")
    val CARBS = stringPreferencesKey("carbs_pct")
    val FAT = stringPreferencesKey("fat_pct")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userWeight by rememberSaveable { mutableStateOf("70.0") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Macros inputs
    var age by rememberSaveable { mutableStateOf("25") }
    var heightCm by rememberSaveable { mutableStateOf("175") }
    var sex by rememberSaveable { mutableStateOf(Sex.Male) }
    var activity by rememberSaveable { mutableStateOf(ActivityLevel.ModeratelyActive) }
    var goal by rememberSaveable { mutableStateOf(Goal.Maintain) }
    var proteinPct by rememberSaveable { mutableStateOf("40") }
    var carbsPct by rememberSaveable { mutableStateOf("40") }
    var fatPct by rememberSaveable { mutableStateOf("20") }

    // Load saved preferences once on composition
    LaunchedEffect(Unit) {
        val prefs = context.profileDataStore.data.first()
        prefs[ProfilePrefsKeys.WEIGHT]?.let { userWeight = it }
        prefs[ProfilePrefsKeys.AGE]?.let { age = it }
        prefs[ProfilePrefsKeys.HEIGHT]?.let { heightCm = it }
        prefs[ProfilePrefsKeys.SEX]?.let { v ->
            runCatching { Sex.valueOf(v) }.getOrNull()?.let { sex = it }
        }
        prefs[ProfilePrefsKeys.ACTIVITY]?.let { v ->
            runCatching { ActivityLevel.valueOf(v) }.getOrNull()?.let { activity = it }
        }
        prefs[ProfilePrefsKeys.GOAL]?.let { v ->
            runCatching { Goal.valueOf(v) }.getOrNull()?.let { goal = it }
        }
        prefs[ProfilePrefsKeys.PROTEIN]?.let { proteinPct = it }
        prefs[ProfilePrefsKeys.CARBS]?.let { carbsPct = it }
        prefs[ProfilePrefsKeys.FAT]?.let { fatPct = it }
    }

    fun saveStr(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: String) {
        scope.launch { context.profileDataStore.edit { it[key] = value } }
    }

    val historyViewModel: HistoryViewModel = hiltViewModel()
    val workoutSessions by historyViewModel.workoutSessions.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header row with profile icon, text, and back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Back button on the far right
                onNavigateBack?.let { backCallback ->
                    IconButton(onClick = backCallback) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dashboard: workouts per day, last 7 days
            WeeklyWorkoutsDashboard(workoutDayCounts = last7DaysData(workoutSessions))

            Spacer(modifier = Modifier.height(24.dp))

            // User Settings Section
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Daily Macros Card
            DailyMacrosCard(
                weightText = userWeight,
                onWeightChange = {
                    val v = it.filter { ch -> ch.isDigit() || ch == '.' }.take(6)
                    userWeight = v
                    saveStr(ProfilePrefsKeys.WEIGHT, v)
                },
                age = age,
                onAgeChange = {
                    val v = it.digits(3)
                    age = v
                    saveStr(ProfilePrefsKeys.AGE, v)
                },
                heightCm = heightCm,
                onHeightChange = {
                    val v = it.digits(3)
                    heightCm = v
                    saveStr(ProfilePrefsKeys.HEIGHT, v)
                },
                sex = sex,
                onSexChange = {
                    sex = it
                    saveStr(ProfilePrefsKeys.SEX, it.name)
                },
                activity = activity,
                onActivityChange = {
                    activity = it
                    saveStr(ProfilePrefsKeys.ACTIVITY, it.name)
                },
                goal = goal,
                onGoalChange = {
                    goal = it
                    saveStr(ProfilePrefsKeys.GOAL, it.name)
                },
                proteinPct = proteinPct,
                onProteinChange = {
                    val v = it.digits(3)
                    proteinPct = v
                    saveStr(ProfilePrefsKeys.PROTEIN, v)
                },
                carbsPct = carbsPct,
                onCarbsChange = {
                    val v = it.digits(3)
                    carbsPct = v
                    saveStr(ProfilePrefsKeys.CARBS, v)
                },
                fatPct = fatPct,
                onFatChange = {
                    val v = it.digits(3)
                    fatPct = v
                    saveStr(ProfilePrefsKeys.FAT, v)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            val context = androidx.compose.ui.platform.LocalContext.current
            var autoStartGps by remember { mutableStateOf(se.umu.calu0217.strive.core.utils.PreferencesUtils.isAutoStartGpsEnabled(context)) }

            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Start GPS on app launch")
                        Switch(
                            checked = autoStartGps,
                            onCheckedChange = {
                                autoStartGps = it
                                se.umu.calu0217.strive.core.utils.PreferencesUtils.setAutoStartGpsEnabled(context, it)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(onClick = {
                        try {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            context.startActivity(intent)
                        } catch (_: Exception) { }
                    }) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Location settings")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom row: About and Danger Zone side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AboutCard(modifier = Modifier.weight(1f))
                DangerZoneCard(
                    onClearAll = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Clear All Data?",
            message = "This will permanently delete all your workouts, templates, and settings. This action cannot be undone.",
            confirmText = "Clear All",
            dismissText = "Cancel",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                scope.launch {
                    context.profileDataStore.edit { it.clear() }
                }
                // Reset UI to defaults
                userWeight = "70.0"
                age = "25"
                heightCm = "175"
                sex = Sex.Male
                activity = ActivityLevel.ModeratelyActive
                goal = Goal.Maintain
                proteinPct = "40"
                carbsPct = "40"
                fatPct = "20"
                showDeleteDialog = false
            }
        )
    }
}
