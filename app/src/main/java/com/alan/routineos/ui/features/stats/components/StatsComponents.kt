package com.alan.routineos.ui.features.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.domain.insights.InsightSeverity
import com.alan.routineos.domain.insights.RoutineInsight
import com.alan.routineos.domain.observability.DailyActivityCell
import com.alan.routineos.domain.observability.ExecutionConsistency
import com.alan.routineos.domain.observability.MostAdjustedActivity
import com.alan.routineos.domain.observability.StatsActivityOption
import com.alan.routineos.ui.features.stats.state.ActivityDetailStats
import com.alan.routineos.ui.theme.*
import java.util.Date

/**
 * ContributionHeatmapCard: GitHub-like daily activity tracker.
 */
@Composable
fun ContributionHeatmapCard(
    heatmapData: List<DailyActivityCell>,
    modifier: Modifier = Modifier
) {
    val activeDaysCount = heatmapData.count { it.completedCount > 0 }
    var selectedCell by remember { mutableStateOf<DailyActivityCell?>(null) }
    
    // Chunk into weeks of 7 days (columns)
    val columns = heatmapData.chunked(7)

    Surface(
        modifier = modifier,
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("MAPA DE APORTACIONES", style = MetaMono.copy(fontSize = 9.sp), color = ColorTextDim)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$activeDaysCount días con actividad en los últimos 90 días",
                style = TitleNode.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                color = ColorText
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Grid Layout: 7 rows, dynamic columns
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                columns.forEach { column ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        column.forEach { cell ->
                            val isSelected = selectedCell?.date == cell.date
                            val cellColor = when (cell.intensity) {
                                4 -> ColorExec
                                3 -> ColorExec.copy(alpha = 0.7f)
                                2 -> ColorPlan
                                1 -> ColorPlan.copy(alpha = 0.4f)
                                else -> ColorBorder.copy(alpha = 0.2f)
                            }
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        if (isSelected) ColorText else cellColor, 
                                        RoundedCornerShape(2.dp)
                                    )
                                    .clickable { selectedCell = if (isSelected) null else cell }
                            )
                        }
                        // Fill remaining spaces in the week if any
                        if (column.size < 7) {
                            repeat(7 - column.size) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color.Transparent)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detail or Legend
            if (selectedCell != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ColorBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = DateUtils.formatShortDate(selectedCell!!.date).uppercase(),
                            style = MetaMono.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                            color = ColorTextDim
                        )
                        if (selectedCell!!.totalCount > 0) {
                            Text(
                                text = "${selectedCell!!.completedCount} de ${selectedCell!!.totalCount} completadas",
                                style = TitleNode.copy(fontSize = 11.sp),
                                color = ColorText
                            )
                        } else {
                            Text(
                                text = "Sin actividades planificadas",
                                style = TitleNode.copy(fontSize = 11.sp),
                                color = ColorTextMuted
                            )
                        }
                    }
                    if (selectedCell!!.totalCount > 0) {
                        val rate = (selectedCell!!.completedCount.toFloat() / selectedCell!!.totalCount * 100).toInt()
                        Text(
                            text = "$rate%",
                            style = TitleNode.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            color = if (rate >= 80) ColorExec else if (rate >= 50) ColorPlan else ColorPending
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Menos", style = MetaMono.copy(fontSize = 9.sp), color = ColorTextMuted)
                    Spacer(modifier = Modifier.width(4.dp))
                    listOf(
                        ColorBorder.copy(alpha = 0.2f),
                        ColorPlan.copy(alpha = 0.4f),
                        ColorPlan,
                        ColorExec.copy(alpha = 0.7f),
                        ColorExec
                    ).forEach { color ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 1.dp)
                                .size(10.dp)
                                .background(color, RoundedCornerShape(2.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Más", style = MetaMono.copy(fontSize = 9.sp), color = ColorTextMuted)
                }
            }
        }
    }
}

/**
 * ConsistencyCard: Displays execution stability over time.
 */
@Composable
fun ConsistencyCard(
    consistency: ExecutionConsistency,
    hasEnoughData: Boolean,
    modifier: Modifier = Modifier
) {
    val statusText = when {
        !hasEnoughData -> "Pendiente"
        consistency.consistencyRate >= 0.8f -> "Excelente"
        consistency.consistencyRate >= 0.6f -> "Buena"
        else -> "Inestable"
    }
    
    val accentColor = when {
        !hasEnoughData -> ColorTextMuted
        consistency.consistencyRate >= 0.8f -> ColorExec
        consistency.consistencyRate >= 0.6f -> ColorPlan
        else -> ColorPending
    }

    Surface(
        modifier = modifier,
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("CONSISTENCIA (30 DÍAS)", style = MetaMono.copy(fontSize = 9.sp), color = ColorTextDim)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!hasEnoughData) {
                Text(
                    text = "Aún no hay días activos suficientes para calcular consistencia.",
                    style = MetaMono.copy(fontSize = 11.sp),
                    color = ColorTextMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${(consistency.consistencyRate * 100).toInt()}%",
                            style = TitleNode.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                            color = ColorText
                        )
                        Text(
                            text = statusText.uppercase(),
                            style = MetaMono.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = accentColor
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${consistency.consistentDays}/${consistency.activeDays}",
                            style = TitleNode.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            color = ColorText
                        )
                        Text(
                            text = "DÍAS CONSISTENTES",
                            style = MetaMono.copy(fontSize = 8.sp),
                            color = ColorTextMuted
                        )
                    }
                }
            }
        }
    }
}

/**
 * MostAdjustedActivitiesCard: List of activities with high volatility.
 */
@Composable
fun MostAdjustedActivitiesCard(
    activities: List<MostAdjustedActivity>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ACTIVIDADES CON MÁS AJUSTES", style = MetaMono.copy(fontSize = 9.sp), color = ColorTextDim)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (activities.isEmpty()) {
                Text(
                    text = "No hay ajustes registrados aún.",
                    style = MetaMono.copy(fontSize = 10.sp),
                    color = ColorTextMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                activities.forEachIndexed { index, activity ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(4.dp).background(ColorPending, CircleShape))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = activity.title,
                            style = TitleNode.copy(fontSize = 13.sp),
                            color = ColorText,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${activity.adjustedCount} AJUSTES",
                            style = MetaMono.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                            color = ColorTextMuted
                        )
                    }
                    if (index < activities.size - 1) {
                        HorizontalDivider(color = ColorBorder, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

/**
 * WeeklyProgressCard: Visualization of 7-day cumulative performance.
 */
@Composable
fun WeeklyProgressCard(
    averageCompletionRate: Float,
    totalSkippedCount: Int,
    totalModifiedCount: Int,
    hasEnoughData: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("RENDIMIENTO SEMANAL", style = MetaMono.copy(fontSize = 9.sp), color = ColorTextDim)
            Spacer(modifier = Modifier.height(12.dp))
            
            if (!hasEnoughData) {
                Text(
                    text = "Aún recolectando datos de la semana...",
                    style = MetaMono.copy(fontSize = 11.sp),
                    color = ColorTextMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(averageCompletionRate * 100).toInt()}%",
                        style = TitleNode.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                        color = ColorText
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        LinearProgressIndicator(
                            progress = { averageCompletionRate },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = if (averageCompletionRate >= 0.75f) ColorExec else if (averageCompletionRate >= 0.5f) ColorPlan else ColorPending,
                            trackColor = ColorBorder.copy(alpha = 0.2f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Promedio semanal", style = MetaMono.copy(fontSize = 8.sp), color = ColorTextMuted)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = ColorBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("OMITIDAS", style = MetaMono.copy(fontSize = 8.sp), color = ColorTextMuted)
                        Text("$totalSkippedCount", style = TitleNode.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = ColorText)
                    }
                    Column {
                        Text("MODIFICADAS", style = MetaMono.copy(fontSize = 8.sp), color = ColorTextMuted)
                        Text("$totalModifiedCount", style = TitleNode.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = ColorText)
                    }
                }
            }
        }
    }
}

/**
 * PlannedVsActualCard: Duration metrics tracker.
 */
@Composable
fun PlannedVsActualCard(
    plannedDurationMinutes: Int,
    actualDurationMinutes: Int,
    modifier: Modifier = Modifier
) {
    val delta = actualDurationMinutes - plannedDurationMinutes
    val description = when {
        actualDurationMinutes == 0 -> "Sin registros reales hoy"
        delta > 0 -> "Sobre el plan (+$delta m)"
        delta < 0 -> "Dentro del plan ($delta m)"
        else -> "Exactamente según el plan"
    }

    Surface(
        modifier = modifier,
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("DURACIÓN TOTAL (SÓLO HOY)", style = MetaMono.copy(fontSize = 9.sp), color = ColorTextDim)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("REAL", style = MetaMono.copy(fontSize = 8.sp), color = ColorTextMuted)
                    Text("${actualDurationMinutes}m", style = TitleNode.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = ColorExec)
                }
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(ColorBorder).align(Alignment.CenterVertically))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("META DIARIA", style = MetaMono.copy(fontSize = 8.sp), color = ColorTextMuted)
                    Text("${plannedDurationMinutes}m", style = TitleNode.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = ColorPlan)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = description.uppercase(),
                style = MetaMono.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                color = if (delta > 0 && actualDurationMinutes > 0) ColorPending else ColorTextMuted
            )
        }
    }
}

/**
 * InsightCard: Visualization of human-readable feedback.
 */
@Composable
fun InsightCard(
    insight: RoutineInsight,
    modifier: Modifier = Modifier
) {
    val accentColor = when (insight.severity) {
        InsightSeverity.POSITIVE -> ColorExec
        InsightSeverity.WARNING -> ColorPending
        InsightSeverity.INFO -> ColorPlan
    }

    Surface(
        modifier = modifier,
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .padding(top = 4.dp)
                    .background(accentColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = insight.title,
                    style = TitleNode.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = ColorText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight.message,
                    style = MetaMono.copy(fontSize = 11.sp, lineHeight = 16.sp),
                    color = ColorTextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = insight.category.name,
                    style = MetaMono.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                    color = accentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * SparklineChart: Organic temporal visualizer.
 * Integrated into the layout with subtle gradients.
 */
@Composable
fun SparklineChart(
    dataPoints: List<NodeFieldValue>,
    modifier: Modifier = Modifier,
    color: Color = ColorPlan
) {
    val values = dataPoints.mapNotNull { it.value.toFloatOrNull() }
    if (values.isEmpty()) return

    val max = values.maxOrNull() ?: 0f
    val min = values.minOrNull() ?: 0f
    val range = if (max - min == 0f) 1f else max - min

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = if (values.size > 1) width / (values.size - 1) else width

        val points = values.mapIndexed { index, value ->
            Offset(
                x = index * stepX,
                y = height - ((value - min) / range * height)
            )
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }

        // Area fill with gradient
        val fillPath = Path().apply {
            addPath(path)
            lineTo(points.last().x, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.15f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Main line
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/**
 * ActivityDetailCard: Deep dive into a specific human activity.
 */
@Composable
fun ActivityDetailCard(
    activityName: String,
    stats: ActivityDetailStats,
    hasEnoughHistory: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(ColorExec, CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = activityName.uppercase(),
                    style = TitleNode.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = ColorText
                )
                if (hasEnoughHistory) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stats.trend.uppercase(),
                        style = MetaMono.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = when(stats.trend) {
                            "Mejora" -> ColorExec
                            "Empeora" -> ColorPending
                            else -> ColorPlan
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!hasEnoughHistory) {
                Text(
                    text = "Todavía no hay historial suficiente para detectar tendencias.",
                    style = MetaMono.copy(fontSize = 11.sp),
                    color = ColorTextMuted,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            } else {
                // Grid of core metrics
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailMetric(
                        label = "Completado",
                        value = "${(stats.completionRate * 100).toInt()}%",
                        subValue = "${stats.completedCount}/${stats.totalDays} DÍAS",
                        modifier = Modifier.weight(1f)
                    )
                    DetailMetric(
                        label = "Postergado",
                        value = "${stats.totalPostpones}",
                        subValue = "TOTAL VECES",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (stats.avgPlannedDuration != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = ColorBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("DURACIÓN MEDIA", style = MetaMono.copy(fontSize = 8.sp), color = ColorTextMuted)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${stats.avgActualDuration ?: stats.avgPlannedDuration}m",
                                    style = TitleNode.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    color = ColorText
                                )
                                if (stats.avgActualDuration != null) {
                                    val diff = stats.avgActualDuration - stats.avgPlannedDuration
                                    Text(
                                        text = " ${if (diff >= 0) "+" else ""}${diff}m",
                                        style = MetaMono.copy(fontSize = 10.sp),
                                        color = if (diff > 0) ColorPending else ColorExec,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PLAN", style = MetaMono.copy(fontSize = 8.sp), color = ColorTextMuted)
                            Text(
                                text = "${stats.avgPlannedDuration}m",
                                style = TitleNode.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                color = ColorTextDim
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            // Specific Insight (Simplified or Empty)
            Surface(
                color = ColorBg,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(ColorPlan, CircleShape))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (hasEnoughHistory) stats.specificInsight else "Historial en construcción...",
                        style = MetaMono.copy(fontSize = 10.sp),
                        color = ColorText
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailMetric(
    label: String,
    value: String,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label.uppercase(), style = MetaMono.copy(fontSize = 8.sp), color = ColorTextMuted)
        Text(value, style = TitleNode.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold), color = ColorText)
        Text(subValue, style = MetaMono.copy(fontSize = 8.sp), color = ColorTextDim)
    }
}

/**
 * CustomSegmentedControl: Premium alternative to Material FilterChips.
 */
@Composable
fun CustomSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(ColorSurface, RoundedCornerShape(10.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .background(
                        color = if (isSelected) ColorBg else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onOptionSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.uppercase(),
                    style = MetaMono.copy(
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) ColorText else ColorTextDim
                )
            }
        }
    }
}

/**
 * InlineActivityExplorer: Main search interface for activities.
 */
@Composable
fun InlineActivityExplorer(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedActivity: StatsActivityOption?,
    onSelectActivity: (StatsActivityOption) -> Unit,
    availableActivities: List<StatsActivityOption>,
    onOpenFullList: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ColorSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = ColorTextMuted, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TitleNode.copy(color = ColorText),
                    cursorBrush = SolidColor(ColorExec),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (query.isEmpty() && selectedActivity == null) {
                            Text("Buscar actividad...", style = TitleNode, color = ColorTextMuted)
                        } else if (query.isEmpty() && selectedActivity != null) {
                            Text(selectedActivity.displayName, style = TitleNode, color = ColorText)
                        }
                        innerTextField()
                    }
                )
            }
        }

        // Compact list of results
        if (query.isNotEmpty()) {
            val displayResults = availableActivities.take(3)
            if (displayResults.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ColorSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        displayResults.forEach { activity ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectActivity(activity); onQueryChange("") }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(ColorPlan, CircleShape))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(activity.displayName, style = TitleNode.copy(fontSize = 13.sp), color = ColorText)
                            }
                        }
                        if (availableActivities.size > 3) {
                            TextButton(
                                onClick = onOpenFullList,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "VER TODOS LOS RESULTADOS (${availableActivities.size})",
                                    style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = ColorTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * NodePickerSheet: Activity exploration selector.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodePickerSheet(
    activities: List<StatsActivityOption>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSelect: (StatsActivityOption) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ColorBg,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text(
                "EXPLORAR HISTORIAL", 
                style = MetaMono.copy(letterSpacing = 2.sp), 
                color = ColorTextDim
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ColorSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, null, tint = ColorTextMuted, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        textStyle = TitleNode.copy(color = ColorText),
                        cursorBrush = SolidColor(ColorExec),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("Filtrar actividades...", style = TitleNode, color = ColorTextMuted)
                            }
                            innerTextField()
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(activities) { activity ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(activity) },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(ColorPlan, CircleShape))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(activity.displayName, style = TitleNode, color = ColorText)
                        }
                    }
                    HorizontalDivider(color = ColorBorder, thickness = 0.5.dp)
                }
            }
        }
    }
}
