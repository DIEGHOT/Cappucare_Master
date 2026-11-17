package cl.duoc.cappucare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.cappucare.model.DataState
import cl.duoc.cappucare.model.Movimiento
import cl.duoc.cappucare.model.MovimientoFormErrores
import cl.duoc.cappucare.model.MovimientoFormState
import cl.duoc.cappucare.model.MovimientoUi
import cl.duoc.cappucare.model.Producto
import cl.duoc.cappucare.model.ResultadoMovimiento
import cl.duoc.cappucare.model.TipoMovimiento
import cl.duoc.cappucare.model.comoAppError
import cl.duoc.cappucare.model.comoDataState
import cl.duoc.cappucare.notification.StockNotifier
import cl.duoc.cappucare.repository.CappucareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovimientoViewModel(
    private val repository: CappucareRepository,
    private val notificador: StockNotifier
) : ViewModel() {

    val productos: StateFlow<List<Producto>> = repository.productos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val historial: StateFlow<DataState<List<MovimientoUi>>> = combine(
        repository.movimientos, repository.productos
    ) { movimientos, productos ->
        val porCodigo = productos.associateBy { it.codigo }
        movimientos.map { m ->
            MovimientoUi(
                movimiento = m,
                nombreProducto = porCodigo[m.productoCodigo]?.nombre ?: m.productoCodigo,
                unidadMedida = porCodigo[m.productoCodigo]?.unidadMedida ?: ""
            )
        }
    }.comoDataState().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DataState.Loading)

    private val _state = MutableStateFlow(MovimientoFormState())
    val state: StateFlow<MovimientoFormState> = _state.asStateFlow()

    fun onProductoChange(codigo: String) = _state.update { it.copy(productoCodigo = codigo, errores = it.errores.copy(producto = null, cantidad = null)) }
    fun onTipoChange(tipo: TipoMovimiento) = _state.update { it.copy(tipo = tipo, errores = it.errores.copy(cantidad = null)) }
    fun onCantidadChange(v: String) = _state.update { it.copy(cantidad = v.filter(Char::isDigit), errores = it.errores.copy(cantidad = null)) }
    fun onMotivoChange(v: String) = _state.update { it.copy(motivo = v, errores = it.errores.copy(motivo = null)) }
    fun onPerfilChange(v: String) = _state.update { it.copy(perfil = v, errores = it.errores.copy(perfil = null)) }
    fun consumirResultado() = _state.update { it.copy(resultado = null) }

    fun guardar() {
        val s = _state.value
        if (s.guardando) return

        viewModelScope.launch {
            val producto = if (s.productoCodigo.isBlank()) null else repository.obtenerProducto(s.productoCodigo)
            val errores = validarMovimiento(s, producto)
            if (errores.hayErrores || producto == null) {
                _state.update { it.copy(errores = errores) }
                return@launch
            }

            _state.update { it.copy(guardando = true, errorGeneral = null) }
            val movimiento = Movimiento(
                productoCodigo = producto.codigo,
                tipo = s.tipo,
                cantidad = s.cantidad.toInt(),
                fecha = System.currentTimeMillis(),
                motivo = s.motivo.trim(),
                perfilResponsable = s.perfil
            )
            repository.registrarMovimiento(movimiento)
                .onSuccess { actualizado ->
                    if (actualizado.stockCritico) notificador.notificarStockCritico(actualizado)
                    _state.value = MovimientoFormState(
                        tipo = s.tipo,
                        perfil = s.perfil,
                        resultado = ResultadoMovimiento(
                            mensaje = "${s.tipo.etiquetaCorta} registrado.\nStock de ${actualizado.nombre}: " +
                                "${actualizado.stockActual} ${actualizado.unidadMedida}",
                            stockCritico = actualizado.stockCritico
                        )
                    )
                }
                .onFailure { e ->
                    _state.update { it.copy(guardando = false, errorGeneral = e.comoAppError().mensaje) }
                }
        }
    }
}

private const val MAX_CANTIDAD = 1_000_000

/** Reglas de validación del formulario de movimientos (función pura, probada con tests unitarios). */
internal fun validarMovimiento(s: MovimientoFormState, producto: Producto?): MovimientoFormErrores {
    val cantidad = s.cantidad.trim().toIntOrNull()
    return MovimientoFormErrores(
        producto = if (producto == null) "Seleccione un insumo" else null,
        cantidad = when {
            s.cantidad.isBlank() -> "Ingrese la cantidad"
            cantidad == null -> "Debe ser un número entero"
            cantidad <= 0 -> "La cantidad debe ser mayor a 0"
            cantidad > MAX_CANTIDAD -> "La cantidad no puede superar $MAX_CANTIDAD"
            s.tipo.esSalida && producto != null && cantidad > producto.stockActual ->
                "Stock insuficiente. Disponible: ${producto.stockActual} ${producto.unidadMedida}"
            else -> null
        },
        motivo = when {
            s.motivo.isBlank() -> "El motivo es obligatorio"
            s.motivo.trim().length < 3 -> "Describa el motivo (mínimo 3 caracteres)"
            else -> null
        },
        perfil = if (s.perfil.isBlank()) "Seleccione el perfil responsable" else null
    )
}
