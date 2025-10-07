package se.umu.calu0217.strive.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    var query by remember { mutableStateOf("") }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var setsInput by remember { mutableStateOf("3") }
    var repsInput by remember { mutableStateOf("10") }
    var restInput by remember { mutableStateOf("60") }

    // Validation errors
    var setsError by remember { mutableStateOf<String?>(null) }
    var repsError by remember { mutableStateOf<String?>(null) }
    var restError by remember { mutableStateOf<String?>(null) }

    val filtered = remember(query, availableExercises) {
        if (query.isBlank()) {
            // Always limit results to avoid rendering thousands of items
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search exercises") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(UiConstants.SMALL_PADDING))

                LazyColumn(modifier = Modifier.heightIn(max = UiConstants.DIALOG_LIST_MAX_HEIGHT)) {
                    itemsIndexed(filtered) { _, ex ->
                        ListItem(
                            headlineContent = { Text(ex.name) },
                            supportingContent = { Text(ex.equipment) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = UiConstants.EXTRA_SMALL_PADDING)
                                .clickable {
                                    selectedExercise = ex
                                    query = ex.name
                                }
                        )
                        HorizontalDivider()
                    }
                }

                Spacer(modifier = Modifier.height(UiConstants.SMALL_PADDING))
                selectedExercise?.let {
                    Text(
                        "Selected: ${it.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(UiConstants.SMALL_PADDING))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(UiConstants.SMALL_PADDING)) {
                    OutlinedTextField(
                        value = setsInput,
                        onValueChange = { 
                            setsInput = it.digits(UiConstants.MAX_SETS_DIGITS)
                            setsError = InputValidator.validateSets(setsInput)
                        },
                        label = { Text("Sets") },
                        isError = setsError != null,
                        supportingText = setsError?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = repsInput,
                        onValueChange = { 
                            repsInput = it.digits(UiConstants.MAX_REPS_DIGITS)
                            repsError = InputValidator.validateReps(repsInput)
                        },
                        label = { Text("Reps") },
                        isError = repsError != null,
                        supportingText = repsError?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = restInput,
                        onValueChange = { 
                            restInput = it.digits(UiConstants.MAX_REST_DIGITS)
                            restError = InputValidator.validateRestTime(restInput)
                        },
                        label = { Text("Rest (s)") },
                        isError = restError != null,
                        supportingText = restError?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
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
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}