package se.umu.calu0217.strive.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import se.umu.calu0217.strive.R
import se.umu.calu0217.strive.core.constants.UiConstants
import se.umu.calu0217.strive.domain.models.Exercise
import se.umu.calu0217.strive.ui.screens.explore.ExploreViewModel

/**
 * Displays a dialog with detailed information about an exercise.
 * Shows exercise image, equipment needed, body parts targeted, and step-by-step instructions.
 *
 * @param exercise The exercise to display details for.
 * @param onDismiss Callback invoked when the dialog is dismissed.
 */
@Composable
fun ExerciseDetailDialog(
    exercise: Exercise,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
        title = { Text(exercise.name) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
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

/**
 * Displays a dialog for adding an exercise to an existing workout template or creating a new one.
 * Allows users to configure sets, reps, and rest time for the exercise.
 *
 * @param exercise The exercise to add to a template.
 * @param onDismiss Callback invoked when the dialog is dismissed.
 * @param onAddToTemplate Callback invoked when adding the exercise to a selected template.
 *        Parameters: templateId, sets, reps, restSeconds.
 * @param onCreateNewTemplate Callback invoked when creating a new template.
 *        Parameter: templateName.
 * @param viewModel The view model for managing templates (injected via Hilt).
 */
@Composable
fun AddToTemplateDialog(
    exercise: Exercise,
    onDismiss: () -> Unit,
    onAddToTemplate: (templateId: Long, sets: Int, reps: Int, restSec: Int) -> Unit,
    onCreateNewTemplate: (templateName: String) -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    var selectedTemplateId by rememberSaveable { mutableStateOf<Long?>(null) }
    var sets by rememberSaveable { mutableStateOf("3") }
    var reps by rememberSaveable { mutableStateOf("12") }
    var restSec by rememberSaveable { mutableStateOf("60") }
    var showCreateTemplateDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTemplates()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = if (isLandscape) {
            DialogProperties(usePlatformDefaultWidth = false)
        } else {
            DialogProperties()
        },
        title = { Text(stringResource(R.string.add_to_template_title, exercise.name)) },
        text = {
            if (isLandscape) {
                // Landscape layout: Two columns
                Row(
                    modifier = Modifier
                        .width(700.dp)
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left column: Template selection
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 240.dp)
                    ) {
                        if (templates.isEmpty()) {
                            Text(
                                text = stringResource(R.string.no_templates_found),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = UiConstants.SMALL_PADDING)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.select_template),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = UiConstants.SMALL_PADDING)
                            )
                            Column (modifier = Modifier
                                .heightIn(max = 100.dp)
                                .verticalScroll(rememberScrollState()),
                            ) {
                                templates.forEach { template ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedTemplateId == template.id,
                                            onClick = { selectedTemplateId = template.id }
                                        )
                                        Spacer(modifier = Modifier.width(UiConstants.EXTRA_SMALL_PADDING))
                                        Text(
                                            text = template.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                        Row {
                            TextButton(
                                onClick = { showCreateTemplateDialog = true }
                            ) {
                                Text(stringResource(R.string.new_template))
                            }

                            Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))

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
                                Text(stringResource(R.string.add_to_template))
                            }
                        }
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.cancel))
                        }
                    }

                    // Right column: Exercise settings
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.exercise_settings),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )

                        OutlinedTextField(
                            value = sets,
                            onValueChange = { sets = it },
                            label = { Text(stringResource(R.string.sets)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        )

                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it },
                            label = { Text(stringResource(R.string.reps)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        )

                        OutlinedTextField(
                            value = restSec,
                            onValueChange = { restSec = it },
                            label = { Text(stringResource(R.string.rest_seconds)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        )
                    }
                }
            } else {
                // Portrait layout: Single column
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (templates.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_templates_found),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = UiConstants.STANDARD_PADDING)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.select_template),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = UiConstants.SMALL_PADDING)
                        )
                        Column (modifier = Modifier
                            .heightIn(max = 100.dp)
                            .verticalScroll(rememberScrollState()),
                        ) {
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
                        }

                        Spacer(modifier = Modifier.height(UiConstants.STANDARD_PADDING))
                    }

                    Text(
                        text = stringResource(R.string.exercise_settings),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = UiConstants.SMALL_PADDING)
                    )

                    OutlinedTextField(
                        value = sets,
                        onValueChange = { sets = it },
                        label = { Text(stringResource(R.string.sets)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = UiConstants.SMALL_PADDING)
                    )

                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text(stringResource(R.string.reps)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = UiConstants.SMALL_PADDING)
                    )

                    OutlinedTextField(
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
            }
        },
        confirmButton = {
            if (!isLandscape) {
                Row {
                    TextButton(
                        onClick = { showCreateTemplateDialog = true }
                    ) {
                        Text(stringResource(R.string.new_template))
                    }

                    Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))

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
                        Text(stringResource(R.string.add_to_template))
                    }
                }
            }
        },
        dismissButton = {
            if (!isLandscape) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }

    )

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