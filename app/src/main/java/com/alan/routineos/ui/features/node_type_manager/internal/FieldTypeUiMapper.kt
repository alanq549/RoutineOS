package com.alan.routineos.ui.features.node_type_manager.internal

import androidx.compose.ui.graphics.Color
import com.alan.routineos.data.local.entities.FieldType

/** Nombre legible para el usuario */
fun FieldType.displayName() = when (this) {
    FieldType.TEXT    -> "Texto"
    FieldType.NUMBER  -> "Número"
    FieldType.BOOLEAN -> "Sí / No"
    FieldType.SELECT  -> "Lista"
    else              -> name
}

/** Colores por tipo (fondo, texto) */
fun FieldType.colors() = when (this) {
    FieldType.NUMBER  -> Color(0xFF0D1F3A) to Color(0xFF42A5F5)
    FieldType.TEXT    -> Color(0xFF0D2010) to Color(0xFF4CAF50)
    FieldType.BOOLEAN -> Color(0xFF1A1208) to Color(0xFFFF9800)
    FieldType.SELECT  -> Color(0xFF1A0D2A) to Color(0xFFCE93D8)
    else              -> Color(0xFF1A1A1A) to Color(0xFFE0E0E0)
}

/** Abreviatura para la pastilla pequeña */
fun FieldType.shortLabel() = when (this) {
    FieldType.TEXT    -> "TXT"
    FieldType.NUMBER  -> "NUM"
    FieldType.BOOLEAN -> "S/N"
    FieldType.SELECT  -> "LST"
    else              -> name.take(3)
}
