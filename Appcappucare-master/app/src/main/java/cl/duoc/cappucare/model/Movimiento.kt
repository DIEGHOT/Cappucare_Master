package cl.duoc.cappucare.model

data class Movimiento(
    val id: Long = 0,
    val productoCodigo: String,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val fecha: Long,
    val motivo: String,
    val perfilResponsable: String
)

/** Movimiento enriquecido con datos del producto para mostrarlo en el historial. */
data class MovimientoUi(
    val movimiento: Movimiento,
    val nombreProducto: String,
    val unidadMedida: String
)
