package cl.duoc.cappucare.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.cappucare.ui.theme.ColorIngreso

private enum class EstadoBoton { NORMAL, CARGANDO, EXITO }

/**
 * Botón con retroalimentación animada: rebota al presionarlo, muestra un progreso mientras guarda
 * y pasa a verde con un check cuando la operación termina con éxito.
 */
@Composable
fun BotonAnimado(
    texto: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cargando: Boolean = false,
    exito: Boolean = false
) {
    val fuenteInteraccion = remember { MutableInteractionSource() }
    val presionado by fuenteInteraccion.collectIsPressedAsState()
    val escala by animateFloatAsState(
        targetValue = if (presionado) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "escalaBoton"
    )
    val colorFondo by animateColorAsState(
        targetValue = if (exito) ColorIngreso else MaterialTheme.colorScheme.primary,
        label = "colorBoton"
    )
    val estado = when {
        cargando -> EstadoBoton.CARGANDO
        exito -> EstadoBoton.EXITO
        else -> EstadoBoton.NORMAL
    }

    Button(
        onClick = { if (estado == EstadoBoton.NORMAL) onClick() },
        interactionSource = fuenteInteraccion,
        colors = ButtonDefaults.buttonColors(containerColor = colorFondo, contentColor = Color.White),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
    ) {
        Crossfade(targetState = estado, label = "iconoBoton") { actual ->
            when (actual) {
                EstadoBoton.CARGANDO -> CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current
                )
                EstadoBoton.EXITO -> Icon(Icons.Filled.Check, contentDescription = null)
                EstadoBoton.NORMAL -> Icon(icono, contentDescription = null)
            }
        }
        Spacer(Modifier.size(8.dp))
        Text(texto)
    }
}
