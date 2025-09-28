package se.umu.calu0217.strive.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.umu.calu0217.strive.domain.models.Exercise
import se.umu.calu0217.strive.domain.models.WorkoutTemplate

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
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Workout",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Start New Workout Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Quick Start",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "Start a workout without a template",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = {
                        viewModel.startQuickWorkout { sessionId ->
                            onNavigateToActiveWorkout(sessionId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start New Workout")
                }
            }
        }

        // Templates Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Workout Templates",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(
                onClick = { viewModel.showCreateTemplateDialog() }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Templates List
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.templates.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No templates yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Create your first workout template to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.templates) { template ->
                    TemplateCard(
                        template = template,
                        onStartWorkout = {
                            viewModel.startWorkoutFromTemplate(template.id) { sessionId ->
                                onNavigateToActiveWorkout(sessionId)
                            }
                        },
                        onEditTemplate = { viewModel.editTemplate(template) },
                        onAddExercises = { viewModel.openAddExercisesDialog(template) },
                        onDeleteTemplate = { viewModel.deleteTemplate(template) }
                    )
                }
            }
        }
    }

    // Create Template Dialog
    if (uiState.showCreateDialog) {
        CreateTemplateDialog(
            onDismiss = { viewModel.hideCreateTemplateDialog() },
            onConfirm = { templateName ->
                viewModel.createTemplate(templateName)
            }
        )
    }

    // Edit Template: Add Exercise Dialog
    run {
        val currentTemplate = uiState.editingTemplate
        if (uiState.showEditDialog && currentTemplate != null) {
            AddExerciseToTemplateDialog(
                templateName = currentTemplate.name,
                availableExercises = uiState.availableExercises,
                onDismiss = { viewModel.hideEditTemplateDialog() },
                onAdd = { exercise: Exercise, sets: Int, reps: Int, restSec: Int ->
                    viewModel.addExerciseToTemplate(exercise.id, sets, reps, restSec)
                }
            )
        }
    }

    // Full Template Editor Dialog
    run {
        val editTemplate = uiState.editorTemplate
        if (uiState.showEditorDialog && editTemplate != null) {
            TemplateEditorDialog(
                template = editTemplate,
                availableExercises = uiState.availableExercises,
                onDismiss = { viewModel.hideTemplateEditor() },
                onSave = { updated -> viewModel.saveEditedTemplate(updated) }
            )
        }
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
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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
                        text = "${template.exercises.size} exercises",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (template.exercises.isNotEmpty()) {
                        val totalSets = template.exercises.sumOf { it.sets }
                        val avgRest = template.exercises.map { it.restSec }.average().toInt()
                        Text(
                            text = "$totalSets sets • ${avgRest}s avg rest",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEditTemplate) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit template",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDeleteTemplate) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete template",
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Workout")
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onAddExercises,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Exercises")
                }
            }
        }
    }
}

@Composable
private fun CreateTemplateDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var templateName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Template") },
        text = {
            Column {
                Text("Enter a name for your new workout template")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text("Template name") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (templateName.isNotBlank()) {
                        onConfirm(templateName)
                        onDismiss()
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddExerciseToTemplateDialog(
    templateName: String,
    availableExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onAdd: (Exercise, Int, Int, Int) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var setsInput by remember { mutableStateOf("3") }
    var repsInput by remember { mutableStateOf("10") }
    var restInput by remember { mutableStateOf("60") }

    val filtered = remember(query, availableExercises) {
        if (query.isBlank()) availableExercises.take(20) else availableExercises.filter { it.name.contains(query, ignoreCase = true) }.take(20)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exercise to \"$templateName\" workout") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search exercises") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    itemsIndexed(filtered) { _, ex ->
                        ListItem(
                            headlineContent = { Text(ex.name) },
                            supportingContent = { Text(ex.bodyParts.joinToString()) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedExercise = ex
                                    query = ex.name
                                }
                        )
                        Divider()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                selectedExercise?.let {
                    Text("Selected: ${it.name}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = setsInput,
                        onValueChange = { setsInput = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("Sets") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = repsInput,
                        onValueChange = { repsInput = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("Reps") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = restInput,
                        onValueChange = { restInput = it.filter { ch -> ch.isDigit() }.take(4) },
                        label = { Text("Rest (s)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedExercise != null,
                onClick = {
                    val ex = selectedExercise ?: return@TextButton
                    val sets = setsInput.toIntOrNull() ?: 3
                    val reps = repsInput.toIntOrNull() ?: 10
                    val rest = restInput.toIntOrNull() ?: 60
                    onAdd(ex, sets, reps, rest)
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
        mutableStateListOf<se.umu.calu0217.strive.domain.models.TemplateExercise>().apply {
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
                                                val filtered = v.filter { it.isDigit() }.take(2)
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
                                                val filtered = v.filter { it.isDigit() }.take(3)
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
                                                val filtered = v.filter { it.isDigit() }.take(4)
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
                                                tint = se.umu.calu0217.strive.ui.theme.EnergeticOrange
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
                                                tint = se.umu.calu0217.strive.ui.theme.EnergeticOrange
                                            )
                                        }
                                    }
                                }
                            }
                            Divider()
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
        var query by remember { mutableStateOf("") }
        var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
        var setsInput by remember { mutableStateOf("3") }
        var repsInput by remember { mutableStateOf("10") }
        var restInput by remember { mutableStateOf("60") }

        val filtered = remember(query, availableExercises) {
            if (query.isBlank()) availableExercises.take(20) else availableExercises.filter { it.name.contains(query, ignoreCase = true) }.take(20)
        }

        AlertDialog(
            onDismissRequest = { showAddExerciseDialog = false },
            title = { Text("Add Exercise") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Search exercises") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        itemsIndexed(filtered) { _, ex ->
                            ListItem(
                                headlineContent = { Text(ex.name) },
                                supportingContent = { Text(ex.bodyParts.joinToString()) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedExercise = ex
                                        query = ex.name
                                    }
                            )
                            Divider()
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    selectedExercise?.let {
                        Text("Selected: ${it.name}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = setsInput,
                            onValueChange = { setsInput = it.filter { ch -> ch.isDigit() }.take(2) },
                            label = { Text("Sets") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = repsInput,
                            onValueChange = { repsInput = it.filter { ch -> ch.isDigit() }.take(3) },
                            label = { Text("Reps") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = restInput,
                            onValueChange = { restInput = it.filter { ch -> ch.isDigit() }.take(4) },
                            label = { Text("Rest (s)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = selectedExercise != null,
                    onClick = {
                        val ex = selectedExercise ?: return@TextButton
                        val sets = setsInput.toIntOrNull() ?: 3
                        val reps = repsInput.toIntOrNull() ?: 10
                        val rest = restInput.toIntOrNull() ?: 60
                        items.add(0, se.umu.calu0217.strive.domain.models.TemplateExercise(
                            exerciseId = ex.id,
                            sets = sets,
                            reps = reps,
                            restSec = rest,
                            position = 0
                        ))
                        showAddExerciseDialog = false
                    }
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddExerciseDialog = false }) { Text("Cancel") }
            }
        )
    }
}
