package com.alan.routineos.ui.features.template_builder.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.TitleNode

@Composable
fun NodeItem(
    name: String,
    depth: Int,
    hasChildren: Boolean = false,
    meta: String? = null,
    hasSchedules: Boolean = false,
    onAddClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onScheduleClick: () -> Unit = {}
) {
    val bgColor = when(depth) {
        0 -> Color(0xFF1E1E1E)
        1 -> Color(0xFF181818)
        else -> Color(0xFF161616)
    }
    
    Surface(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 2.5.dp)
            .padding(start = (depth * 30).dp)
            .fillMaxWidth(),
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, if(depth == 0) Color(0xFF2A2A2A) else Color(0xFF222222))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(22.dp),
                color = if (depth == 0) Color(0xFF0D1F3A) else Color(0xFF1A1A1A),
                shape = RoundedCornerShape(5.dp),
                border = if (depth > 0) androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2A2A2A)) else null
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (depth == 0) Icons.Default.Folder else Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (depth == 0) Color(0xFF1565C0) else Color(0xFF555555),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                style = TitleNode.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                color = Color(0xFFE0E0E0)
            )
            
            if (meta != null) {
                Text(
                    text = meta,
                    style = TitleNode.copy(fontSize = 10.sp),
                    color = Color(0xFF444444),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Schedule Button
                Surface(
                    modifier = Modifier.size(24.dp),
                    color = if (hasSchedules) Color(0xFF0D1F3A) else Color(0xFF1A1A1A),
                    shape = RoundedCornerShape(5.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, if (hasSchedules) Color(0xFF1565C0) else Color(0xFF2A2A2A)),
                    onClick = onScheduleClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Schedule,
                            null,
                            tint = if (hasSchedules) Color(0xFF42A5F5) else Color(0xFF555555),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                if (depth == 0) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF1A1A1A),
                        shape = RoundedCornerShape(5.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2A2A2A)),
                        onClick = onAddClick
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, null, tint = Color(0xFF1565C0), modifier = Modifier.size(12.dp))
                        }
                    }
                }
                Surface(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFF1A1A1A),
                    shape = RoundedCornerShape(5.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2A2A2A)),
                    onClick = onDeleteClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Delete, null, tint = Color(0xFF553333), modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}
