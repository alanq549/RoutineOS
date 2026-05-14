package com.alan.routineos.ui.features.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.RoutineTemplate
import com.alan.routineos.ui.theme.*

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        color = ColorSurface,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (query.isEmpty()) "Buscar actividad..." else query,
                style = TitleNode.copy(fontSize = 16.sp),
                color = if (query.isEmpty()) ColorTextDim else ColorText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TemplateCard(
    template: RoutineTemplate,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = try {
                                Color(android.graphics.Color.parseColor(template.colorHex))
                            } catch (e: Exception) {
                                ColorPlan
                            },
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(template.name, style = TitleNode.copy(fontSize = 16.sp), color = ColorText)
                    Text(
                        "6 materias · metadatos: aula, profesor",
                        style = MetaMono,
                        color = ColorTextDim
                    )
                }
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = ColorPlan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val days = listOf("L", "Ma", "Mi", "J", "V", "S", "D")
                days.forEachIndexed { index, day ->
                    val isActive = index < 5
                    DayBadge(day, isActive)
                }
            }
        }
    }
}

@Composable
fun DayBadge(day: String, isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp, 24.dp)
            .background(
                color = if (isActive) ColorPlan.copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 1.dp,
                color = if (isActive) ColorPlan else ColorBorder,
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            style = MetaMono.copy(fontSize = 10.sp),
            color = if (isActive) ColorPlan else ColorTextDim
        )
    }
}
