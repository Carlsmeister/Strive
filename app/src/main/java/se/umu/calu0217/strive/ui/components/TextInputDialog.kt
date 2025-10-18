package se.umu.calu0217.strive.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.umu.calu0217.strive.core.validation.InputValidator

/**
 * Reusable dialog for text input with validation.
 * Used for creating templates, entering names, etc.
 */
@Composable
fun TextInputDialog(
    title: String,
    label: String,
    description: String? = null,
    initialValue: String = "",
    confirmText: String = "OK",
    dismissText: String = "Cancel",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    validateInput: ((String) -> String?)? = null
) {
    var inputText by rememberSaveable { mutableStateOf(initialValue) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        errorText = validateInput?.invoke(it) ?: InputValidator.validateTemplateName(it)
                    },
                    label = { Text(label) },
                    isError = errorText != null,
                    supportingText = errorText?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = inputText.isNotBlank() && errorText == null,
                onClick = {
                    val trimmedInput = inputText.trim()
                    if (trimmedInput.isNotBlank()) {
                        onConfirm(trimmedInput)
                    }
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}