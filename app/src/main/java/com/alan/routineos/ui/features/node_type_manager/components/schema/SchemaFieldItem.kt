package com.alan.routineos.ui.features.node_type_manager.components.schema

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.ui.features.node_type_manager.internal.colors
import com.alan.routineos.ui.features.node_type_manager.internal.shortLabel
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode

@Composable
fun SchemaFieldItem(schema: NodeMetadataSchema, onDelete: () -> Unit) {
    val (bgColor, txtColor) = schema.fieldType.colors()

    Surface(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 2.dp)
            .fillMaxWidth(),
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2A2A2A))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // pastilla de tipo
            Surface(color = bgColor, shape = RoundedCornerShape(5.dp)) {
                Text(
                    schema.fieldType.shortLabel(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                    color = txtColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            // nombre
            Text(
                schema.fieldName,
                modifier = Modifier.weight(1f),
                style = TitleNode.copy(fontSize = 12.sp),
                color = Color(0xFFE0E0E0)
            )

            // metadata contextual
            val hint = buildString {
                schema.unit?.let { append(it) }
                when (schema.fieldType) {
                    FieldType.BOOLEAN -> schema.defaultValue?.let {
                        append(" · def: ${if (it == "true") "Sí" else "No"}")
                    }
                    else -> schema.defaultValue?.let { append(" · def: $it") }
                }
            }
            if (hint.isNotEmpty()) {
                Text(
                    text = hint,
                    style = TitleNode.copy(fontSize = 10.sp),
                    color = Color(0xFF444444),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            // botón eliminar
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(5.dp))
                    .border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(5.dp))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close, null,
                    tint = Color(0xFF884444),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
