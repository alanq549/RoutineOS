package com.alan.routineos.ui.features.node_type_manager.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode

@Composable
fun TypeChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) Color(0xFF0D1F3A) else Color(0xFF1A1A1A),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isSelected) Color(0xFF1565C0) else Color(0xFF2A2A2A))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = TitleNode.copy(fontSize = 10.sp),
            color = if (isSelected) Color(0xFF42A5F5) else Color(0xFF555555)
        )
    }
}

@Composable
fun SchemaFieldItem(schema: NodeMetadataSchema, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp).fillMaxWidth(),
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2A2A2A))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (bgColor, txtColor) = when(schema.fieldType) {
                FieldType.NUMBER -> Color(0xFF0D1F3A) to Color(0xFF42A5F5)
                FieldType.TEXT -> Color(0xFF0D2010) to Color(0xFF4CAF50)
                FieldType.BOOLEAN -> Color(0xFF1A1208) to Color(0xFFFF9800)
                FieldType.SELECT -> Color(0xFF1A0D2A) to Color(0xFFCE93D8)
                else -> Color(0xFF1A1A1A) to Color(0xFFE0E0E0)
            }
            
            Surface(color = bgColor, shape = RoundedCornerShape(5.dp)) {
                Text(
                    schema.fieldType.name.take(3).uppercase(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                    color = txtColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(schema.fieldName, modifier = Modifier.weight(1f), style = TitleNode.copy(fontSize = 12.sp), color = Color(0xFFE0E0E0))
            
            if (schema.unit != null || schema.defaultValue != null) {
                Text(
                    text = listOfNotNull(schema.unit, schema.defaultValue?.let { "def: $it" }).joinToString(" \u00b7 "),
                    style = TitleNode.copy(fontSize = 10.sp),
                    color = Color(0xFF444444),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Box(
                modifier = Modifier.size(24.dp).background(Color(0xFF1A1A1A), RoundedCornerShape(5.dp)).border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(5.dp)).clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, null, tint = Color(0xFF553333), modifier = Modifier.size(12.dp))
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
    var name by remember { mutableStateOf("peso") }
    var type by remember { mutableStateOf(FieldType.NUMBER) }
    var defaultValue by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        dragHandle = {
            Box(modifier = Modifier.padding(vertical = 14.dp).size(36.dp, 3.dp).background(Color(0xFF333333), RoundedCornerShape(2.dp)))
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("NUEVO CAMPO", style = MetaMono.copy(fontSize = 11.sp, letterSpacing = 0.1.sp), color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(14.dp))
            
            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                Text("NOMBRE DEL CAMPO", style = MetaMono.copy(fontSize = 9.sp), color = Color(0xFF555555))
                Spacer(modifier = Modifier.height(4.dp))
                ManagerTextField(
                    value = name,
                    onValueChange = { name = it },
                    isFocused = true
                )
            }

            Text("TIPO DE DATO", style = MetaMono.copy(fontSize = 9.sp), color = Color(0xFF555555))
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(FieldType.TEXT, FieldType.NUMBER, FieldType.BOOLEAN, FieldType.SELECT).forEach { fieldType ->
                    val isSel = type == fieldType
                    Surface(
                        modifier = Modifier.clickable { type = fieldType },
                        color = if (isSel) Color(0xFF0D1F3A) else Color(0xFF1A1A1A),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isSel) Color(0xFF1565C0) else Color(0xFF2A2A2A))
                    ) {
                        Text(
                            fieldType.name.take(4),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = TitleNode.copy(fontSize = 10.sp),
                            color = if (isSel) Color(0xFF42A5F5) else Color(0xFF555555)
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("VALOR POR DEFECTO", style = MetaMono.copy(fontSize = 9.sp), color = Color(0xFF555555))
                    Spacer(modifier = Modifier.height(4.dp))
                    ManagerTextField(
                        value = defaultValue,
                        onValueChange = { defaultValue = it },
                        placeholder = "\u2014"
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("UNIDAD", style = MetaMono.copy(fontSize = 9.sp), color = Color(0xFF555555))
                    Spacer(modifier = Modifier.height(4.dp))
                    ManagerTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        isFocused = true
                    )
                }
            }

            Button(
                onClick = { onAdd(name, name, type, defaultValue.ifBlank { null }, unit.ifBlank { null }) },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                shape = RoundedCornerShape(8.dp),
                enabled = name.isNotBlank()
            ) {
                Text("AGREGAR CAMPO", style = TitleNode.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium))
            }
        }
    }
}

@Composable
fun ManagerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isFocused: Boolean = false
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
            .border(
                width = 0.5.dp,
                color = if (isFocused) Color(0xFF1565C0) else Color(0xFF2A2A2A),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        textStyle = TitleNode.copy(color = Color(0xFFE0E0E0), fontSize = 13.sp),
        cursorBrush = SolidColor(Color(0xFF1565C0)),
        decorationBox = { inner ->
            if (value.isEmpty()) Text(placeholder, color = Color(0xFF444444), style = TitleNode.copy(fontSize = 13.sp))
            inner()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTypeSheet(onDismiss: () -> Unit, onCreate: (String, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E)
    ) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text("NUEVO TIPO DE ACTIVIDAD", style = MetaMono, color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                Text("NOMBRE", style = MetaMono.copy(fontSize = 9.sp), color = Color(0xFF555555))
                Spacer(modifier = Modifier.height(4.dp))
                ManagerTextField(
                    value = name,
                    onValueChange = { name = it }
                )
            }
            
            Button(
                onClick = { onCreate(name, true) },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                shape = RoundedCornerShape(8.dp),
                enabled = name.isNotBlank()
            ) {
                Text("CREAR TIPO", style = TitleNode.copy(fontWeight = FontWeight.Medium))
            }
        }
    }
}
