package com.alan.routineos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.BgDark
import com.alan.routineos.ui.theme.NeonEmerald
import com.alan.routineos.ui.theme.TextSecondary

@Composable
fun UserMenu(
    userName: String?,
    onLogout: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        // El disparador: Icono de 3 barras neón
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = NeonEmerald,
                modifier = Modifier.size(24.dp)
            )
        }

        // El menú con estilo Glass
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(BgDark.copy(alpha = 0.9f)) // Casi opaco para legibilidad
                .border(1.dp, NeonEmerald.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 4.dp)
        ) {
            // Opción: Perfil (Próximamente)
            if (userName != null) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Hola $userName",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    },
                    onClick = {},
                    enabled = false
                )
            }

            // Opción: Configuración (Próximamente)
            DropdownMenuItem(
                text = {
                    Text(
                        "SETTINGS // BEYOND",
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.5f)
                    )
                },
                onClick = { /* Próximamente */ },
                enabled = false
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color.White.copy(alpha = 0.1f)
            )

            // Opción: Cerrar Sesión (Activa)
            DropdownMenuItem(
                text = {
                    Text(
                        "CERRAR SESIÓN",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                },
                onClick = {
                    expanded = false
                    onLogout()
                },
                leadingIcon = {
                    // Puedes añadir un icono pequeño aquí si quieres
                }
            )
        }
    }
}