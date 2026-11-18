package cl.duoc.cappucare.model

/* ---------- Inventario ---------- */
data class InventarioUiState(
    val productos: List<Producto> = emptyList(),
    val totalProductos: Int = 0,
    val totalCriticos: Int = 0,
    val consulta: String = "",
    val soloCriticos: Boolean = false,
    val cargando: Boolean = true,
    val sincronizando: Boolean = false,
    val error: String? = null
)

/* ---------- Formulario de insumo ---------- */
data class ProductoFormErrores(
    val codigo: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null,
    val stockActual: String? = null,
    val stockMinimo: String? = null,
    val unidadMedida: String? = null,
    val ubicacion: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(codigo, nombre, descripcion, categoria, stockActual, stockMinimo, unidadMedida, ubicacion)
            .any { it != null }
}

data class ProductoFormState(
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val stockActual: String = "",
    val stockMinimo: String = "",
    val unidadMedida: String = "",
    val ubicacion: String = "",
    val fotoUri: String? = null,
    val errores: ProductoFormErrores = ProductoFormErrores(),
    val errorGeneral: String? = null,
    val esEdicion: Boolean = false,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val alertaStock: Boolean = false
)

/* ---------- Formulario de movimiento ---------- */
data class MovimientoFormErrores(
    val producto: String? = null,
    val cantidad: String? = null,
    val motivo: String? = null,
    val perfil: String? = null
) {
    val hayErrores: Boolean get() = listOf(producto, cantidad, motivo, perfil).any { it != null }
}

data class ResultadoMovimiento(val mensaje: String, val stockCritico: Boolean)

data class MovimientoFormState(
    val productoCodigo: String = "",
    val tipo: TipoMovimiento = TipoMovimiento.INGRESO,
    val cantidad: String = "",
    val motivo: String = "",
    val perfil: String = "",
    val errores: MovimientoFormErrores = MovimientoFormErrores(),
    val errorGeneral: String? = null,
    val guardando: Boolean = false,
    val resultado: ResultadoMovimiento? = null
)
