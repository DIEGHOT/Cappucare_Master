package cl.duoc.cappucare.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.cappucare.model.DataState
import cl.duoc.cappucare.model.MovimientoUi
import cl.duoc.cappucare.ui.components.EstadoVacio
import cl.duoc.cappucare.ui.theme.ColorIngreso
import cl.duoc.cappucare.ui.theme.ColorSalida
import cl.duoc.cappucare.ui.utils.columnasGrid
import cl.duoc.cappucare.ui.utils.formatearFecha
import cl.duoc.cappucare.ui.utils.tipoPantalla
import cl.duoc.cappucare.viewmodel.AppViewModelProvider
import cl.duoc.cappucare.viewmodel.MovimientoViewModel

/** STATEFUL */
@Composable
fun HistorialScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: MovimientoViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val historial by viewModel.historial.collectAsStateWithLifecycle()
    HistorialContent(historial, windowSizeClass.tipoPantalla().columnasGrid())
}

private enum class VistaHistorial { CARGANDO, ERROR, VACIO, LISTA }

/** STATELESS */
@Composable
fun HistorialContent(
    estado: DataState<List<MovimientoUi>>,
    columnas: Int,
    modifier: Modifier = Modifier
) {
    val vista = when (estado) {
        is DataState.Loading -> VistaHistorial.CARGANDO
        is DataState.Error -> VistaHistorial.ERROR
        is DataState.Success -> if (estado.datos.isEmpty()) VistaHistorial.VACIO else VistaHistorial.LISTA
    }
    val movimientos = (estado as? DataState.Success)?.datos.orEmpty()
    val mensajeError = (estado as? DataState.Error)?.mensaje.orEmpty()

    Crossfade(targetState = vista, modifier = modifier.fillMaxSize(), label = "vistaHistorial") { actual ->
        when (actual) {
            VistaHistorial.CARGANDO -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            VistaHistorial.ERROR -> EstadoVacio(
                titulo = "No se pudo cargar el historial",
                descripcion = mensajeError
            )
            VistaHistorial.VACIO -> EstadoVacio(
                titulo = "Sin movimientos",
                descripcion = "Los ingresos, ventas y mermas aparecerán aquí con su trazabilidad."
            )
            VistaHistorial.LISTA -> LazyVerticalGrid(
                columns = GridCells.Fixed(columnas),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(movimientos, key = { it.movimiento.id }) { item -> MovimientoItem(item) }
            }
        }
    }
}

@Composable
private fun MovimientoItem(item: MovimientoUi) {
    val m = item.movimiento
    val color: Color = if (m.tipo.esSalida) ColorSalida else ColorIngreso
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (m.tipo.esSalida) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    contentDescription = m.tipo.etiqueta,
                    tint = color
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.nombreProducto, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${m.tipo.etiqueta}: ${if (m.tipo.esSalida) "-" else "+"}${m.cantidad} ${item.unidadMedida}",
                    color = color,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text("Motivo: ${m.motivo}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "${m.perfilResponsable} · ${formatearFecha(m.fecha)}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
