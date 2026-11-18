package cl.duoc.cappucare.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.cappucare.model.DataState
import cl.duoc.cappucare.model.Indicador
import cl.duoc.cappucare.ui.components.EstadoVacio
import cl.duoc.cappucare.ui.utils.columnasGrid
import cl.duoc.cappucare.ui.utils.formatearMonto
import cl.duoc.cappucare.ui.utils.tipoPantalla
import cl.duoc.cappucare.viewmodel.AppViewModelProvider
import cl.duoc.cappucare.viewmodel.IndicadoresViewModel

/** STATEFUL: indicadores económicos obtenidos de la API externa mindicador.cl. */
@Composable
fun MercadoScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: IndicadoresViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    MercadoContent(
        estado = estado,
        columnas = windowSizeClass.tipoPantalla().columnasGrid(),
        onActualizar = viewModel::cargar
    )
}

private enum class VistaMercado { CARGANDO, ERROR, LISTA }

/** STATELESS */
@Composable
fun MercadoContent(
    estado: DataState<List<Indicador>>,
    columnas: Int,
    onActualizar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vista = when (estado) {
        is DataState.Loading -> VistaMercado.CARGANDO
        is DataState.Error -> VistaMercado.ERROR
        is DataState.Success -> VistaMercado.LISTA
    }
    val indicadores = (estado as? DataState.Success)?.datos.orEmpty()
    val mensajeError = (estado as? DataState.Error)?.mensaje.orEmpty()

    Column(modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Indicadores económicos", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Fuente: mindicador.cl. Referencia para estimar el costo de reposición de insumos importados.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onActualizar, enabled = vista != VistaMercado.CARGANDO) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar indicadores")
            }
        }

        Crossfade(
            targetState = vista,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            label = "vistaMercado"
        ) { actual ->
            when (actual) {
                VistaMercado.CARGANDO -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                VistaMercado.ERROR -> EstadoVacio(
                    titulo = "No se pudieron cargar los indicadores",
                    descripcion = mensajeError
                )
                VistaMercado.LISTA -> LazyVerticalGrid(
                    columns = GridCells.Fixed(columnas),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(indicadores, key = { it.codigo }) { indicador -> IndicadorCard(indicador) }
                }
            }
        }
    }
}

@Composable
private fun IndicadorCard(indicador: Indicador) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(indicador.nombre, style = MaterialTheme.typography.titleSmall)
            Text(
                formatearMonto(indicador.valor),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${indicador.unidadMedida} · ${indicador.fecha.take(10)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
