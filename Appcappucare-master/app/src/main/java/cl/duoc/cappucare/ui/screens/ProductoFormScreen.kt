package cl.duoc.cappucare.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.cappucare.model.Catalogos
import cl.duoc.cappucare.model.ProductoFormState
import cl.duoc.cappucare.ui.components.BotonAnimado
import cl.duoc.cappucare.ui.components.CappucareDropdown
import cl.duoc.cappucare.ui.components.CappucareTextField
import cl.duoc.cappucare.ui.components.ConfirmacionOverlay
import cl.duoc.cappucare.ui.components.EscanerCodigo
import cl.duoc.cappucare.ui.components.ImagePickerSection
import cl.duoc.cappucare.ui.components.MensajeError
import cl.duoc.cappucare.ui.utils.ImageUtils
import cl.duoc.cappucare.ui.utils.anchoMaximoFormulario
import cl.duoc.cappucare.ui.utils.tipoPantalla
import cl.duoc.cappucare.viewmodel.AppViewModelProvider
import cl.duoc.cappucare.viewmodel.ProductoFormViewModel
import kotlinx.coroutines.delay

/** Callbacks agrupados para el formulario (State Hoisting). */
class ProductoFormAcciones(
    val onCodigo: (String) -> Unit,
    val onNombre: (String) -> Unit,
    val onCategoria: (String) -> Unit,
    val onStockActual: (String) -> Unit,
    val onStockMinimo: (String) -> Unit,
    val onUnidad: (String) -> Unit,
    val onUbicacion: (String) -> Unit,
    val onEscanear: () -> Unit,
    val onTomarFoto: () -> Unit,
    val onElegirGaleria: () -> Unit,
    val onGuardar: () -> Unit,
    val onCancelar: () -> Unit
)

/** STATEFUL: ViewModel + launchers de Cámara/Galería (TakePicture / GetContent) + escáner. */
@Composable
fun ProductoFormScreen(
    codigo: String?,
    windowSizeClass: WindowSizeClass,
    onVolver: () -> Unit,
    viewModel: ProductoFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val contexto = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var uriPendiente by rememberSaveable { mutableStateOf<Uri?>(null) }
    var mostrarEscaner by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(codigo) { viewModel.cargar(codigo) }

    // Tras guardar: se muestra la animación de confirmación y se vuelve al inventario
    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            delay(1600)
            onVolver()
        }
    }

    BackHandler(enabled = mostrarEscaner) { mostrarEscaner = false }

    // Cámara: TakePicture escribe la foto en la URI del FileProvider
    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val destino = uriPendiente
        if (exito && destino != null) {
            viewModel.onFotoChange(destino.toString())
        } else {
            ImageUtils.eliminar(contexto, destino?.toString())
        }
        uriPendiente = null
    }
    fun lanzarCamara() {
        val destino = ImageUtils.crearUriParaCamara(contexto)
        uriPendiente = destino
        tomarFoto.launch(destino)
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) lanzarCamara() else viewModel.mostrarError("Se necesita el permiso de cámara para tomar la foto")
    }
    // Galería: GetContent entrega una URI temporal que se copia al almacenamiento interno
    val elegirImagen = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val copia = ImageUtils.copiarDesdeGaleria(contexto, uri)
            if (copia != null) {
                viewModel.onFotoChange(copia.toString())
            } else {
                viewModel.mostrarError("No se pudo leer la imagen seleccionada")
            }
        }
    }

    val acciones = ProductoFormAcciones(
        onCodigo = viewModel::onCodigoChange,
        onNombre = viewModel::onNombreChange,
        onCategoria = viewModel::onCategoriaChange,
        onStockActual = viewModel::onStockActualChange,
        onStockMinimo = viewModel::onStockMinimoChange,
        onUnidad = viewModel::onUnidadChange,
        onUbicacion = viewModel::onUbicacionChange,
        onEscanear = { mostrarEscaner = true },
        onTomarFoto = {
            val concedido = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
            if (concedido) lanzarCamara() else permisoCamara.launch(Manifest.permission.CAMERA)
        },
        onElegirGaleria = { elegirImagen.launch("image/*") },
        onGuardar = viewModel::guardar,
        onCancelar = onVolver
    )

    Box(Modifier.fillMaxSize()) {
        ProductoFormContent(
            state = state,
            acciones = acciones,
            anchoMaximo = windowSizeClass.tipoPantalla().anchoMaximoFormulario()
        )
        AnimatedVisibility(
            visible = mostrarEscaner,
            enter = fadeIn() + slideInVertically { it / 3 },
            exit = fadeOut() + slideOutVertically { it / 3 }
        ) {
            EscanerCodigo(
                onCodigoLeido = { leido ->
                    viewModel.onCodigoEscaneado(leido)
                    mostrarEscaner = false
                },
                onCerrar = { mostrarEscaner = false }
            )
        }
        ConfirmacionOverlay(
            visible = state.guardadoExitoso,
            mensaje = if (state.esEdicion) "Insumo actualizado" else "Insumo registrado",
            esAlerta = state.alertaStock
        )
    }
}

/** STATELESS: solo pinta el estado y emite eventos. Cero lógica de validación. */
@Composable
fun ProductoFormContent(
    state: ProductoFormState,
    acciones: ProductoFormAcciones,
    anchoMaximo: Dp,
    modifier: Modifier = Modifier
) {
    val e = state.errores
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = anchoMaximo)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ImagePickerSection(
                fotoUri = state.fotoUri,
                onTomarFoto = acciones.onTomarFoto,
                onElegirGaleria = acciones.onElegirGaleria
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                CappucareTextField(
                    value = state.codigo, onValueChange = acciones.onCodigo,
                    label = "Código / ID del insumo", error = e.codigo,
                    leadingIcon = Icons.Filled.Tag, enabled = !state.esEdicion,
                    ayuda = if (state.esEdicion) "El código no se puede modificar" else null,
                    modifier = Modifier.weight(1f)
                )
                FilledTonalIconButton(
                    onClick = acciones.onEscanear,
                    enabled = !state.esEdicion,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = "Escanear código de barras")
                }
            }
            CappucareTextField(
                value = state.nombre, onValueChange = acciones.onNombre,
                label = "Nombre", error = e.nombre, leadingIcon = Icons.Filled.Inventory2
            )
            CappucareDropdown(
                textoSeleccionado = state.categoria,
                opciones = Catalogos.categorias, etiqueta = { it },
                onSeleccion = acciones.onCategoria,
                label = "Categoría", error = e.categoria, leadingIcon = Icons.Filled.Category
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CappucareTextField(
                    value = state.stockActual, onValueChange = acciones.onStockActual,
                    label = "Stock actual", error = e.stockActual,
                    leadingIcon = Icons.Filled.Numbers, keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                CappucareTextField(
                    value = state.stockMinimo, onValueChange = acciones.onStockMinimo,
                    label = "Nivel mínimo", error = e.stockMinimo,
                    leadingIcon = Icons.Filled.Warning, keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }
            CappucareDropdown(
                textoSeleccionado = state.unidadMedida,
                opciones = Catalogos.unidades, etiqueta = { it },
                onSeleccion = acciones.onUnidad,
                label = "Unidad de medida", error = e.unidadMedida, leadingIcon = Icons.Filled.Straighten
            )
            CappucareTextField(
                value = state.ubicacion, onValueChange = acciones.onUbicacion,
                label = "Ubicación en bodega (ej: Pasillo 3 - Estante B)", error = e.ubicacion,
                leadingIcon = Icons.Filled.Place, imeAction = ImeAction.Done
            )

            if (state.errorGeneral != null) {
                MensajeError(state.errorGeneral)
            }

            Spacer(Modifier.height(8.dp))
            BotonAnimado(
                texto = if (state.esEdicion) "Guardar cambios" else "Registrar insumo",
                icono = Icons.Filled.Save,
                onClick = acciones.onGuardar,
                cargando = state.guardando,
                exito = state.guardadoExitoso
            )
            OutlinedButton(onClick = acciones.onCancelar, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }
}
