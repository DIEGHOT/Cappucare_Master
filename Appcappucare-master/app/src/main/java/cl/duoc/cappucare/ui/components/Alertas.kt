package cl.duoc.cappucare.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.cappucare.ui.theme.ColorIngreso

/** Banner animado que aparece/desaparece según haya insumos con stock crítico. */
@Composable
fun AlertaStockBanner(cantidad: Int, modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "bannerPulso")
    val alphaIcono by transicion.animateFloat(
        initialValue = 1f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "alphaIcono"
    )
    AnimatedVisibility(
        visible = cantidad > 0,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Warning, contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.alpha(alphaIcono)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (cantidad == 1) "1 insumo con stock crítico" else "$cantidad insumos con stock crítico",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Overlay de confirmación: check con rebote al guardar, o triángulo de alerta pulsante
 * si el stock quedó en nivel crítico. Se cierra al tocarlo o desde el estado del ViewModel.
 */
@Composable
fun ConfirmacionOverlay(
    visible: Boolean,
    mensaje: String,
    esAlerta: Boolean,
    modifier: Modifier = Modifier,
    onCerrar: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        var animar by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { animar = true }
        val escala by animateFloatAsState(
            targetValue = if (animar) 1f else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "escalaIcono"
        )
        val pulso = rememberInfiniteTransition(label = "pulsoAlerta")
        val factor by pulso.animateFloat(
            initialValue = 1f, targetValue = if (esAlerta) 1.15f else 1f,
            animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
            label = "factorPulso"
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onCerrar),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Registro guardado",
                        tint = ColorIngreso,
                        modifier = Modifier
                            .size(84.dp)
                            .scale(escala)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        mensaje,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    AnimatedVisibility(visible = esAlerta, enter = scaleIn() + fadeIn(), exit = scaleOut()) {
                        Row(
                            Modifier.padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning, contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.scale(factor)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "¡Stock crítico! Se envió una notificación",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
