package cl.duoc.cappucare.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.cappucare.model.Producto
import coil.compose.AsyncImage

/** Tarjeta de insumo (stateless). Pulsa y se tiñe de rojo cuando el stock es crítico. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoCard(
    producto: Producto,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val critico = producto.stockCritico
    val transicion = rememberInfiniteTransition(label = "pulso")
    val alphaPulso by transicion.animateFloat(
        initialValue = 1f,
        targetValue = if (critico) 0.3f else 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "alphaPulso"
    )
    val progreso by animateFloatAsState(
        targetValue = (producto.stockActual.toFloat() / (producto.stockMinimo * 3f).coerceAtLeast(1f)).coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "progresoStock"
    )

    Card(
        onClick = onEditar,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (critico) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (producto.fotoUri != null) {
                    AsyncImage(
                        model = producto.fotoUri,
                        contentDescription = "Foto de ${producto.nombre}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(72.dp)
                    )
                } else {
                    Icon(
                        Icons.Filled.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${producto.codigo} · ${producto.categoria}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("Bodega: ${producto.ubicacion}", style = MaterialTheme.typography.bodySmall)
                Text("Estado: ${producto.estado.etiqueta}", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (critico) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Stock crítico",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(18.dp)
                                .alpha(alphaPulso)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        "${producto.stockActual} ${producto.unidadMedida} (mín. ${producto.stockMinimo})",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (critico) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (critico) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(0.dp)) {
                IconButton(onClick = onEditar) { Icon(Icons.Filled.Edit, contentDescription = "Editar ${producto.nombre}") }
                IconButton(onClick = onEliminar) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar ${producto.nombre}") }
            }
        }
    }
}
