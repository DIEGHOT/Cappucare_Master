package cl.duoc.cappucare.model

/** Errores de negocio tipados que viajan desde el repositorio hasta el ViewModel. */
sealed class AppError(val mensaje: String) : Exception(mensaje) {
    class CodigoDuplicado(codigo: String) : AppError("Ya existe un insumo con el código $codigo")
    class ProductoNoExiste : AppError("El insumo seleccionado no existe")
    class StockInsuficiente(val disponible: Int) : AppError("Stock insuficiente. Disponible: $disponible")
    class Desconocido(causa: Throwable?) : AppError(causa?.message ?: "Ocurrió un error inesperado")
}

fun Throwable.comoAppError(): AppError = this as? AppError ?: AppError.Desconocido(this)
