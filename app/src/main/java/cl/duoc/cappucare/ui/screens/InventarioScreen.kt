package cl.duoc.cappucare.ui.screens

import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.cappucare.model.InventarioUiState
import cl.duoc.cappucare.model.Producto
import cl.duoc.cappucare.ui.components.AlertaStockBanner
import cl.duoc.cappucare.ui.components.EstadoVacio
import cl.duoc.cappucare.ui.components.ProductoCard
import cl.duoc.cappucare.ui.utils.ImageUtils
import cl.duoc.cappucare.ui.utils.columnasGrid
import cl.duoc.cappucare.ui.utils.tipoPantalla
import cl.duoc.cappucare.viewmodel.AppViewModelProvider
import cl.duoc.cappucare.viewmodel.InventarioViewModel

/** STATEFUL: conecta el ViewModel con el estado local de UI (diálogo y snackbar). */
@Composable
fun InventarioScreen(
    windowSizeClass: WindowSizeClass,
    onNuevo: () -> Unit,
    onEditar: (String) -> Unit,
    viewModel: InventarioViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val contexto = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var porEliminar by remember { mutableStateOf<Producto?>(null) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.consumirMensaje()
        }
    }

    Box(Modifier.fillMaxSize()) {
        InventarioContent(
            state = state,
            columnas = windowSizeClass.tipoPantalla().columnasGrid(),
            onConsultaChange = viewModel::onConsultaChange,
            onToggleCriticos = viewModel::onToggleCriticos,
            onSincronizar = viewModel::sincronizar,
            onNuevo = onNuevo,
            onEditar = onEditar,
            onEliminar = { porEliminar = it }
        )
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 8.dp)
        )
    }

    porEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
            title = { Text("Eliminar insumo") },
            text = { Text("¿Eliminar «${producto.nombre}»? Sus movimientos pasados se conservan en el historial.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(producto.codigo, producto.nombre)
                    ImageUtils.eliminar(contexto, producto.fotoUri)
                    porEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

private enum class VistaInventario { CARGANDO, ERROR, VACIO, LISTA }

/** STATELESS: solo recibe estado y callbacks. */
@Composable
fun InventarioContent(
    state: InventarioUiState,
    columnas: Int,
    onConsultaChange: (String) -> Unit,
    onToggleCriticos: () -> Unit,
    onSincronizar: () -> Unit,
    onNuevo: () -> Unit,
    onEditar: (String) -> Unit,
    onEliminar: (Producto) -> Unit,
    modifier: Modifier = Modifier
) {
    val vista = when {
        state.cargando -> VistaInventario.CARGANDO
        state.error != null -> VistaInventario.ERROR
        state.productos.isEmpty() -> VistaInventario.VACIO
        else -> VistaInventario.LISTA
    }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            if (state.sincronizando) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            OutlinedTextField(
                value = state.consulta,
                onValueChange = onConsultaChange,
                singleLine = true,
                placeholder = { Text("Buscar por nombre, código o categoría") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (state.consulta.isNotEmpty()) {
                    {
                        IconButton(onClick = { onConsultaChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda")
                        }
                    }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            Row(
                Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = state.soloCriticos,
                    onClick = onToggleCriticos,
                    label = { Text("Stock crítico") },
                    leadingIcon = {
                        Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "${state.productos.size} de ${state.totalProductos} insumos",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onSincronizar, enabled = !state.sincronizando) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Sincronizar con el servidor")
                }
            }
            AlertaStockBanner(
                cantidad = state.totalCriticos,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Crossfade(
                targetState = vista,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "vistaInventario"
            ) { actual ->
                when (actual) {
                    VistaInventario.CARGANDO -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    VistaInventario.ERROR -> EstadoVacio(
                        titulo = "No se pudo cargar el inventario",
                        descripcion = state.error ?: "Intenta nuevamente"
                    )
                    VistaInventario.VACIO -> EstadoVacio(
                        titulo = if (state.totalProductos == 0) "Aún no hay insumos" else "Sin resultados",
                        descripcion = if (state.totalProductos == 0) {
                            "Registra tu primer insumo con el botón «Nuevo insumo»."
                        } else {
                            "Prueba con otra búsqueda o quita el filtro."
                        }
                    )
                    VistaInventario.LISTA -> LazyVerticalGrid(
                        columns = GridCells.Fixed(columnas),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.productos, key = { it.codigo }) { producto ->
                            ProductoCard(
                                producto = producto,
                                onEditar = { onEditar(producto.codigo) },
                                onEliminar = { onEliminar(producto) }
                            )
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onNuevo,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Nuevo insumo") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}
