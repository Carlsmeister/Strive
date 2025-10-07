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
                            .clip(RoundedCornerShape(8.dp))
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
                    Spacer(modifier = Modifier.height(4.dp))

                    exercise.instructions.forEachIndexed { index, step ->
                        Row(modifier = Modifier.padding(bottom = 6.dp)) {
                            Text(
                                text = stringResource(R.string.step_number, index + 1),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
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
                        text = "No workout templates found. Create a new template to add this exercise.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else {
                    // Template selection
                    Text(
                        text = "Select Template:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    templates.forEach { template ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTemplateId == template.id,
                                onClick = { selectedTemplateId = template.id }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = template.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Exercise parameters
                Text(
                    text = "Exercise Settings:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Sets input
                TextField(
                    value = sets,
                    onValueChange = { sets = it },
                    label = { Text("Sets") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // Reps input
                TextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // Rest time input
                TextField(
                    value = restSec,
                    onValueChange = { restSec = it },
                    label = { Text("Rest (s)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
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

                Spacer(modifier = Modifier.width(8.dp))

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
                Text("Cancel")
            }
        }
    )

    // Create new template dialog
    if (showCreateTemplateDialog) {
        TextInputDialog(
            title = "Create New Template",
            label = "Template Name",
            confirmText = "Create",
            dismissText = "Cancel",
            onDismiss = { showCreateTemplateDialog = false },
            onConfirm = { templateName ->
                onCreateNewTemplate(templateName)
                showCreateTemplateDialog = false
            }
        )
    }
}