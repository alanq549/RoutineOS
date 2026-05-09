package com.alan.routineos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.NodeType
import com.alan.routineos.ui.theme.ColorBg
import com.alan.routineos.ui.theme.ColorBorder
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.ColorPending
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.ui.theme.ColorSurface2
import com.alan.routineos.ui.theme.ColorText
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.ColorTextMuted
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode
import com.alan.routineos.ui.viewmodel.NodeTypeManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeTypeManagerScreen(
    onBack: () -> Unit,
    viewModel: NodeTypeManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateTypeSheet by remember { mutableStateOf(false) }
    var showAddSchemaSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GESTIÓN DE TIPOS", style = MetaMono) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorBg)
            )
        },
        floatingActionButton = {
            if (uiState.selectedType == null) {
                FloatingActionButton(
                    onClick = { showCreateTypeSheet = true },
                    containerColor = ColorExec,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Type")
                }
            }
        },
        containerColor = ColorBg
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            if (uiState.selectedType == null) {
                TypeList(
                    nodeTypes = uiState.nodeTypes,
                    onSelect = viewModel::selectType,
                    onDelete = viewModel::deleteNodeType
                )
            } else {
                TypeDetail(
                    nodeType = uiState.selectedType!!,
                    schemas = uiState.schemasForSelectedType,
                    onBack = { viewModel.selectType(null) },
                    onAddSchema = { showAddSchemaSheet = true },
                    onDeleteSchema = viewModel::deleteSchema
                )
            }
        }

        if (showCreateTypeSheet) {
            CreateTypeSheet(
                onDismiss = { showCreateTypeSheet = false },
                onCreate = { name, hasMetrics ->
                    viewModel.createNodeType(name, hasMetrics)
                    showCreateTypeSheet = false
                }
            )
        }

        if (showAddSchemaSheet && uiState.selectedType != null) {
            AddSchemaSheet(
                onDismiss = { showAddSchemaSheet = false },
                onAdd = { name, label, type, default, unit ->
                    // Update ViewModel to handle new fields or just pass them
                    // Since I haven't updated the ViewModel method signature yet, I should probably do it or use a more generic way.
                    // Let's assume I'll update the ViewModel method.
                    viewModel.addSchemaFull(
                        uiState.selectedType!!.id,
                        name,
                        label,
                        type,
                        default,
                        unit
                    )
                    showAddSchemaSheet = false
                }
            )
        }
    }
}

@Composable
fun TypeList(
    nodeTypes: List<NodeType>,
    onSelect: (NodeType) -> Unit,
    onDelete: (NodeType) -> Unit
) {
    if (nodeTypes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay tipos definidos", color = ColorTextDim, style = TitleNode)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(nodeTypes) { type ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(type) },
                    colors = CardDefaults.cardColors(containerColor = ColorSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(type.name, style = TitleNode, color = ColorText)
                            if (type.hasMetricFields) {
                                Text("CON MÉTRICAS", style = MetaMono, color = ColorPending)
                            }
                        }
                        IconButton(onClick = { onDelete(type) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.Red.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypeDetail(
    nodeType: NodeType,
    schemas: List<com.alan.routineos.data.local.entities.NodeMetadataSchema>,
    onBack: () -> Unit,
    onAddSchema: () -> Unit,
    onDeleteSchema: (com.alan.routineos.data.local.entities.NodeMetadataSchema) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                nodeType.name.uppercase(),
                style = TitleNode,
                color = ColorExec,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onBack) {
                Text("CERRAR", style = MetaMono, color = ColorTextDim)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("CAMPOS DE SEGUIMIENTO", style = MetaMono, color = ColorTextDim)
        Spacer(modifier = Modifier.height(8.dp))

        if (!nodeType.hasMetricFields) {
            Text(
                "Este tipo no tiene métricas habilitadas.",
                color = ColorTextMuted,
                style = TitleNode
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(schemas) { schema ->
                    ListItem(
                        headlineContent = { Text(schema.fieldLabel, color = ColorText) },
                        supportingContent = {
                            Text(
                                "${schema.fieldType} • Default: ${schema.defaultValue ?: "-"} • Unidad: ${schema.unit ?: "-"}",
                                style = MetaMono,
                                color = ColorTextMuted
                            )
                        },
                        trailingContent = {
                            IconButton(onClick = { onDeleteSchema(schema) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = ColorTextMuted
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = ColorSurface)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Button(
                onClick = onAddSchema,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorSurface2),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AGREGAR CAMPO")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTypeSheet(onDismiss: () -> Unit, onCreate: (String, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var hasMetrics by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ColorSurface) {
        Column(modifier = Modifier
            .padding(16.dp)
            .padding(bottom = 32.dp)) {
            Text("NUEVO TIPO DE ACTIVIDAD", style = MetaMono, color = ColorText)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre (ej: Tarea, Reunión)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = hasMetrics,
                    onCheckedChange = { hasMetrics = it },
                    colors = CheckboxDefaults.colors(checkedColor = ColorExec)
                )
                Text(
                    "Habilitar métricas (seguimiento de datos)",
                    color = ColorText,
                    style = TitleNode
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onCreate(name, hasMetrics) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                enabled = name.isNotBlank()
            ) {
                Text("CREAR")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSchemaSheet(
    onDismiss: () -> Unit,
    onAdd: (String, String, FieldType, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(FieldType.NUMBER) }
    var defaultValue by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ColorSurface) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("NUEVO CAMPO", style = MetaMono, color = ColorText)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Etiqueta (ej: Peso kg)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("ID Técnico (ej: peso_kg)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("TIPO DE DATO", style = MetaMono, color = ColorTextDim)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FieldType.entries.take(4).forEach { fieldType ->
                    FilterChip(
                        selected = type == fieldType,
                        onClick = { type = fieldType },
                        label = { Text(fieldType.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ColorExec.copy(
                                alpha = 0.2f
                            ), selectedLabelColor = ColorExec
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = defaultValue,
                onValueChange = { defaultValue = it },
                label = { Text("Valor por defecto (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Unidad (ej: kg, min, rep)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    onAdd(
                        name,
                        label,
                        type,
                        defaultValue.ifBlank { null },
                        unit.ifBlank { null })
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                enabled = name.isNotBlank() && label.isNotBlank()
            ) {
                Text("AGREGAR")
            }
        }
    }
}
