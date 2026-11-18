package cl.duoc.cappucare.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.cappucare.model.Catalogos
import cl.duoc.cappucare.model.MovimientoFormState
import cl.duoc.cappucare.model.Producto
import cl.duoc.cappucare.model.TipoMovimiento
import cl.duoc.cappucare.ui.components.BotonAnimado
import cl.duoc.cappucare.ui.components.CappucareDropdown
import cl.duoc.cappucare.ui.components.CappucareTextField
import cl.duoc.cappucare.ui.components.ConfirmacionOverlay
import cl.duoc.cappucare.ui.components.MensajeError
import cl.duoc.cappucare.ui.utils.anchoMaximoFormulario
import cl.duoc.cappucare.ui.utils.formatearFecha
import cl.duoc.cappucare.ui.utils.tipoPantalla
import cl.duoc.cappucare.viewmodel.AppViewModelProvider
import cl.duoc.cappucare.viewmodel.MovimientoViewModel
import kotlinx.coroutines.delay

/** STATEFUL */
@Composable
fun MovimientoScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: MovimientoViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val productos by viewModel.productos.collectAsStateWithLifecycle()

    // La confirmación se cierra sola (o al tocar) tras mostrar la animación
    LaunchedEffect(state.resultado) {
        if (state.resultado != null) {
            delay(2200)
            viewModel.consumirResultado()
        }
    }

    Box(Modifier.fillMaxSize()) {
        MovimientoContent(
            state = state,
            productos = productos,
            anchoMaximo = windowSizeClass.tipoPantalla().anchoMaximoFormulario(),
            onProducto = viewModel::onProductoChange,
            onTipo = viewModel::onTipoChange,
            onCantidad = viewModel::onCantidadChange,
            onMotivo = viewModel::onMotivoChange,
            onPerfil = viewModel::onPerfilChange,
            onGuardar = viewModel::guardar
        )
        ConfirmacionOverlay(
            visible = state.resultado != null,
            mensaje = state.resultado?.mensaje ?: "",
            esAlerta = state.resultado?.stockCritico == true,
            onCerrar = viewModel::consumirResultado
        )
    }
}

/** STATELESS */
@Composable
fun MovimientoContent(
    state: MovimientoFormState,
    productos: List<Producto>,
    anchoMaximo: Dp,
    onProducto: (String) -> Unit,
    onTipo: (TipoMovimiento) -> Unit,
    onCantidad: (String) -> Unit,
    onMotivo: (String) -> Unit,
    onPerfil: (String) -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val seleccionado = productos.firstOrNull { it.codigo == state.productoCodigo }
    val ahora = remember { System.currentTimeMillis() }

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = anchoMaximo)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Registrar movimiento de bodega", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            CappucareDropdown(
                textoSeleccionado = seleccionado?.let { "${it.codigo} · ${it.nombre}" } ?: "",
                opciones = productos,
                etiqueta = { "${it.codigo} · ${it.nombre} (${it.stockActual} ${it.unidadMedida})" },
                onSeleccion = { onProducto(it.codigo) },
                label = "Insumo médico",
                error = state.errores.producto,
                leadingIcon = Icons.Filled.Inventory2
            )

            AnimatedVisibility(
                visible = seleccionado != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (seleccionado != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (seleccionado.stockCritico) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .animateContentSize()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                "Stock actual: ${seleccionado.stockActual} ${seleccionado.unidadMedida}  ·  Mínimo: ${seleccionado.stockMinimo}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text("Ubicación: ${seleccionado.ubicacion}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Text("Tipo de movimiento", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoMovimiento.values().forEach { tipo ->
                    FilterChip(
                        selected = state.tipo == tipo,
                        onClick = { onTipo(tipo) },
                        label = { Text(tipo.etiquetaCorta) },
                        leadingIcon = if (state.tipo == tipo) {
                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            CappucareTextField(
                value = state.cantidad, onValueChange = onCantidad,
                label = "Cantidad", error = state.errores.cantidad,
                leadingIcon = Icons.Filled.Numbers, keyboardType = KeyboardType.Number
            )
            CappucareTextField(
                value = state.motivo, onValueChange = onMotivo,
                label = if (state.tipo == TipoMovimiento.INGRESO) "Motivo (ej: recepción de proveedor)" else "Motivo (ej: venta factura 123)",
                error = state.errores.motivo, leadingIcon = Icons.Filled.EditNote
            )
            CappucareDropdown(
                textoSeleccionado = state.perfil,
                opciones = Catalogos.perfiles, etiqueta = { it },
                onSeleccion = onPerfil,
                label = "Perfil responsable", error = state.errores.perfil,
                leadingIcon = Icons.Filled.Person
            )
            Text(
                "Fecha y hora: ${formatearFecha(ahora)} (se registra automáticamente)",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (state.errorGeneral != null) MensajeError(state.errorGeneral)

            Spacer(Modifier.height(8.dp))
            BotonAnimado(
                texto = "Registrar movimiento",
                icono = Icons.Filled.Save,
                onClick = onGuardar,
                cargando = state.guardando,
                exito = state.resultado != null
            )
        }
    }
}
