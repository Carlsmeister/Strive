package se.umu.calu0217.strive.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.lazy.LazyListState

@Composable
fun VerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val thumbColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

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
