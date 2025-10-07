package se.umu.calu0217.strive.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import se.umu.calu0217.strive.R
import se.umu.calu0217.strive.core.constants.UiConstants
import se.umu.calu0217.strive.domain.models.Exercise
import se.umu.calu0217.strive.ui.screens.explore.ExploreViewModel

@Composable
fun ExerciseDetailDialog(
    exercise: Exercise,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text(exercise.name) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image preview (if available)
                exercise.imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = exercise.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(UiConstants.SMALL_PADDING))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Equipment
                Text(
                    text = stringResource(R.string.equipment),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = exercise.equipment.ifBlank { "Bodyweight" },
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Body parts
                Text(
                    text = stringResource(R.string.body_parts),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (exercise.bodyParts.isEmpty()) "—" else exercise.bodyParts.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium
                )

                if (exercise.instructions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.how_to_do_it),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(UiConstants.EXTRA_SMALL_PADDING))

                    exercise.instructions.forEachIndexed { index, step ->
                        Row(modifier = Modifier.padding(bottom = 6.dp)) {
                            Text(
                                text = stringResource(R.string.step_number, index + 1),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun AddToTemplateDialog(
    exercise: Exercise,
    onDismiss: () -> Unit,
    onAddToTemplate: (templateId: Long, sets: Int, reps: Int, restSec: Int) -> Unit,
    onCreateNewTemplate: (templateName: String) -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()

    var selectedTemplateId by remember { mutableStateOf<Long?>(null) }
    var sets by remember { mutableStateOf("3") }
    var reps by remember { mutableStateOf("12") }
    var restSec by remember { mutableStateOf("60") }
    var showCreateTemplateDialog by remember { mutableStateOf(false) }

    // Load templates when dialog opens
    LaunchedEffect(Unit) {
        viewModel.loadTemplates()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${exercise.name} to Template") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (templates.isEmpty()) {
                    // No templates available
                    Text(
                        text = stringResource(R.string.no_templates_found),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = UiConstants.STANDARD_PADDING)
                    )
                } else {
                    // Template selection
                    Text(
                        text = stringResource(R.string.select_template),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = UiConstants.SMALL_PADDING)
                    )

                    templates.forEach { template ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = UiConstants.EXTRA_SMALL_PADDING),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTemplateId == template.id,
                                onClick = { selectedTemplateId = template.id }
                            )
                            Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))
                            Text(
                                text = template.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(UiConstants.STANDARD_PADDING))
                }

                // Exercise parameters
                Text(
                    text = stringResource(R.string.exercise_settings),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = UiConstants.SMALL_PADDING)
                )

                // Sets input
                TextField(
                    value = sets,
                    onValueChange = { sets = it },
                    label = { Text(stringResource(R.string.sets)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = UiConstants.SMALL_PADDING)
                )

                // Reps input
                TextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text(stringResource(R.string.reps)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = UiConstants.SMALL_PADDING)
                )

                // Rest time input
                TextField(
                    value = restSec,
                    onValueChange = { restSec = it },
                    label = { Text(stringResource(R.string.rest_seconds)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = UiConstants.SMALL_PADDING)
                )
            }
        },
        confirmButton = {
            Row {
                // Create new template button
                TextButton(
                    onClick = { showCreateTemplateDialog = true }
                ) {
                    Text("New Template")
                }

                Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))

                // Add to selected template button
                Button(
                    onClick = {
                        val templateId = selectedTemplateId
                        val setsInt = sets.toIntOrNull() ?: 3
                        val repsInt = reps.toIntOrNull() ?: 12
                        val restSecInt = restSec.toIntOrNull() ?: 60

                        if (templateId != null) {
                            onAddToTemplate(templateId, setsInt, repsInt, restSecInt)
                        }
                    },
                    enabled = selectedTemplateId != null
                ) {
                    Text("Add to Template")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    // Create new template dialog
    if (showCreateTemplateDialog) {
        TextInputDialog(
            title = stringResource(R.string.create_template),
            label = stringResource(R.string.template_name_label),
            confirmText = stringResource(R.string.create),
            dismissText = stringResource(R.string.cancel),
            onDismiss = { showCreateTemplateDialog = false },
            onConfirm = { templateName ->
                onCreateNewTemplate(templateName)
                showCreateTemplateDialog = false
            }
        )
    }
}