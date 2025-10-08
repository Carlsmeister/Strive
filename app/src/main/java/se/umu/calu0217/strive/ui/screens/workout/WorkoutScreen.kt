package se.umu.calu0217.strive.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.umu.calu0217.strive.core.constants.UiConstants
import se.umu.calu0217.strive.core.utils.digits
import se.umu.calu0217.strive.domain.models.Exercise
import se.umu.calu0217.strive.domain.models.WorkoutTemplate
import se.umu.calu0217.strive.domain.models.TemplateExercise
import se.umu.calu0217.strive.ui.components.AddExerciseDialog
import se.umu.calu0217.strive.ui.components.TextInputDialog
import se.umu.calu0217.strive.ui.components.LoadingIndicator
import se.umu.calu0217.strive.ui.theme.EnergeticOrange
import se.umu.calu0217.strive.R

/**
 * Main workout screen for managing workout templates and starting workout sessions.
 * Provides quick start functionality and displays a list of saved workout templates.
 *
 * @param onNavigateToActiveWorkout Callback to navigate to the active workout screen with session ID.
 * @param viewModel The view model managing workout templates and sessions (injected via Hilt).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onNavigateToActiveWorkout: (Long) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(UiConstants.STANDARD_PADDING)
    ) {
        // Quick Start Section
        QuickStartCard(onStartWorkout = {
            viewModel.startQuickWorkout { sessionId ->
                onNavigateToActiveWorkout(sessionId)
            }
        })

        Spacer(modifier = Modifier.height(UiConstants.STANDARD_PADDING))

        // Templates Section Header
        TemplatesHeader(onCreateTemplate = { viewModel.showCreateTemplateDialog() })

        Spacer(modifier = Modifier.height(UiConstants.STANDARD_PADDING))

        // Templates List
        if (uiState.isLoading) {
            LoadingIndicator()
        } else if (uiState.templates.isEmpty()) {
            EmptyTemplatesCard()
        } else {
            TemplatesList(
                templates = uiState.templates,
                onStartWorkout = { template ->
                    viewModel.startWorkoutFromTemplate(template.id) { sessionId ->
                        onNavigateToActiveWorkout(sessionId)
                    }
                },
                onEditTemplate = { viewModel.editTemplate(it) },
                onAddExercises = { viewModel.openAddExercisesDialog(it) },
                onDeleteTemplate = { viewModel.deleteTemplate(it) }
            )
        }
    }

    // Dialogs
    WorkoutDialogs(
        uiState = uiState,
        viewModel = viewModel
    )
}

/**
 * Card component for quickly starting a workout without a template.
 *
 * @param onStartWorkout Callback invoked when the quick start button is clicked.
 */
@Composable
private fun QuickStartCard(onStartWorkout: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(UiConstants.STANDARD_PADDING)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(UiConstants.SMALL_ICON_SIZE)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.quick_start),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = stringResource(R.string.start_new_workout_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = UiConstants.STANDARD_PADDING)
            )
            Button(
                onClick = onStartWorkout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))
                Text(stringResource(R.string.start_new_workout))
            }
        }
    }
}

@Composable
private fun TemplatesHeader(onCreateTemplate: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.workout_templates),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        TextButton(onClick = onCreateTemplate) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(UiConstants.EXTRA_SMALL_PADDING))
            Text(stringResource(R.string.create))
        }
    }
}

@Composable
private fun EmptyTemplatesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UiConstants.LARGE_PADDING),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(UiConstants.LARGE_ICON_SIZE),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(UiConstants.STANDARD_PADDING))
            Text(
                text = stringResource(R.string.no_templates_yet),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TemplatesList(
    templates: List<WorkoutTemplate>,
    onStartWorkout: (WorkoutTemplate) -> Unit,
    onEditTemplate: (WorkoutTemplate) -> Unit,
    onAddExercises: (WorkoutTemplate) -> Unit,
    onDeleteTemplate: (WorkoutTemplate) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(templates) { template ->
            TemplateCard(
                template = template,
                onStartWorkout = { onStartWorkout(template) },
                onEditTemplate = { onEditTemplate(template) },
                onAddExercises = { onAddExercises(template) },
                onDeleteTemplate = { onDeleteTemplate(template) }
            )
        }
    }
}

@Composable
private fun WorkoutDialogs(
    uiState: WorkoutUiState,
    viewModel: WorkoutViewModel
) {
    // Create Template Dialog
    if (uiState.showCreateDialog) {
        TextInputDialog(
            title = stringResource(R.string.create_template),
            label = stringResource(R.string.template_name),
            description = stringResource(R.string.template_name_prompt),
            confirmText = stringResource(R.string.create),
            dismissText = stringResource(R.string.cancel),
            onDismiss = { viewModel.hideCreateTemplateDialog() },
            onConfirm = { templateName ->
                viewModel.createTemplate(templateName)
            }
        )
    }

    // Add Exercise Dialog
    if (uiState.showEditDialog && uiState.editingTemplate != null) {
        val template = uiState.editingTemplate!!
        AddExerciseDialog(
            title = "Add Exercise to \"${template.name}\" workout",
            availableExercises = uiState.availableExercises,
            onDismiss = { viewModel.hideEditTemplateDialog() },
            onAdd = { exercise, sets, reps, rest ->
                viewModel.addExerciseToTemplate(exercise.id, sets, reps, rest)
            }
        )
    }

    // Template Editor Dialog
    if (uiState.showEditorDialog && uiState.editorTemplate != null) {
        TemplateEditorDialog(
            template = uiState.editorTemplate!!,
            availableExercises = uiState.availableExercises,
            onDismiss = { viewModel.hideTemplateEditor() },
            onSave = { updatedTemplate ->
                viewModel.saveEditedTemplate(updatedTemplate)
            }
        )
    }
}

@Composable
private fun TemplateCard(
    template: WorkoutTemplate,
    onStartWorkout: () -> Unit,
    onEditTemplate: () -> Unit,
    onAddExercises: () -> Unit,
    onDeleteTemplate: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(UiConstants.STANDARD_PADDING)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = context.resources.getQuantityString(
                            R.plurals.exercises_count,
                            template.exercises.size,
                            template.exercises.size
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (template.exercises.isNotEmpty()) {
                        val totalSets = template.exercises.sumOf { it.sets }
                        val avgRest = template.exercises.map { it.restSec }.average().toInt()
                        Text(
                            text = stringResource(R.string.sets_and_avg_rest, totalSets, avgRest),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEditTemplate) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_template),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDeleteTemplate) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.clear_all_data),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (template.exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onStartWorkout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))
                    Text(stringResource(R.string.start_workout))
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onAddExercises,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))
                    Text(stringResource(R.string.add_exercises))
                }
            }
        }
    }
}

@Composable
private fun TemplateEditorDialog(
    template: WorkoutTemplate,
    availableExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onSave: (WorkoutTemplate) -> Unit
) {
    var nameText by remember(template.id) { mutableStateOf(template.name) }
    val items = remember(template.id) {
        mutableStateListOf<TemplateExercise>().apply {
            addAll(template.exercises.sortedBy { it.position })
        }
    }

    fun exerciseName(exId: Long): String {
        return availableExercises.firstOrNull { it.id == exId }?.name ?: "Exercise #$exId"
    }

    var showAddExerciseDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Edit Template", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { showAddExerciseDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Exercise")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Template name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (items.isEmpty()) {
                    Text("No exercises in this template yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        itemsIndexed(items) { index, te ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        exerciseName(te.exerciseId),
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { items.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Sets
                                        var setsStr by remember(te, index) { mutableStateOf(te.sets.toString()) }
                                        OutlinedTextField(
                                            value = setsStr,
                                            onValueChange = { v ->
                                                val filtered = v.digits(2)
                                                setsStr = filtered
                                                val newVal = filtered.toIntOrNull() ?: te.sets
                                                items[index] = items[index].copy(sets = newVal)
                                            },
                                            label = { Text("Sets") },
                                            singleLine = true,
                                            modifier = Modifier.width(70.dp)
                                        )
                                        // Reps
                                        var repsStr by remember(te, index) { mutableStateOf(te.reps.toString()) }
                                        OutlinedTextField(
                                            value = repsStr,
                                            onValueChange = { v ->
                                                val filtered = v.digits(3)
                                                repsStr = filtered
                                                val newVal = filtered.toIntOrNull() ?: te.reps
                                                items[index] = items[index].copy(reps = newVal)
                                            },
                                            label = { Text("Reps") },
                                            singleLine = true,
                                            modifier = Modifier.width(70.dp)
                                        )
                                        // Rest
                                        var restStr by remember(te, index) { mutableStateOf(te.restSec.toString()) }
                                        OutlinedTextField(
                                            value = restStr,
                                            onValueChange = { v ->
                                                val filtered = v.digits(4)
                                                restStr = filtered
                                                val newVal = filtered.toIntOrNull() ?: te.restSec
                                                items[index] = items[index].copy(restSec = newVal)
                                            },
                                            label = { Text("Rest", maxLines = 1) },
                                            singleLine = true,
                                            modifier = Modifier.width(90.dp)
                                        )
                                    }
                                    // Side up/down arrows
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (index > 0) {
                                                    val item = items.removeAt(index)
                                                    items.add(index - 1, item)
                                                }
                                            },
                                            enabled = index > 0
                                        ) {
                                            Icon(
                                                Icons.Filled.ArrowUpward,
                                                contentDescription = "Move up",
                                                tint = EnergeticOrange
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (index < items.size - 1) {
                                                    val item = items.removeAt(index)
                                                    items.add(index + 1, item)
                                                }
                                            },
                                            enabled = index < items.size - 1
                                        ) {
                                            Icon(
                                                Icons.Filled.ArrowDownward,
                                                contentDescription = "Move down",
                                                tint = EnergeticOrange
                                            )
                                        }
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Use the arrows on each exercise to reorder", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val updated = template.copy(
                    name = nameText.ifBlank { template.name },
                    exercises = items.mapIndexed { idx, e -> e.copy(position = idx) }
                )
                onSave(updated)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showAddExerciseDialog) {
        AddExerciseDialog(
            title = "Add Exercise",
            availableExercises = availableExercises,
            onDismiss = { showAddExerciseDialog = false },
            onAdd = { exercise, sets, reps, rest ->
                items.add(0, TemplateExercise(
                    exerciseId = exercise.id,
                    sets = sets,
                    reps = reps,
                    restSec = rest,
                    position = 0
                ))
                showAddExerciseDialog = false
            }
        )
    }
}