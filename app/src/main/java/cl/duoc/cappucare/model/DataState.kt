package cl.duoc.cappucare.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/** Estado de una carga de datos: permite a la UI distinguir cargando / éxito / error. */
sealed interface DataState<out T> {
    data object Loading : DataState<Nothing>
    data class Success<T>(val datos: T) : DataState<T>
    data class Error(val mensaje: String) : DataState<Nothing>
}

fun <T> Flow<T>.comoDataState(): Flow<DataState<T>> =
    map<T, DataState<T>> { DataState.Success(it) }
        .onStart { emit(DataState.Loading) }
        .catch { emit(DataState.Error(it.message ?: "No se pudieron leer los datos")) }
