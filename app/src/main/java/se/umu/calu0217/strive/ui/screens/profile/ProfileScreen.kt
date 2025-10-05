@file:OptIn(ExperimentalMaterial3Api::class)
package se.umu.calu0217.strive.ui.screens.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

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
                    val v = it.filter { ch -> ch.isDigit() }.take(3)
                    age = v
                    saveStr(ProfilePrefsKeys.AGE, v)
                },
                heightCm = heightCm,
                onHeightChange = {
                    val v = it.filter { ch -> ch.isDigit() }.take(3)
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
                    val v = it.filter { ch -> ch.isDigit() }.take(3)
                    proteinPct = v
                    saveStr(ProfilePrefsKeys.PROTEIN, v)
                },
                carbsPct = carbsPct,
                onCarbsChange = {
                    val v = it.filter { ch -> ch.isDigit() }.take(3)
                    carbsPct = v
                    saveStr(ProfilePrefsKeys.CARBS, v)
                },
                fatPct = fatPct,
                onFatChange = {
                    val v = it.filter { ch -> ch.isDigit() }.take(3)
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
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Clear All Data?") },
            text = { Text("This will permanently delete all your workouts, templates, and settings. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
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
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun WeeklyWorkoutsDashboard(workoutDayCounts: List<DayLabelCount>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("This Week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            val maxVal = (workoutDayCounts.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
            val barWidth = 24.dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                workoutDayCounts.forEach { item ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Bar(heightFraction = item.count.toFloat() / maxVal.toFloat(), color = MaterialTheme.colorScheme.primary, width = barWidth)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${item.count}", style = MaterialTheme.typography.labelSmall)
                        Text(item.day, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(item.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    }
                }
            }
        }
    }
}

@Composable
private fun Bar(heightFraction: Float, color: Color, width: Dp, maxHeight: Dp = 100.dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(maxHeight)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxHeight * heightFraction.coerceIn(0f, 1f))
                .background(color, RoundedCornerShape(6.dp))
        )
    }
}

data class DayLabelCount(val day: String, val date: String, val count: Int)

private fun last7DaysData(workouts: List<se.umu.calu0217.strive.domain.models.WorkoutSession>): List<DayLabelCount> {
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    // Move to start of current week (Monday) at 00:00
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    val weekStart = cal.timeInMillis
    val dayMillis = 24L * 60 * 60 * 1000
    val dayFmt = SimpleDateFormat("E", Locale.getDefault())
    val dateFmt = SimpleDateFormat("dd/MM", Locale.getDefault())
    return (0..6).map { d ->
        val dayStart = weekStart + d * dayMillis
        val dayEnd = dayStart + dayMillis
        val count = workouts.count { it.endedAt != null && it.startedAt in dayStart until dayEnd }
        val day = dayFmt.format(Date(dayStart)).take(3)
        val date = dateFmt.format(Date(dayStart))
        DayLabelCount(day = day, date = date, count = count)
    }
}

@Composable
private fun AboutCard(modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Strive Fitness",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DangerZoneCard(onClearAll: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Danger Zone",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onClearAll,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear All Data")
            }
        }
    }
}

@Composable
private fun ResultChip(title: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DailyMacrosCard(
    weightText: String,
    onWeightChange: (String) -> Unit,
    age: String,
    onAgeChange: (String) -> Unit,
    heightCm: String,
    onHeightChange: (String) -> Unit,
    sex: Sex,
    onSexChange: (Sex) -> Unit,
    activity: ActivityLevel,
    onActivityChange: (ActivityLevel) -> Unit,
    goal: Goal,
    onGoalChange: (Goal) -> Unit,
    proteinPct: String,
    onProteinChange: (String) -> Unit,
    carbsPct: String,
    onCarbsChange: (String) -> Unit,
    fatPct: String,
    onFatChange: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Daily Macros", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            // Inputs row 1: Weight, Height
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = onWeightChange,
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = heightCm,
                    onValueChange = onHeightChange,
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Inputs row 2: Age, Sex
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = age,
                    onValueChange = onAgeChange,
                    label = { Text("Age (y)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                SegmentedButtonsSex(current = sex, onChange = onSexChange, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Inputs row 2: Activity, Goal
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                DropdownActivity(activity, onActivityChange, modifier = Modifier.weight(1f))
                DropdownGoal(goal, onGoalChange, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Inputs row 3: Macro percentages
            Text("Macro split (%)", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = proteinPct,
                    onValueChange = onProteinChange,
                    label = { Text("Protein %") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = carbsPct,
                    onValueChange = onCarbsChange,
                    label = { Text("Carbs %") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = fatPct,
                    onValueChange = onFatChange,
                    label = { Text("Fat %") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val pctP = proteinPct.toIntOrNull() ?: 0
            val pctC = carbsPct.toIntOrNull() ?: 0
            val pctF = fatPct.toIntOrNull() ?: 0
            val pctSum = (pctP + pctC + pctF).coerceAtLeast(1)

            val ageVal = age.toIntOrNull() ?: 0
            val heightVal = heightCm.toIntOrNull() ?: 0
            val weightKg = weightText.toDoubleOrNull() ?: 0.0
            val bmr = mifflinStJeor(sex, weightKg, heightVal, ageVal)
            val tdee = (bmr * activity.factor + goal.adjustment).coerceAtLeast(0.0)

            val proteinG = ((tdee * (pctP / 100.0)) / 4.0)
            val carbsG = ((tdee * (pctC / 100.0)) / 4.0)
            val fatG = ((tdee * (pctF / 100.0)) / 9.0)

            // Results
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                ResultChip(title = "Calories", value = tdee.roundToInt().toString() + " kcal")
                ResultChip(title = "Protein", value = proteinG.roundToInt().toString() + " g")
                ResultChip(title = "Carbs", value = carbsG.roundToInt().toString() + " g")
                ResultChip(title = "Fat", value = fatG.roundToInt().toString() + " g")
            }

            if (pctSum != 100) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Note: Macro percentages sum to $pctSum%. Consider adjusting to 100% for accuracy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SegmentedButtonsSex(current: Sex, onChange: (Sex) -> Unit, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        SegmentedButton(
            selected = current == Sex.Male,
            onClick = { onChange(Sex.Male) },
            shape = SegmentedButtonDefaults.itemShape(0, 2),
            label = { Text("Male", textAlign = TextAlign.Center) }
        )
        SegmentedButton(
            selected = current == Sex.Female,
            onClick = { onChange(Sex.Female) },
            shape = SegmentedButtonDefaults.itemShape(1, 2),
            label = { Text("Female", textAlign = TextAlign.Center) }
        )
    }
}

@Composable
private fun DropdownActivity(current: ActivityLevel, onChange: (ActivityLevel) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            readOnly = true,
            value = current.label,
            onValueChange = {},
            label = { Text("Activity") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ActivityLevel.values().forEach { level ->
                DropdownMenuItem(text = { Text(level.label) }, onClick = {
                    onChange(level)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun DropdownGoal(current: Goal, onChange: (Goal) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            readOnly = true,
            value = current.label,
            onValueChange = {},
            label = { Text("Goal") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Goal.values().forEach { g ->
                DropdownMenuItem(text = { Text(g.label) }, onClick = {
                    onChange(g)
                    expanded = false
                })
            }
        }
    }
}

private fun mifflinStJeor(sex: Sex, weightKg: Double, heightCm: Int, age: Int): Double {
    // male: 10*W + 6.25*H - 5*A + 5
    // female: 10*W + 6.25*H - 5*A - 161
    val base = 10.0 * weightKg + 6.25 * heightCm + (-5.0 * age)
    return if (sex == Sex.Male) base + 5.0 else base - 161.0
}

enum class Sex { Male, Female }

enum class ActivityLevel(val label: String, val factor: Double) {
    Sedentary("Sedentary (little/no exercise)", 1.2),
    LightlyActive("Lightly active (1-3 days/wk)", 1.375),
    ModeratelyActive("Moderately active (3-5 days/wk)", 1.55),
    VeryActive("Very active (6-7 days/wk)", 1.725),
    ExtraActive("Extra active (physical job)", 1.9)
}

enum class Goal(val label: String, val adjustment: Double) {
    Lose("Lose weight (~-500 kcal)", -500.0),
    Maintain("Maintain", 0.0),
    Gain("Gain weight (~+300 kcal)", 300.0)
}
