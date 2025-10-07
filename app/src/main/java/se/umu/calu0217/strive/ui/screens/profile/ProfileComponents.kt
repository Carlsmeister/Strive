@file:OptIn(ExperimentalMaterial3Api::class)
package se.umu.calu0217.strive.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import se.umu.calu0217.strive.R
import se.umu.calu0217.strive.core.constants.UiConstants
import se.umu.calu0217.strive.core.validation.InputValidator.validateHeight
import se.umu.calu0217.strive.core.validation.InputValidator.validateWeight
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Screen-scoped UI components for the Profile feature.
 * These composables and helper types are intended for use within the Profile screen.
 * Promote individual components to ui/components/ when they become shared across features.
 */

@Composable
fun WeeklyWorkoutsDashboard(workoutDayCounts: List<DayLabelCount>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(UiConstants.STANDARD_PADDING)) {
            Text(stringResource(R.string.this_week), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            val maxVal = (workoutDayCounts.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
            val barWidth = UiConstants.SMALL_ICON_SIZE
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
fun Bar(heightFraction: Float, color: Color, width: Dp, maxHeight: Dp = 100.dp) {
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

fun last7DaysData(workouts: List<se.umu.calu0217.strive.domain.models.WorkoutSession>): List<DayLabelCount> {
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
fun AboutCard(modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UiConstants.STANDARD_PADDING),
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
                    text = stringResource(R.string.strive_fitness),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.app_version, "1.0.0"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DangerZoneCard(onClearAll: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(UiConstants.STANDARD_PADDING)) {
            Text(
                text = stringResource(R.string.danger_zone),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(UiConstants.SMALL_PADDING))
            OutlinedButton(
                onClick = onClearAll,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(UiConstants.STANDARD_PADDING))
                Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))
                Text(stringResource(R.string.clear_all_data))
            }
        }
    }
}

@Composable
fun ResultChip(title: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = UiConstants.SMALL_PADDING), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun DailyMacrosCard(
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
    var weightError by remember { mutableStateOf<String?>(null) }
    var heightError by remember { mutableStateOf<String?>(null) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(UiConstants.STANDARD_PADDING)) {
            Text(stringResource(R.string.daily_macros), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            // Inputs row 1: Weight, Height
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = {
                        onWeightChange(it)
                        weightError = validateWeight(it)
                    },
                    label = { Text(stringResource(R.string.weight_kg)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = weightError != null,
                    supportingText = weightError?.let { { Text(it) } },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = heightCm,
                    onValueChange = {
                        onHeightChange(it)
                        heightError = validateHeight(it)
                    },
                    label = { Text(stringResource(R.string.height_cm)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = heightError != null,
                    supportingText = heightError?.let { { Text(it) } },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Inputs row 2: Age, Sex
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = age,
                    onValueChange = onAgeChange,
                    label = { Text(stringResource(R.string.age_years)) },
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
            Text(stringResource(R.string.macro_split), style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = proteinPct,
                    onValueChange = onProteinChange,
                    label = { Text(stringResource(R.string.protein_percent)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = carbsPct,
                    onValueChange = onCarbsChange,
                    label = { Text(stringResource(R.string.carbs_percent)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = fatPct,
                    onValueChange = onFatChange,
                    label = { Text(stringResource(R.string.fat_percent)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(UiConstants.STANDARD_PADDING))

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
                ResultChip(title = stringResource(R.string.calories), value = stringResource(R.string.kcal_value, tdee.roundToInt()))
                ResultChip(title = stringResource(R.string.protein), value = stringResource(R.string.grams_value, proteinG.roundToInt()))
                ResultChip(title = stringResource(R.string.carbs), value = stringResource(R.string.grams_value, carbsG.roundToInt()))
                ResultChip(title = stringResource(R.string.fat), value = stringResource(R.string.grams_value, fatG.roundToInt()))
            }

            if (pctSum != 100) {
                Spacer(modifier = Modifier.height(UiConstants.SMALL_PADDING))
                Text(
                    text = stringResource(R.string.macro_sum_warning, pctSum),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SegmentedButtonsSex(current: Sex, onChange: (Sex) -> Unit, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        SegmentedButton(
            selected = current == Sex.Male,
            onClick = { onChange(Sex.Male) },
            shape = SegmentedButtonDefaults.itemShape(0, 2),
            label = { Text(stringResource(R.string.male), textAlign = TextAlign.Center) }
        )
        SegmentedButton(
            selected = current == Sex.Female,
            onClick = { onChange(Sex.Female) },
            shape = SegmentedButtonDefaults.itemShape(1, 2),
            label = { Text(stringResource(R.string.female), textAlign = TextAlign.Center) }
        )
    }
}

@Composable
fun DropdownActivity(current: ActivityLevel, onChange: (ActivityLevel) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            readOnly = true,
            value = current.label,
            onValueChange = {},
            label = { Text(stringResource(R.string.activity)) },
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
fun DropdownGoal(current: Goal, onChange: (Goal) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            readOnly = true,
            value = current.label,
            onValueChange = {},
            label = { Text(stringResource(R.string.goal)) },
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

fun mifflinStJeor(sex: Sex, weightKg: Double, heightCm: Int, age: Int): Double {
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
