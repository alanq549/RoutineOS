package com.alan.routineos.ui.features.template_builder.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.NodeSchedule
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.ColorSurface
import java.util.Locale
import java.util.UUID

private data class ScheduleGroupUi(
    val id: String = UUID.randomUUID().toString(),
    val days: Set<Int>,
    val startTime: String,
    val endTime: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeScheduleSheet(
    node: Node,
    currentSchedules: List<NodeSchedule>,
    onDismiss: () -> Unit,
    onToggleSequential: (Boolean) -> Unit,
    onSave: (List<NodeSchedule>) -> Unit
) {
    val context = LocalContext.current
    
    // Group incoming schedules by time to allow multi-day selection per row
    val initialGroups = currentSchedules.groupBy { it.startTime to it.endTime }
        .map { (time, list) ->
            ScheduleGroupUi(
                days = list.map { it.dayOfWeek }.toSet(),
                startTime = time.first,
                endTime = time.second
            )
        }

    var groups by remember { mutableStateOf(initialGroups) }

    fun showPicker(currentTime: String, onSelected: (String) -> Unit) {
        val parts = currentTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(context, { _, hour, min ->
            onSelected(String.format(Locale.US, "%02d:%02d", hour, min))
        }, h, m, true).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ColorSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Text(
                text = "HORARIO: ${node.name.uppercase()}",
                style = MetaMono.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(20.dp))

            // Option: Sequential (No fixed time)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (groups.isEmpty()) ColorExec.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(10.dp))
                    .border(0.5.dp, if (groups.isEmpty()) ColorExec else Color(0xFF2A2A2A), RoundedCornerShape(10.dp))
                    .clickable { 
                        groups = emptyList()
                        onToggleSequential(true)
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = groups.isEmpty(), 
                    onClick = { 
                        groups = emptyList()
                        onToggleSequential(true)
                    }, 
                    colors = RadioButtonDefaults.colors(selectedColor = ColorExec)
                )
                Column {
                    Text("Secuencial", style = TitleNode.copy(fontSize = 14.sp), color = Color.White)
                    Text("Sigue al bloque anterior (automático)", style = MetaMono.copy(fontSize = 10.sp), color = Color(0xFF666666))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("DÍAS Y HORARIOS ESPECÍFICOS", style = MetaMono.copy(fontSize = 9.sp), color = Color(0xFF444444))
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(groups, key = { it.id }) { group ->
                    ScheduleGroupRow(
                        days = group.days,
                        startTime = group.startTime,
                        endTime = group.endTime,
                        onDelete = { groups = groups.filter { it.id != group.id } },
                        onDaysChange = { newDays ->
                            groups = groups.map { if (it.id == group.id) it.copy(days = newDays) else it }
                        },
                        onStartTimeClick = { showPicker(group.startTime) { t -> 
                            groups = groups.map { if (it.id == group.id) it.copy(startTime = t) else it }
                        }},
                        onEndTimeClick = { showPicker(group.endTime) { t -> 
                            groups = groups.map { if (it.id == group.id) it.copy(endTime = t) else it }
                        }}
                    )
                }
                
                item {
                    Button(
                        onClick = {
                            groups = groups + ScheduleGroupUi(days = emptySet(), startTime = "08:00", endTime = "09:00")
                            onToggleSequential(false)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar nuevo horario", style = TitleNode.copy(fontSize = 12.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { 
                    // Flatten groups back to List<NodeSchedule>
                    val result = groups.flatMap { group ->
                        group.days.map { day ->
                            NodeSchedule(
                                nodeId = node.id,
                                dayOfWeek = day,
                                startTime = group.startTime,
                                endTime = group.endTime
                            )
                        }
                    }
                    onSave(result) 
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("CONFIRMAR", style = TitleNode.copy(color = Color.White))
            }
        }
    }
}

@Composable
private fun ScheduleGroupRow(
    days: Set<Int>,
    startTime: String,
    endTime: String,
    onDelete: () -> Unit,
    onDaysChange: (Set<Int>) -> Unit,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161616), RoundedCornerShape(10.dp))
            .border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dayLabels = listOf("L", "M", "M", "J", "V", "S", "D")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                dayLabels.forEachIndexed { index, label ->
                    val dayNum = index + 1
                    val isSelected = days.contains(dayNum)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (isSelected) ColorExec else Color(0xFF222222), RoundedCornerShape(4.dp))
                            .clickable { 
                                val newDays = if (isSelected) days - dayNum else days + dayNum
                                onDaysChange(newDays)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, style = TitleNode.copy(fontSize = 10.sp), color = if (isSelected) Color.White else Color(0xFF666666))
                    }
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, tint = Color(0xFF553333), modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onStartTimeClick() }) {
                Text("INICIO", style = MetaMono.copy(fontSize = 7.sp), color = Color(0xFF555555))
                Text(startTime, style = TitleNode.copy(fontSize = 15.sp, letterSpacing = 1.sp), color = Color.White)
            }
            Text("-", color = Color(0xFF333333))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onEndTimeClick() }) {
                Text("FIN", style = MetaMono.copy(fontSize = 7.sp), color = Color(0xFF555555))
                Text(endTime, style = TitleNode.copy(fontSize = 15.sp, letterSpacing = 1.sp), color = Color.White)
            }
        }
    }
}
