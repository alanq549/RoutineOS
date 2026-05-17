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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.ui.theme.*

/**
 * MetricItem: Integrated typographic metric.
 * Focused on human readability and longitudinal evolution.
 */
@Composable
fun MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = ColorText,
    trend: String? = null
) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        Text(
            text = label.uppercase(),
            style = MetaMono.copy(fontSize = 10.sp, letterSpacing = 1.sp),
            color = ColorTextDim
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                style = TitleNode.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                color = color
            )
            if (trend != null) {
                Text(
                    text = trend,
                    style = MetaMono.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = ColorExec,
                    modifier = Modifier.padding(bottom = 8.dp)
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
 * NodePickerSheet: Activity exploration selector.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodePickerSheet(
    nodes: List<Node>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSelect: (Node) -> Unit
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
                items(nodes) { node ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(node) },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(ColorPlan, CircleShape))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(node.name, style = TitleNode, color = ColorText)
                        }
                    }
                    HorizontalDivider(color = ColorBorder, thickness = 0.5.dp)
                }
            }
        }
    }
}
