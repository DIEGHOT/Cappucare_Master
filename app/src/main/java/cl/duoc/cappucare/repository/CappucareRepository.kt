package cl.duoc.cappucare.repository

import cl.duoc.cappucare.data.local.MovimientoDao
import cl.duoc.cappucare.data.local.ProductoDao
import cl.duoc.cappucare.data.remote.IndicadoresApi
import cl.duoc.cappucare.data.remote.MovimientosApi
import cl.duoc.cappucare.data.remote.ProductosApi
import cl.duoc.cappucare.model.AppError
import cl.duoc.cappucare.model.Indicador
import cl.duoc.cappucare.model.Movimiento
import cl.duoc.cappucare.model.Producto
import cl.duoc.cappucare.model.toDto
import cl.duoc.cappucare.model.toEntity
import cl.duoc.cappucare.model.toModel
import cl.duoc.cappucare.model.toRequestDto
import com.google.gson.JsonParser
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

/**
- Fuente única de datos. Los microservicios son la fuente de verdad y Room guarda una copia local:
- la UI siempre lee de Room (funciona sin conexión) y toda escritura va primero al servidor.
 */
class CappucareRepository(
    private val productoDao: ProductoDao,
    private val movimientoDao: MovimientoDao,
    private val productosApi: ProductosApi,
    private val movimientosApi: MovimientosApi,
    private val indicadoresApi: IndicadoresApi
) {

    val productos: Flow<List<Producto>> =
        productoDao.observarTodos().map { lista -> lista.map { it.toModel() } }

    val movimientos: Flow<List<Movimiento>> =
        movimientoDao.observarTodos().map { lista -> lista.map { it.toModel() } }

    suspend fun obtenerProducto(codigo: String): Producto? =
        productoDao.obtenerPorCodigo(codigo)?.toModel()

    /** Descarga insumos y movimientos y reemplaza la copia local. Si falla, se conservan los datos locales. */
    suspend fun sincronizar(): Result<Unit> = llamarRed {
        val insumos = productosApi.listar()
        val historial = movimientosApi.listar()
        productoDao.reemplazarTodos(insumos.map { it.toEntity() })
        movimientoDao.reemplazarTodos(historial.map { it.toEntity() })
    }

    suspend fun guardarProducto(producto: Producto, esEdicion: Boolean): Result<Producto> =
        llamarRed({ AppError.CodigoDuplicado(producto.codigo) }) {
            val dto = producto.toDto()
            if (esEdicion) {
                productosApi.actualizar(producto.codigo, dto)
            } else {
                productosApi.crear(dto)
            }
            val entidad = dto.toEntity()
            productoDao.insertar(entidad)
            entidad.toModel()
        }

    suspend fun eliminarProducto(codigo: String): Result<Unit> = llamarRed {
        productosApi.eliminar(codigo)
        productoDao.eliminarPorCodigo(codigo)
    }

    /** Registra el movimiento en el servidor (que ajusta el stock) y actualiza la copia local. */
    suspend fun registrarMovimiento(movimiento: Movimiento): Result<Producto> =
        llamarRed({ AppError.StockInsuficiente(it) }) {
            val creado = movimientosApi.crear(movimiento.toRequestDto())
            movimientoDao.insertar(creado.toEntity())
            val actualizado = productoDao.obtenerPorCodigo(movimiento.productoCodigo)
                ?.copy(stockActual = creado.stockResultante)
                ?: productosApi.obtener(movimiento.productoCodigo).toEntity()
            productoDao.insertar(actualizado)
            actualizado.toModel()
        }

    /** Consulta la API externa de indicadores económicos. */
    suspend fun obtenerIndicadores(): Result<List<Indicador>> = llamarRed {
        indicadoresApi.obtener().toModel()
    }

    private suspend inline fun <T> llamarRed(
        noinline alConflicto: (String) -> AppError = { AppError.Conflicto(it) },
        bloque: () -> T
    ): Result<T> = try {
        Result.success(bloque())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e.comoErrorDeRed(alConflicto))
    }
}

/** Traduce excepciones de red/HTTP a errores que la UI sabe explicar al usuario. */
internal fun Throwable.comoErrorDeRed(alConflicto: (String) -> AppError = { AppError.Conflicto(it) }): AppError =
    when (this) {
        is AppError -> this
        is HttpException -> {
            val detalle = mensajeDelServidor() ?: "Error del servidor (${code()})"
            when (code()) {
                404 -> AppError.ProductoNoExiste()
                409 -> alConflicto(detalle)
                else -> AppError.Servidor(detalle)
            }
        }
        is IOException -> AppError.SinConexion()
        else -> AppError.Desconocido(this)
    }

/** Lee el campo "mensaje" del cuerpo de error JSON que entregan los microservicios. */
private fun HttpException.mensajeDelServidor(): String? {
    val cuerpo = response()?.errorBody()?.string().orEmpty()
    return runCatching { JsonParser.parseString(cuerpo).asJsonObject.get("mensaje").asString }.getOrNull()
}
