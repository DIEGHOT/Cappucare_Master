package cl.duoc.cappucare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.cappucare.model.DataState
import cl.duoc.cappucare.model.InventarioUiState
import cl.duoc.cappucare.model.comoAppError
import cl.duoc.cappucare.model.comoDataState
import cl.duoc.cappucare.repository.CappucareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventarioViewModel(private val repository: CappucareRepository) : ViewModel() {

    private val consulta = MutableStateFlow("")
    private val soloCriticos = MutableStateFlow(false)

    private val sincronizando = MutableStateFlow(false)

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    val uiState: StateFlow<InventarioUiState> = combine(
        repository.productos.comoDataState(), consulta, soloCriticos, sincronizando
    ) { datos, q, criticos, enCurso ->
        when (datos) {
            is DataState.Loading -> InventarioUiState(consulta = q, soloCriticos = criticos, cargando = true, sincronizando = enCurso)
            is DataState.Error -> InventarioUiState(
                consulta = q, soloCriticos = criticos, cargando = false,
                sincronizando = enCurso, error = datos.mensaje
            )
            is DataState.Success -> {
                val filtro = q.trim()
                val productos = datos.datos
                InventarioUiState(
                    productos = productos.filter { p ->
                        (!criticos || p.stockCritico) &&
                            (filtro.isEmpty() ||
                                p.nombre.contains(filtro, ignoreCase = true) ||
                                p.codigo.contains(filtro, ignoreCase = true) ||
                                p.categoria.contains(filtro, ignoreCase = true))
                    },
                    totalProductos = productos.size,
                    totalCriticos = productos.count { it.stockCritico },
                    consulta = q,
                    soloCriticos = criticos,
                    cargando = false,
                    sincronizando = enCurso
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InventarioUiState())

    init {
        sincronizar()
    }

    /** Descarga los datos de los microservicios; si falla se siguen mostrando los guardados en el dispositivo. */
    fun sincronizar() {
        if (sincronizando.value) return
        viewModelScope.launch {
            sincronizando.value = true
            repository.sincronizar().onFailure {
                _mensaje.value = "${it.comoAppError().mensaje}. Mostrando los datos guardados en el dispositivo."
            }
            sincronizando.value = false
        }
    }

    fun onConsultaChange(valor: String) {
        consulta.value = valor
    }

    fun onToggleCriticos() {
        soloCriticos.value = !soloCriticos.value
    }

    fun eliminar(codigo: String, nombre: String) {
        viewModelScope.launch {
            repository.eliminarProducto(codigo)
                .onSuccess { _mensaje.value = "«$nombre» eliminado del inventario" }
                .onFailure { _mensaje.value = it.comoAppError().mensaje }
        }
    }

    fun consumirMensaje() {
        _mensaje.value = null
    }
}
