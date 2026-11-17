package cl.duoc.cappucare.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.cappucare.CappucareApplication
import cl.duoc.cappucare.repository.AppContainer

/** Fábrica única de ViewModels: inyecta el repositorio y el notificador desde [AppContainer]. */
object AppViewModelProvider {

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            InventarioViewModel(contenedor().repository)
        }
        initializer {
            ProductoFormViewModel(contenedor().repository, contenedor().notificador)
        }
        initializer {
            MovimientoViewModel(contenedor().repository, contenedor().notificador)
        }
    }

    private fun CreationExtras.contenedor(): AppContainer {
        val aplicacion = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CappucareApplication
        return aplicacion.container
    }
}
