package com.alan.routineos.ui.features.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.ui.theme.*

@Composable
fun MetricCard(
    label: String, 
    value: String, 
    modifier: Modifier = Modifier,
    color: Color = ColorExec
) {
    Surface(
        modifier = modifier,
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MetaMono, color = ColorTextDim)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MonoTimer.copy(fontSize = 24.sp), color = color)
        }
    }
}

@Composable
fun SparklineChart(
    dataPoints: List<NodeFieldValue>,
    modifier: Modifier = Modifier
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

        drawPath(
            path = path,
            color = ColorExec,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw points
        points.forEach { point ->
            drawCircle(
                color = ColorExec,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = ColorBg,
                radius = 2.dp.toPx(),
                center = point
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodePickerSheet(
    nodes: List<Node>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSelect: (Node) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ColorSurface) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text("SELECCIONAR ACTIVIDAD", style = MetaMono, color = ColorText)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(modifier = Modifier.height(300.dp)) {
                items(nodes) { node ->
                    ListItem(
                        headlineContent = { Text(node.name, color = ColorText) },
                        modifier = Modifier.clickable { onSelect(node) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}
