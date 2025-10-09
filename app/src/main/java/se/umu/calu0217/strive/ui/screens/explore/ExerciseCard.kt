package se.umu.calu0217.strive.ui.screens.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import se.umu.calu0217.strive.domain.models.Exercise

/**
 * Card component displaying exercise information with image, name, equipment, and body parts.
 * Provides click actions to view details or add the exercise to a workout template.
 *
 * @param exercise The exercise data to display.
 * @param onClick Callback invoked when the card is clicked to view exercise details.
 * @param onAddToTemplate Callback invoked when the "add to template" button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit,
    onAddToTemplate: () -> Unit = {}
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
                                android.util.Log.d("ExerciseImage", "Failed image for '${exercise.name}' -> $imageUrl cause=${result.throwable.localizedMessage}")
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

            // Add icon button
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