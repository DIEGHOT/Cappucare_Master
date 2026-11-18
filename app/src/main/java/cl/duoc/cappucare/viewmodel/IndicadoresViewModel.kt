package cl.duoc.cappucare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.cappucare.model.DataState
import cl.duoc.cappucare.model.Indicador
import cl.duoc.cappucare.model.comoAppError
import cl.duoc.cappucare.repository.CappucareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Indicadores económicos (API externa) usados como referencia para decidir compras de reposición. */
class IndicadoresViewModel(private val repository: CappucareRepository) : ViewModel() {

    private val _estado = MutableStateFlow<DataState<List<Indicador>>>(DataState.Loading)
    val estado: StateFlow<DataState<List<Indicador>>> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = DataState.Loading
            repository.obtenerIndicadores()
                .onSuccess { _estado.value = DataState.Success(it) }
                .onFailure { _estado.value = DataState.Error(it.comoAppError().mensaje) }
        }
    }
}
