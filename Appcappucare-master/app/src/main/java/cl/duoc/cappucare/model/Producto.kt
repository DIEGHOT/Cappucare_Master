package cl.duoc.cappucare.model

/** Modelo de dominio de un insumo médico. */
data class Producto(
    val codigo: String,
    val nombre: String,
    val categoria: String,
    val stockActual: Int,
    val stockMinimo: Int,
    val unidadMedida: String,
    val ubicacion: String,
    val fotoUri: String? = null
) {
    /** Verdadero cuando el stock llegó o bajó del nivel mínimo. */
    val stockCritico: Boolean get() = stockActual <= stockMinimo
}
