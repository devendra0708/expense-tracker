package com.expensetracker.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensetracker.app.data.CategoryTotal
import com.expensetracker.app.data.MonthlyTotal
import com.expensetracker.app.ui.CategoryIcons
import com.expensetracker.app.ui.formatCurrency
import com.expensetracker.app.util.DateUtils
@Composable
fun CategoryPieChart(
    categoryTotals: List<CategoryTotal>,
    modifier: Modifier = Modifier
) {
    val total = categoryTotals.sumOf { it.total }
    if (total <= 0) {
        Box(
            modifier = modifier.height(200.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No spending data for this month",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val slices = categoryTotals.sortedByDescending { it.total }
    var startAngle = -90f

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val strokeWidth = size.minDimension
            val radius = strokeWidth / 2f
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(strokeWidth, strokeWidth)

            slices.forEach { item ->
                val sweep = (item.total / total * 360f).toFloat()
                drawArc(
                    color = CategoryIcons.colorFor(item.category),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = topLeft,
                    size = arcSize
                )
                startAngle += sweep
            }
            drawCircle(
                color = Color.White,
                radius = radius * 0.55f,
                center = center
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            slices.take(5).forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .padding(0.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = CategoryIcons.colorFor(item.category))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.category.label} (${((item.total / total) * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyBarChart(
    monthlyTotals: List<MonthlyTotal>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    if (monthlyTotals.all { it.total <= 0 }) {
        Box(
            modifier = modifier.height(200.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No monthly data yet",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxTotal = monthlyTotals.maxOf { it.total }.coerceAtLeast(1.0)
    val barColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val barCount = monthlyTotals.size
            val gap = size.width * 0.04f
            val barWidth = (size.width - gap * (barCount + 1)) / barCount
            val chartHeight = size.height - 24f

            monthlyTotals.forEachIndexed { index, item ->
                val barHeight = (item.total / maxTotal * chartHeight).toFloat()
                val left = gap + index * (barWidth + gap)
                val top = chartHeight - barHeight

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            monthlyTotals.forEach { item ->
                Text(
                    text = DateUtils.formatMonthShort(item.year, item.month),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            monthlyTotals.forEach { item ->
                Text(
                    text = formatCurrency(item.total, currencySymbol),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun BudgetProgressBar(
    spent: Double,
    budget: Double,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    val progress = if (budget > 0) (spent / budget).coerceIn(0.0, 1.0).toFloat() else 0f
    val overBudget = budget > 0 && spent > budget
    val color = when {
        overBudget -> MaterialTheme.colorScheme.error
        progress > 0.8f -> Color(0xFFE65100)
        else -> MaterialTheme.colorScheme.primary
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatCurrency(spent, currencySymbol),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "of ${formatCurrency(budget, currencySymbol)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.material3.LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        if (overBudget) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Over budget by ${formatCurrency(spent - budget, currencySymbol)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else if (budget > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatCurrency(budget - spent, currencySymbol)} remaining",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
