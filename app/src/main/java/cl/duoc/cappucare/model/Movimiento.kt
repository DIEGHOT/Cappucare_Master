package cl.duoc.cappucare.model

data class Movimiento(
    val id: Long = 0,
    val productoCodigo: String,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val fecha: Long,
    val motivo: String,
    val perfilResponsable: String,
    val productoNombre: String = "",
    val unidadMedida: String = "",
    val stockResultante: Int = 0
)

/** Movimiento enriquecido con datos del producto para mostrarlo en el historial. */
data class MovimientoUi(
    val movimiento: Movimiento,
    val nombreProducto: String,
    val unidadMedida: String
)

/** Indicador económico obtenido de la API externa (dólar, euro, UF). */
data class Indicador(
    val codigo: String,
    val nombre: String,
    val unidadMedida: String,
    val fecha: String,
    val valor: Double
)
