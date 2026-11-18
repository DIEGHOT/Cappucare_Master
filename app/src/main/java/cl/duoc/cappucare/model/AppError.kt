package cl.duoc.cappucare.model

/** Errores de negocio y de red tipados que viajan desde el repositorio hasta el ViewModel. */
sealed class AppError(val mensaje: String) : Exception(mensaje) {
    class CodigoDuplicado(codigo: String) : AppError("Ya existe un insumo con el código $codigo")
    class ProductoNoExiste : AppError("El insumo seleccionado no existe")
    class StockInsuficiente(detalle: String) : AppError(detalle)
    class Conflicto(detalle: String) : AppError(detalle)
    class SinConexion : AppError(
        "No se pudo conectar con el servidor. Verifica tu conexión y que los microservicios estén activos"
    )
    class Servidor(detalle: String) : AppError(detalle)
    class Desconocido(causa: Throwable?) : AppError(causa?.message ?: "Ocurrió un error inesperado")
}

fun Throwable.comoAppError(): AppError = this as? AppError ?: AppError.Desconocido(this)
