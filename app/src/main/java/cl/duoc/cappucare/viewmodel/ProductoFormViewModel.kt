package cl.duoc.cappucare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.cappucare.model.AppError
import cl.duoc.cappucare.model.Producto
import cl.duoc.cappucare.model.ProductoFormErrores
import cl.duoc.cappucare.model.ProductoFormState
import cl.duoc.cappucare.model.comoAppError
import cl.duoc.cappucare.notification.StockNotifier
import cl.duoc.cappucare.repository.CappucareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * TODA la validación del formulario vive en este archivo. La UI solo observa [state]
 * y pinta los errores en isError / supportingText.
 */
class ProductoFormViewModel(
    private val repository: CappucareRepository,
    private val notificador: StockNotifier
) : ViewModel() {

    private val _state = MutableStateFlow(ProductoFormState())
    val state: StateFlow<ProductoFormState> = _state.asStateFlow()

    private var claveCargada: String? = SIN_CARGAR

    /** Inicializa el formulario (nuevo = null, edición = código). Idempotente ante rotaciones. */
    fun cargar(codigo: String?) {
        val clave = codigo ?: NUEVO
        if (claveCargada == clave) return
        claveCargada = clave
        if (codigo == null) {
            _state.value = ProductoFormState()
            return
        }
        viewModelScope.launch {
            repository.obtenerProducto(codigo)?.let { _state.value = it.comoFormulario() }
        }
    }

    /** Resultado del escáner: si el código ya existe se carga para edición; si no, solo se completa el campo. */
    fun onCodigoEscaneado(valor: String) {
        val codigo = valor.trim()
        viewModelScope.launch {
            val existente = repository.obtenerProducto(codigo.uppercase())
            if (existente != null) {
                _state.value = existente.comoFormulario()
            } else {
                _state.update {
                    it.copy(
                        codigo = codigo, esEdicion = false,
                        errores = it.errores.copy(codigo = null), errorGeneral = null
                    )
                }
            }
        }
    }

    fun onCodigoChange(v: String) = _state.update { it.copy(codigo = v, errores = it.errores.copy(codigo = null), errorGeneral = null) }
    fun onNombreChange(v: String) = _state.update { it.copy(nombre = v, errores = it.errores.copy(nombre = null)) }
    fun onDescripcionChange(v: String) = _state.update { it.copy(descripcion = v, errores = it.errores.copy(descripcion = null)) }
    fun onCategoriaChange(v: String) = _state.update { it.copy(categoria = v, errores = it.errores.copy(categoria = null)) }
    fun onStockActualChange(v: String) = _state.update { it.copy(stockActual = v.filter(Char::isDigit), errores = it.errores.copy(stockActual = null)) }
    fun onStockMinimoChange(v: String) = _state.update { it.copy(stockMinimo = v.filter(Char::isDigit), errores = it.errores.copy(stockMinimo = null)) }
    fun onUnidadChange(v: String) = _state.update { it.copy(unidadMedida = v, errores = it.errores.copy(unidadMedida = null)) }
    fun onUbicacionChange(v: String) = _state.update { it.copy(ubicacion = v, errores = it.errores.copy(ubicacion = null)) }
    fun onFotoChange(uri: String?) = _state.update { it.copy(fotoUri = uri, errorGeneral = null) }
    fun mostrarError(mensaje: String) = _state.update { it.copy(errorGeneral = mensaje) }

    fun guardar() {
        val s = _state.value
        if (s.guardando || s.guardadoExitoso) return

        val errores = validarProducto(s)
        if (errores.hayErrores) {
            _state.update { it.copy(errores = errores) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(guardando = true, errorGeneral = null) }
            val producto = Producto(
                codigo = s.codigo.trim().uppercase(),
                nombre = s.nombre.trim(),
                descripcion = s.descripcion.trim(),
                categoria = s.categoria,
                stockActual = s.stockActual.toInt(),
                stockMinimo = s.stockMinimo.toInt(),
                unidadMedida = s.unidadMedida,
                ubicacion = s.ubicacion.trim(),
                fotoUri = s.fotoUri
            )
            repository.guardarProducto(producto, s.esEdicion)
                .onSuccess { guardado ->
                    if (guardado.stockCritico) notificador.notificarStockCritico(guardado)
                    _state.update {
                        it.copy(guardando = false, guardadoExitoso = true, alertaStock = guardado.stockCritico)
                    }
                }
                .onFailure { e ->
                    val error = e.comoAppError()
                    _state.update {
                        if (error is AppError.CodigoDuplicado) {
                            it.copy(guardando = false, errores = it.errores.copy(codigo = error.mensaje))
                        } else {
                            it.copy(guardando = false, errorGeneral = error.mensaje)
                        }
                    }
                }
        }
    }

    private fun Producto.comoFormulario() = ProductoFormState(
        codigo = codigo,
        nombre = nombre,
        descripcion = descripcion,
        categoria = categoria,
        stockActual = stockActual.toString(),
        stockMinimo = stockMinimo.toString(),
        unidadMedida = unidadMedida,
        ubicacion = ubicacion,
        fotoUri = fotoUri,
        esEdicion = true
    )

    private companion object {
        const val SIN_CARGAR = "__sin_cargar__"
        const val NUEVO = "__nuevo__"
    }
}

private const val MAX_STOCK = 1_000_000
private const val MAX_DESCRIPCION = 200
private val REGEX_CODIGO = Regex("^[A-Za-z0-9-]{3,20}$")

/** Reglas de validación del formulario de insumos (función pura, probada con tests unitarios). */
internal fun validarProducto(s: ProductoFormState): ProductoFormErrores {
    val codigo = s.codigo.trim()
    val stock = s.stockActual.trim().toIntOrNull()
    val minimo = s.stockMinimo.trim().toIntOrNull()
    return ProductoFormErrores(
        codigo = when {
            codigo.isEmpty() -> "El código es obligatorio"
            !REGEX_CODIGO.matches(codigo) -> "Use 3 a 20 caracteres: letras, números o guiones"
            else -> null
        },
        nombre = when {
            s.nombre.isBlank() -> "El nombre es obligatorio"
            s.nombre.trim().length < 3 -> "El nombre debe tener al menos 3 caracteres"
            else -> null
        },
        descripcion = if (s.descripcion.trim().length > MAX_DESCRIPCION) "La descripción no puede superar los $MAX_DESCRIPCION caracteres" else null,
        categoria = if (s.categoria.isBlank()) "Seleccione una categoría" else null,
        stockActual = when {
            s.stockActual.isBlank() -> "Ingrese el stock actual"
            stock == null -> "Debe ser un número entero"
            stock < 0 -> "El stock no puede ser negativo"
            stock > MAX_STOCK -> "El stock no puede superar $MAX_STOCK"
            else -> null
        },
        stockMinimo = when {
            s.stockMinimo.isBlank() -> "Ingrese el nivel mínimo"
            minimo == null -> "Debe ser un número entero"
            minimo < 1 -> "El nivel mínimo debe ser mayor a 0"
            minimo > MAX_STOCK -> "El nivel mínimo no puede superar $MAX_STOCK"
            else -> null
        },
        unidadMedida = if (s.unidadMedida.isBlank()) "Seleccione la unidad de medida" else null,
        ubicacion = when {
            s.ubicacion.isBlank() -> "La ubicación en bodega es obligatoria"
            s.ubicacion.trim().length < 2 -> "La ubicación es demasiado corta"
            else -> null
        }
    )
}
