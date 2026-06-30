package com.ledgerly.expense.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ledgerly.expense.core.util.Money
import com.ledgerly.expense.domain.model.CategoryTotal
import com.ledgerly.expense.domain.model.MonthTotal

private fun parseColor(hex: String, fallback: Color): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(fallback)

/** A simple monthly-trend bar chart drawn on a Canvas (no chart library needed). */
@Composable
fun MonthlyTrendChart(
    data: List<MonthTotal>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
) {
    if (data.isEmpty()) return
    val max = (data.maxOfOrNull { it.totalCents } ?: 1L).coerceAtLeast(1L)
    Canvas(modifier = modifier.fillMaxWidth().height(160.dp).padding(top = 8.dp)) {
        val gap = 10.dp.toPx()
        val barWidth = (size.width - gap * (data.size - 1)) / data.size
        data.forEachIndexed { index, month ->
            val ratio = month.totalCents.toFloat() / max
            val barHeight = size.height * ratio
            val x = index * (barWidth + gap)
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            )
        }
    }
}

/** A donut chart + legend summarizing spending by category. */
@Composable
fun CategoryDonutChart(
    data: List<CategoryTotal>,
    modifier: Modifier = Modifier,
) {
    val total = data.sumOf { it.totalCents }
    if (total <= 0) return
    val fallback = MaterialTheme.colorScheme.primary
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(120.dp).padding(8.dp)) {
            var startAngle = -90f
            val stroke = Stroke(width = 26.dp.toPx())
            data.forEach { slice ->
                val sweep = 360f * (slice.totalCents.toFloat() / total)
                drawArc(
                    color = parseColor(slice.colorHex, fallback),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = stroke,
                )
                startAngle += sweep
            }
        }
        Column(
            modifier = Modifier.padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            data.take(5).forEach { slice ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(10.dp)) {
                        drawCircle(parseColor(slice.colorHex, fallback))
                    }
                    Text(
                        "  ${slice.categoryName}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        Money.formatCompact(slice.totalCents),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
