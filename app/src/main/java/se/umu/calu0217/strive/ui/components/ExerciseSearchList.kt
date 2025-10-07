package se.umu.calu0217.strive.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import se.umu.calu0217.strive.core.constants.UiConstants
import se.umu.calu0217.strive.domain.models.Exercise

/**
 * Shared composable for searching and picking exercises from a list.
 * Consolidates duplicated UI from multiple dialogs.
 */
@Composable
fun ExerciseSearchList(
    availableExercises: List<Exercise>,
    query: String,
    onQueryChange: (String) -> Unit,
    onItemClick: (Exercise) -> Unit,
    modifier: Modifier = Modifier,
    maxResults: Int = UiConstants.MAX_SEARCH_RESULTS
) {
    val filtered = remember(query, availableExercises) {
        if (query.isBlank()) {
            availableExercises.take(maxResults)
        } else {
            availableExercises.filter { it.name.contains(query, ignoreCase = true) }.take(maxResults)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search exercises") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Only show the list if we have results and the query doesn't exactly match a single exercise
        val shouldShowList =
            filtered.isNotEmpty() &&
                !(filtered.size == 1 && filtered.first().name.equals(query, ignoreCase = true))

        if (shouldShowList) {
            Spacer(modifier = Modifier.height(UiConstants.SMALL_PADDING))
            LazyColumn(modifier = Modifier.heightIn(max = UiConstants.DIALOG_LIST_MAX_HEIGHT)) {
                itemsIndexed(filtered) { _, ex ->
                    ListItem(
                        headlineContent = { Text(ex.name, style = MaterialTheme.typography.bodyLarge) },
                        supportingContent = { Text(ex.bodyParts.joinToString()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(ex) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}