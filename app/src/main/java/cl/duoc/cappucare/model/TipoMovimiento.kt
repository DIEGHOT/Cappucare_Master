package cl.duoc.cappucare.model

enum class TipoMovimiento(
    val etiqueta: String,
    val etiquetaCorta: String,
    val esSalida: Boolean
) {
    INGRESO("Ingreso por recepción", "Ingreso", false),
    SALIDA_VENTA("Salida por venta", "Venta", true),
    SALIDA_MERMA("Salida por merma", "Merma", true);

    companion object {
        fun desde(nombre: String): TipoMovimiento =
            values().firstOrNull { it.name == nombre } ?: INGRESO
    }
}
