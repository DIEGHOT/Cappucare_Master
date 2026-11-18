package cl.duoc.cappucare.data.remote

import com.google.gson.annotations.SerializedName

/** Insumo tal como lo envía y recibe productos-service. */
data class ProductoDto(
    val codigo: String,
    val nombre: String,
    val descripcion: String?,
    val categoria: String,
    val stockActual: Int,
    val stockMinimo: Int,
    val unidadMedida: String,
    val ubicacion: String,
    val fotoUri: String?
)

/** Datos que la app envía para registrar un movimiento (la fecha la asigna el servidor). */
data class MovimientoRequestDto(
    val productoCodigo: String,
    val tipo: String,
    val cantidad: Int,
    val motivo: String,
    val perfilResponsable: String
)

/** Movimiento tal como lo entrega movimientos-service. */
data class MovimientoDto(
    val id: Long,
    val productoCodigo: String,
    val productoNombre: String,
    val unidadMedida: String,
    val tipo: String,
    val cantidad: Int,
    val stockResultante: Int,
    val fecha: Long,
    val motivo: String,
    val perfilResponsable: String
)

/** Un indicador de https://mindicador.cl/api (solo los campos que usa la app). */
data class IndicadorDto(
    val codigo: String,
    val nombre: String,
    @SerializedName("unidad_medida") val unidadMedida: String,
    val fecha: String,
    val valor: Double
)

/** Respuesta de https://mindicador.cl/api: se ignoran los indicadores que la app no usa. */
data class IndicadoresDto(
    val uf: IndicadorDto?,
    val dolar: IndicadorDto?,
    val euro: IndicadorDto?
)
