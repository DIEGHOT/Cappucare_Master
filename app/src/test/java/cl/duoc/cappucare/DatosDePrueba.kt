package cl.duoc.cappucare

import cl.duoc.cappucare.data.local.MovimientoEntity
import cl.duoc.cappucare.data.local.ProductoEntity
import cl.duoc.cappucare.data.remote.MovimientoDto
import cl.duoc.cappucare.data.remote.ProductoDto
import cl.duoc.cappucare.model.Producto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

object DatosDePrueba {

    val producto = Producto(
        codigo = "INS-001", nombre = "Guantes de nitrilo", descripcion = "Caja de 100",
        categoria = "Equipos de protección", stockActual = 10, stockMinimo = 5,
        unidadMedida = "Caja", ubicacion = "Pasillo 1", fotoUri = null
    )

    val productoCritico = producto.copy(codigo = "INS-002", nombre = "Mascarillas", stockActual = 3)

    val entidad = ProductoEntity(
        codigo = "INS-001", nombre = "Guantes de nitrilo", descripcion = "Caja de 100",
        categoria = "Equipos de protección", stockActual = 10, stockMinimo = 5,
        unidadMedida = "Caja", ubicacion = "Pasillo 1", fotoUri = null
    )

    val entidadCritica = entidad.copy(codigo = "INS-002", nombre = "Mascarillas", stockActual = 3)

    val dto = ProductoDto(
        codigo = "INS-001", nombre = "Guantes de nitrilo", descripcion = "Caja de 100",
        categoria = "Equipos de protección", stockActual = 10, stockMinimo = 5,
        unidadMedida = "Caja", ubicacion = "Pasillo 1", fotoUri = null
    )

    val movimientoDto = MovimientoDto(
        id = 7, productoCodigo = "INS-001", productoNombre = "Guantes de nitrilo", unidadMedida = "Caja",
        tipo = "SALIDA_VENTA", cantidad = 3, stockResultante = 7, fecha = 1_700_000_000_000,
        motivo = "Venta factura 123", perfilResponsable = "Vendedor"
    )

    val movimientoEntidad = MovimientoEntity(
        id = 7, productoCodigo = "INS-001", productoNombre = "Guantes de nitrilo", unidadMedida = "Caja",
        tipo = "SALIDA_VENTA", cantidad = 3, stockResultante = 7, fecha = 1_700_000_000_000,
        motivo = "Venta factura 123", perfilResponsable = "Vendedor"
    )

    /** Error HTTP como el que lanzaría Retrofit, con el cuerpo JSON que entregan los microservicios. */
    fun errorHttp(codigo: Int, mensaje: String): HttpException {
        val cuerpo = """{"mensaje":"$mensaje"}""".toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(codigo, cuerpo))
    }

    fun errorHttpSinCuerpo(codigo: Int): HttpException =
        HttpException(Response.error<Any>(codigo, "".toResponseBody("text/plain".toMediaType())))
}
