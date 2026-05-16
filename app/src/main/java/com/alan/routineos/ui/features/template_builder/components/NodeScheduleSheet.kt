package com.alan.routineos.ui.features.template_builder.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeSchedule
import com.alan.routineos.ui.theme.ColorBorder
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeScheduleSheet(
    node: Node,
    currentSchedules: List<NodeSchedule>,
    onDismiss: () -> Unit,
    onSave: (List<NodeSchedule>) -> Unit,
    onToggleSequential: (Boolean) -> Unit
) {
    var schedules by remember { mutableStateOf(currentSchedules) }
    var isSequential by remember { mutableStateOf(node.isSequential) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "HORARIOS: ${node.name.uppercase()}",
                style = MetaMono,
                color = Color(0xFF7D8590)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Sequential Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ejecución secuencial", style = TitleNode, color = Color.White)
                    Text(
                        "Si está activo, el nodo aparece siempre en orden. Si está apagado, solo aparece los días con horario.",
                        style = MetaMono.copy(fontSize = 10.sp),
                        color = Color(0xFF7D8590)
                    )
                }
                Switch(
                    checked = isSequential,
                    onCheckedChange = { 
                        isSequential = it
                        onToggleSequential(it)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = ColorExec)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isSequential) {
                Text("REPETICIÓN SEMANAL", style = MetaMono, color = Color(0xFF7D8590))
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(schedules) { schedule ->
                        ScheduleRow(
                            schedule = schedule,
                            onDelete = { schedules = schedules - schedule }
                        )
                    }
                    
                    item {
                        AddScheduleButton {
                            // Default: Monday at 08:00
                            val newSchedule = NodeSchedule(
                                nodeId = node.id,
                                dayOfWeek = 1,
                                startTime = "08:00",
                                endTime = "09:00"
                            )
                            schedules = schedules + newSchedule
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onSave(schedules) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CONFIRMAR HORARIOS", style = TitleNode.copy(color = Color.White))
            }
        }
    }
}

@Composable
private fun ScheduleRow(
    schedule: NodeSchedule,
    onDelete: () -> Unit
) {
    val days = listOf("L", "M", "M", "J", "V", "S", "D")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF161B22), RoundedCornerShape(8.dp))
            .border(0.5.dp, ColorBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(ColorExec.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                .border(0.5.dp, ColorExec, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = days.getOrElse(schedule.dayOfWeek - 1) { "?" },
                style = TitleNode.copy(fontSize = 12.sp),
                color = ColorExec
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = "${schedule.startTime} - ${schedule.endTime}",
            style = TitleNode,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, null, tint = Color(0xFFF85149), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun AddScheduleButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(48.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, null, tint = Color(0xFF7D8590), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Añadir horario", style = TitleNode.copy(fontSize = 12.sp), color = Color(0xFF7D8590))
        }
    }
}
