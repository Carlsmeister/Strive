package se.umu.calu0217.strive.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import se.umu.calu0217.strive.R
import se.umu.calu0217.strive.core.constants.UiConstants

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
            modifier = Modifier.padding(UiConstants.STANDARD_PADDING)
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

            Spacer(modifier = Modifier.height(UiConstants.SMALL_PADDING))

            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(UiConstants.STANDARD_PADDING))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
                Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))
                Button(onClick = onRetry) {
                    Text("Try Again")
                }
                Spacer(modifier = Modifier.width(UiConstants.SMALL_PADDING))
                Button(
                    onClick = onForceRefresh,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = stringResource(R.string.force_refresh),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}