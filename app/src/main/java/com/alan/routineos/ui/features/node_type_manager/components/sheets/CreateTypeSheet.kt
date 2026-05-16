package com.alan.routineos.ui.features.node_type_manager.components.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.node_type_manager.components.schema.FieldLabel
import com.alan.routineos.ui.features.node_type_manager.components.schema.ManagerTextField
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTypeSheet(onDismiss: () -> Unit, onCreate: (String, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 36.dp)
        ) {
            Text("NUEVO TIPO DE ACTIVIDAD", style = MetaMono, color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Define un conjunto de campos reutilizables, por ejemplo: \"Ejercicio\" con series, reps y peso.",
                style = TitleNode.copy(fontSize = 11.sp),
                color = Color(0xFF555555)
            )
            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Nombre")
            Spacer(modifier = Modifier.height(4.dp))
            ManagerTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "ej: Ejercicio, Materia, Hábito…",
                isFocused = true
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onCreate(name, true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0),
                    disabledContainerColor = Color(0xFF1A1A1A)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = name.isNotBlank()
            ) {
                Text(
                    "CREAR TIPO",
                    style = TitleNode.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}
