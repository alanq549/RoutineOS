package com.alan.routineos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.BgDark
import com.alan.routineos.ui.theme.NeonEmerald
import kotlinx.coroutines.delay

@Composable
fun NotificationOverlay(
    message: String?,
    onDismiss: () -> Unit
) {
    // Manejamos la visibilidad interna basada en si el mensaje existe
    val isVisible = message != null

    LaunchedEffect(message) {
        if (message != null) {
            delay(3500) // Un poco más de tiempo para mensajes largos
            onDismiss()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 60.dp)
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    // Borde Neon muy fino
                    .border(
                        1.dp,
                        Brush.verticalGradient(listOf(NeonEmerald, Color.Transparent)),
                        RoundedCornerShape(12.dp)
                    )
                    // Fondo Glass Dark
                    .background(BgDark.copy(alpha = 0.9f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Indicador de estado (punto parpadeante o fijo)
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(NeonEmerald, RoundedCornerShape(50.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SYSTEM MESSAGE",
                        color = NeonEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message ?: "",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace, // Estilo código/terminal
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}