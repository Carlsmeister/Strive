package se.umu.calu0217.strive.ui.screens.explore

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import se.umu.calu0217.strive.domain.models.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ExploreScreen(
    onExerciseClick: (Long) -> Unit = {},
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilters by viewModel.selectedFilters.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Local UI state for dialogs
        var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
        var exerciseToAddToTemplate by remember { mutableStateOf<Exercise?>(null) }

        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            placeholder = { Text("Search exercises...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("chest", "back", "legs", "shoulders", "arms", "core")
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilters.contains(filter),
                    onClick = { viewModel.toggleFilter(filter) },
                    label = { Text(filter.replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                ErrorCard(
                    error = uiState.error!!,
                    onDismiss = viewModel::clearError,
                    onRetry = { viewModel.retryLoadingExercises() },
                    onForceRefresh = { viewModel.forceRefreshExercises() }
                )
            }
            exercises.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No exercises found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            else -> {
                val listState = rememberLazyListState()
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {
                        items(exercises) { exercise ->
                            ExerciseCard(
                                exercise = exercise,
                                onClick = { selectedExercise = exercise },
                                onAddToTemplate = { exerciseToAddToTemplate = exercise }
                            )
                        }
                    }
                    VerticalScrollbar(
                        listState = listState,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(6.dp)
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }

        // Exercise Detail Dialog
        if (selectedExercise != null) {
            ExerciseDetailDialog(
                exercise = selectedExercise!!,
                onDismiss = { selectedExercise = null }
            )
        }

        // Add to Template Dialog
        if (exerciseToAddToTemplate != null) {
            AddToTemplateDialog(
                exercise = exerciseToAddToTemplate!!,
                onDismiss = { exerciseToAddToTemplate = null },
                onAddToTemplate = { templateId, sets, reps, restSec ->
                    viewModel.addExerciseToTemplate(
                        exerciseToAddToTemplate!!.id,
                        templateId,
                        sets,
                        reps,
                        restSec
                    )
                    exerciseToAddToTemplate = null
                },
                onCreateNewTemplate = { templateName ->
                    viewModel.createNewTemplate(templateName, exerciseToAddToTemplate!!.id)
                    exerciseToAddToTemplate = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit,
    onAddToTemplate: () -> Unit = {} // Added this parameter
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise Image
            exercise.imageUrl?.let { imageUrl ->
                val context = LocalContext.current
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .listener(
                            onSuccess = { _, _ ->
                                android.util.Log.d("ExerciseImage", "Loaded image for '${exercise.name}' -> $imageUrl")
                            },
                            onError = { _, result ->
                                android.util.Log.d("ExerciseImage", "Failed image for '${exercise.name}' -> $imageUrl cause=${result.throwable?.localizedMessage}")
                            }
                        )
                        .build(),
                    contentDescription = exercise.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    fallback = painterResource(id = android.R.drawable.ic_menu_gallery)
                )
            } ?: run {
                // Default fitness icon when no imageUrl is available
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Exercise image placeholder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Exercise Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )


                Text(
                    text = exercise.equipment,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Made the Add icon clickable
            IconButton(
                onClick = onAddToTemplate
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add to template",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ErrorCard(
    error: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onForceRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = when {
                    error.contains("internet", ignoreCase = true) -> "No Internet Connection"
                    error.contains("timeout", ignoreCase = true) -> "Connection Timeout"
                    error.contains("No exercises available", ignoreCase = true) -> "No Exercises Found"
                    else -> "Error Loading Exercises"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onRetry) {
                    Text("Try Again")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onForceRefresh,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Force Refresh",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}


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
                    text = "Equipment",
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
                    text = "Body parts",
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
                        text = "How to do it",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    exercise.instructions.forEachIndexed { index, step ->
                        Row(modifier = Modifier.padding(bottom = 6.dp)) {
                            Text(
                                text = "${index + 1}.",
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
    var newTemplateName by remember { mutableStateOf("") }

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
        AlertDialog(
            onDismissRequest = { showCreateTemplateDialog = false },
            title = { Text("Create New Template") },
            text = {
                TextField(
                    value = newTemplateName,
                    onValueChange = { newTemplateName = it },
                    label = { Text("Template Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTemplateName.isNotBlank()) {
                            onCreateNewTemplate(newTemplateName)
                            showCreateTemplateDialog = false
                        }
                    },
                    enabled = newTemplateName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTemplateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun VerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

    Canvas(modifier = modifier) {
        val height = size.height
        val width = size.width

        val layoutInfo = listState.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        val visibleItems = layoutInfo.visibleItemsInfo

        if (totalItems == 0 || visibleItems.isEmpty() || height <= 0f) {
            // Nothing to draw
            return@Canvas
        }

        val avgItemSizePx = visibleItems.map { it.size }.average().toFloat().coerceAtLeast(1f)
        val totalContentHeightPx = avgItemSizePx * totalItems
        val fractionVisible = (height / totalContentHeightPx).coerceIn(0.05f, 1f)
        val thumbHeightPx = fractionVisible * height

        val scrollPx = listState.firstVisibleItemIndex * avgItemSizePx + listState.firstVisibleItemScrollOffset
        val maxScrollPx = (totalContentHeightPx - height).coerceAtLeast(1f)
        val thumbTopPx = (scrollPx / maxScrollPx) * (height - thumbHeightPx)

        // Draw track
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(0f, 0f),
            size = Size(width, height),
            cornerRadius = CornerRadius(width / 2f, width / 2f)
        )

        // Draw thumb
        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(0f, thumbTopPx.coerceIn(0f, height - thumbHeightPx)),
            size = Size(width, thumbHeightPx),
            cornerRadius = CornerRadius(width / 2f, width / 2f)
        )
    }
}
