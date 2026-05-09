package com.alan.routineos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.core.util.DateUtils
import com.alan.routineos.data.local.entities.RoutineTemplate
import com.alan.routineos.ui.theme.*
import com.alan.routineos.ui.viewmodel.PlannerViewModel
import com.alan.routineos.ui.viewmodel.ScheduleWithTemplate
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddScheduleSheet by remember { mutableStateOf(false) }
    var showAddExceptionSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(ColorSurface).statusBarsPadding()) {
                CenterAlignedTopAppBar(
                    title = { Text("PLANIFICADOR", style = MetaMono) },
                    actions = {
                        IconButton(onClick = { showAddExceptionSheet = true }) {
                            Icon(Icons.Default.EventBusy, contentDescription = "Excepciones", tint = ColorPending)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = ColorSurface)
                )
                WeekStrip(
                    days = uiState.weekDays,
                    selectedDate = uiState.selectedDate,
                    onDateSelect = viewModel::selectDate
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddScheduleSheet = true },
                containerColor = ColorExec,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule")
            }
        },
        containerColor = ColorBg
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            DayDetailSection(
                selectedDate = uiState.selectedDate,
                schedules = uiState.activeSchedules,
                instance = uiState.instanceForSelectedDate
            )
        }

        if (showAddScheduleSheet) {
            AddScheduleSheet(
                templates = uiState.templates,
                onDismiss = { showAddScheduleSheet = false },
                onConfirm = { templateId, startTime ->
                    val weekday = DateUtils.getDayOfWeek(Date(uiState.selectedDate))
                    viewModel.addSchedule(templateId, weekday, startTime)
                    showAddScheduleSheet = false
                }
            )
        }

        if (showAddExceptionSheet) {
            AddExceptionSheet(
                onDismiss = { showAddExceptionSheet = false },
                onConfirm = { label, from, to ->
                    viewModel.addException(label, from, to)
                    showAddExceptionSheet = false
                }
            )
        }
    }
}

@Composable
fun WeekStrip(
    days: List<Long>,
    selectedDate: Long,
    onDateSelect: (Long) -> Unit
) {
    val dayFormat = SimpleDateFormat("E", Locale.getDefault())
    val dateFormat = SimpleDateFormat("d", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        days.forEach { date ->
            val isSelected = date == selectedDate
            val dayName = dayFormat.format(Date(date)).take(1).uppercase()
            val dayNum = dateFormat.format(Date(date))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onDateSelect(date) }
                    .background(
                        if (isSelected) ColorExec.copy(alpha = 0.1f) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(dayName, style = MetaMono, color = if (isSelected) ColorExec else ColorTextDim)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    dayNum, 
                    style = TitleNode, 
                    color = if (isSelected) ColorExec else ColorText,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun DayDetailSection(
    selectedDate: Long,
    schedules: List<ScheduleWithTemplate>,
    instance: com.alan.routineos.data.local.entities.DayInstance?
) {
    val fullDateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale.getDefault())
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = fullDateFormat.format(Date(selectedDate)).uppercase(),
            style = MetaMono,
            color = ColorTextDim
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (schedules.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("Sin rutinas programadas", color = ColorTextMuted, style = TitleNode)
            }
        } else {
            schedules.forEach { item ->
                ScheduleCard(item)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (instance != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                color = ColorExec.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorExec.copy(alpha = 0.3f))
            ) {
                Text(
                    "INSTANCIA GENERADA", 
                    style = MetaMono, 
                    color = ColorExec, 
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ScheduleCard(item: ScheduleWithTemplate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.template?.name ?: "Sin nombre", style = TitleNode, color = ColorText)
                Text(item.schedule.startTime ?: "--:--", style = MonoTimer.copy(fontSize = 18.sp), color = ColorTextDim)
            }
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = try { Color(item.template?.colorHex?.toColorInt() ?: 0xFF3FB950.toInt()) } 
                                catch (e: Exception) { ColorExec }, 
                        shape = CircleShape
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleSheet(
    templates: List<RoutineTemplate>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var selectedTemplateId by remember { mutableStateOf(templates.firstOrNull()?.id ?: "") }
    var startTime by remember { mutableStateOf("08:00") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ColorSurface) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text("PROGRAMAR RUTINA", style = MetaMono, color = ColorText)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("SELECCIONAR PLANTILLA", style = MetaMono, color = ColorTextDim)
            LazyColumn(modifier = Modifier.height(150.dp)) {
                items(templates) { template ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTemplateId = template.id }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTemplateId == template.id,
                            onClick = { selectedTemplateId = template.id },
                            colors = RadioButtonDefaults.colors(selectedColor = ColorExec)
                        )
                        Text(template.name, color = ColorText, style = TitleNode)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("HORA DE INICIO", style = MetaMono, color = ColorTextDim)
            OutlinedTextField(
                value = startTime,
                onValueChange = { startTime = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("08:00") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onConfirm(selectedTemplateId, startTime) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                enabled = selectedTemplateId.isNotBlank()
            ) {
                Text("CONFIRMAR")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExceptionSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, Long, Long) -> Unit
) {
    var label by remember { mutableStateOf("") }
    val today = DateUtils.getStartOfDay()

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ColorSurface) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text("NUEVA EXCEPCIÓN", style = MetaMono, color = ColorText)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Ej: Vacaciones, Exámenes") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Se aplicará desde hoy por 7 días (Simplificado)", color = ColorTextDim, style = TitleNode)

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { 
                    val to = today + (7 * 24 * 60 * 60 * 1000L)
                    onConfirm(label, today, to) 
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorPending),
                enabled = label.isNotBlank()
            ) {
                Text("BLOQUEAR GENERACIÓN")
            }
        }
    }
}
