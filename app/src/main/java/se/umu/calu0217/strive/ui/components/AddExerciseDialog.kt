package se.umu.calu0217.strive.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import se.umu.calu0217.strive.R
import se.umu.calu0217.strive.core.constants.UiConstants
import se.umu.calu0217.strive.core.utils.digits
import se.umu.calu0217.strive.core.validation.InputValidator
import se.umu.calu0217.strive.domain.models.Exercise

/**
 * Reusable dialog for adding an exercise with sets, reps, and rest configuration.
 * Used throughout the app for adding exercises to templates.
 */
@Composable
fun AddExerciseDialog(
    title: String = "Add Exercise",
    availableExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onAdd: (Exercise, Int, Int, Int) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedExerciseId by rememberSaveable { mutableStateOf<Long?>(null) }
    var setsInput by rememberSaveable { mutableStateOf("3") }
    var repsInput by rememberSaveable { mutableStateOf("10") }
    var restInput by rememberSaveable { mutableStateOf("60") }

    var setsError by remember { mutableStateOf<String?>(null) }
    var repsError by remember { mutableStateOf<String?>(null) }
    var restError by remember { mutableStateOf<String?>(null) }

    val selectedExercise = availableExercises.firstOrNull { it.id == selectedExerciseId }

    val filtered = remember(query, availableExercises) {
        if (query.isBlank()) {
            availableExercises.take(UiConstants.MAX_INITIAL_RESULTS)
        } else {
            availableExercises.filter { exercise ->
                val searchQuery = query.lowercase()
                exercise.name.lowercase().contains(searchQuery) ||
                exercise.equipment.lowercase().contains(searchQuery) ||
                exercise.bodyParts.any { it.lowercase().contains(searchQuery) }
            }.take(UiConstants.MAX_SEARCH_RESULTS)
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val screenHeight = configuration.screenHeightDp.dp

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = if (isLandscape) {
            DialogProperties(usePlatformDefaultWidth = false)
        } else {
            DialogProperties()
        },
        title = { Text(title) },
        text = {
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .width(700.dp)
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text(stringResource(R.string.search_exercises)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = selectedExercise?.let { "Selected: ${it.name}" } ?: "Select Exercise",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedExercise != null) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedExercise != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            itemsIndexed(filtered) { _, ex ->
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            ex.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            ex.equipment,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedExerciseId = ex.id
                                            query = ex.name
                                        },
                                    colors = if (ex.id == selectedExerciseId) {
                                        ListItemDefaults.colors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    } else {
                                        ListItemDefaults.colors()
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 240.dp),

                    ) {
                        OutlinedTextField(
                            value = setsInput,
                            onValueChange = {
                                setsInput = it.digits(UiConstants.MAX_SETS_DIGITS)
                                setsError = InputValidator.validateSets(setsInput)
                            },
                            label = { Text(stringResource(R.string.sets)) },
                            isError = setsError != null,
                            supportingText = null,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        )

                        OutlinedTextField(
                            value = repsInput,
                            onValueChange = {
                                repsInput = it.digits(UiConstants.MAX_REPS_DIGITS)
                                repsError = InputValidator.validateReps(repsInput)
                            },
                            label = { Text(stringResource(R.string.reps)) },
                            isError = repsError != null,
                            supportingText = null,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        )

                        OutlinedTextField(
                            value = restInput,
                            onValueChange = {
                                restInput = it.digits(UiConstants.MAX_REST_DIGITS)
                                restError = InputValidator.validateRestTime(restInput)
                            },
                            label = { Text(stringResource(R.string.rest_seconds_label)) },
                            isError = restError != null,
                            supportingText = null,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text(stringResource(R.string.search_exercises)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(UiConstants.SMALL_PADDING))

                    val exerciseListHeight = (screenHeight * 0.25f).coerceAtMost(150.dp).coerceAtLeast(100.dp)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = exerciseListHeight)
                    ) {
                        itemsIndexed(filtered) { _, ex ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        ex.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        ex.equipment,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedExerciseId = ex.id
                                        query = ex.name
                                    },
                                colors = if (ex.id == selectedExerciseId) {
                                    ListItemDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                } else {
                                    ListItemDefaults.colors()
                                }
                            )
                            HorizontalDivider()
                        }
                    }

                    Spacer(modifier = Modifier.height(UiConstants.SMALL_PADDING))
                    selectedExercise?.let { ex ->
                        Text(
                            "Selected: ${ex.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(UiConstants.SMALL_PADDING))
                    }

                    // More compact layout: 2 columns, 2 rows
                    Row(horizontalArrangement = Arrangement.spacedBy(UiConstants.SMALL_PADDING)) {
                        OutlinedTextField(
                            value = setsInput,
                            onValueChange = {
                                setsInput = it.digits(UiConstants.MAX_SETS_DIGITS)
                                setsError = InputValidator.validateSets(setsInput)
                            },
                            label = { Text(stringResource(R.string.sets)) },
                            isError = setsError != null,
                            supportingText = setsError?.let { err -> { Text(err, maxLines = 1) } },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = repsInput,
                            onValueChange = {
                                repsInput = it.digits(UiConstants.MAX_REPS_DIGITS)
                                repsError = InputValidator.validateReps(repsInput)
                            },
                            label = { Text(stringResource(R.string.reps)) },
                            isError = repsError != null,
                            supportingText = repsError?.let { err -> { Text(err, maxLines = 1) } },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(UiConstants.EXTRA_SMALL_PADDING))

                    OutlinedTextField(
                        value = restInput,
                        onValueChange = {
                            restInput = it.digits(UiConstants.MAX_REST_DIGITS)
                            restError = InputValidator.validateRestTime(restInput)
                        },
                        label = { Text(stringResource(R.string.rest_seconds_label)) },
                        isError = restError != null,
                        supportingText = restError?.let { err -> { Text(err, maxLines = 1) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedExercise != null && setsError == null && repsError == null && restError == null,
                onClick = {
                    val ex = selectedExercise ?: return@TextButton
                    val sets = setsInput.toIntOrNull()?.coerceIn(UiConstants.MIN_SETS, UiConstants.MAX_SETS) ?: 3
                    val reps = repsInput.toIntOrNull()?.coerceIn(UiConstants.MIN_REPS, UiConstants.MAX_REPS) ?: 10
                    val rest = restInput.toIntOrNull()?.coerceIn(UiConstants.MIN_REST_SEC, UiConstants.MAX_REST_SEC) ?: 60
                    onAdd(ex, sets, reps, rest)
                }
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
