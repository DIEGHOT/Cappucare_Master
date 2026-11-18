package cl.duoc.cappucare.model

/** Estado comercial de un insumo según sus existencias. */
enum class EstadoProducto(val etiqueta: String) {
    DISPONIBLE("Disponible"),
    STOCK_BAJO("Stock bajo"),
    AGOTADO("Agotado")
}

/** Modelo de dominio de un insumo médico. */
data class Producto(
    val codigo: String,
    val nombre: String,
    val descripcion: String = "",
    val categoria: String,
    val stockActual: Int,
    val stockMinimo: Int,
    val unidadMedida: String,
    val ubicacion: String,
    val fotoUri: String? = null
) {
    /** Verdadero cuando el stock llegó o bajó del nivel mínimo. */
    val stockCritico: Boolean get() = stockActual <= stockMinimo

    val estado: EstadoProducto
        get() = when {
            stockActual <= 0 -> EstadoProducto.AGOTADO
            stockCritico -> EstadoProducto.STOCK_BAJO
            else -> EstadoProducto.DISPONIBLE
        }
}
