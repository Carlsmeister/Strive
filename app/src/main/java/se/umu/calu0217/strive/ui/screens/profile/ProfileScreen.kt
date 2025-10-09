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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.umu.calu0217.strive.R
import se.umu.calu0217.strive.core.constants.UiConstants
import se.umu.calu0217.strive.ui.screens.history.HistoryViewModel
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import se.umu.calu0217.strive.core.utils.PreferencesUtils.setAutoStartGpsEnabled
import se.umu.calu0217.strive.core.utils.PreferencesUtils.isAutoStartGpsEnabled
import se.umu.calu0217.strive.core.utils.digits
import se.umu.calu0217.strive.ui.components.ConfirmationDialog
import androidx.activity.compose.BackHandler

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

/**
 * User profile and settings screen.
 * Allows users to configure personal information, fitness goals, and app preferences.
 * Displays weekly workout statistics and provides data management options.
 *
 * @param onNavigateBack Callback to navigate back to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userWeight by rememberSaveable { mutableStateOf("70.0") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = onNavigateBack != null) {
        onNavigateBack?.invoke()
    }

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
                .padding(UiConstants.STANDARD_PADDING)
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
                        modifier = Modifier.size(UiConstants.MEDIUM_ICON_SIZE),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))
                    Text(
                        text = stringResource(R.string.profile),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Back button on the far right
                onNavigateBack?.let { backCallback ->
                    IconButton(onClick = backCallback) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(UiConstants.LARGE_PADDING))

            // Dashboard: workouts per day, last 7 days
            WeeklyWorkoutsDashboard(workoutDayCounts = last7DaysData(workoutSessions))

            Spacer(modifier = Modifier.height(UiConstants.LARGE_PADDING))

            // User Settings Section
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = UiConstants.STANDARD_PADDING)
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

            Spacer(modifier = Modifier.height(UiConstants.LARGE_PADDING))

            val locationContext = LocalContext.current
            var autoStartGps by remember { mutableStateOf(isAutoStartGpsEnabled(locationContext)) }

            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(UiConstants.SMALL_PADDING)) {
                    Text(
                        text = stringResource(R.string.location),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(UiConstants.EXTRA_SMALL_PADDING))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.start_gps_on_launch))
                        Switch(
                            checked = autoStartGps,
                            onCheckedChange = {
                                autoStartGps = it
                                setAutoStartGpsEnabled(locationContext, it)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(UiConstants.EXTRA_SMALL_PADDING))
                    OutlinedButton(onClick = {
                        try {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            locationContext.startActivity(intent)
                        } catch (_: Exception) { }
                    }) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))
                        Text(stringResource(R.string.open_location_settings))
                    }
                }
            }

            Spacer(modifier = Modifier.height(UiConstants.STANDARD_PADDING))

            // Bottom row: About and Danger Zone side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiConstants.STANDARD_PADDING)
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
            title = stringResource(R.string.clear_all_title),
            message = stringResource(R.string.clear_all_confirmation),
            confirmText = stringResource(R.string.clear_all),
            dismissText = stringResource(R.string.cancel),
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                scope.launch {
                    // Clear all workout data by deleting the database
                    showDeleteDialog = false
                }
            }
        )
    }
}
