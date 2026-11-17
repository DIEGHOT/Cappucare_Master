package cl.duoc.cappucare.repository

import androidx.room.withTransaction
import cl.duoc.cappucare.data.local.AppDatabase
import cl.duoc.cappucare.model.AppError
import cl.duoc.cappucare.model.Movimiento
import cl.duoc.cappucare.model.Producto
import cl.duoc.cappucare.model.toEntity
import cl.duoc.cappucare.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CappucareRepository(private val db: AppDatabase) {

    private val productoDao = db.productoDao()
    private val movimientoDao = db.movimientoDao()

    val productos: Flow<List<Producto>> =
        productoDao.observarTodos().map { lista -> lista.map { it.toModel() } }

    val movimientos: Flow<List<Movimiento>> =
        movimientoDao.observarTodos().map { lista -> lista.map { it.toModel() } }

    suspend fun obtenerProducto(codigo: String): Producto? =
        productoDao.obtenerPorCodigo(codigo)?.toModel()

    suspend fun guardarProducto(producto: Producto, esEdicion: Boolean): Result<Producto> = runCatching {
        if (esEdicion) {
            productoDao.actualizar(producto.toEntity())
        } else {
            if (productoDao.obtenerPorCodigo(producto.codigo) != null) {
                throw AppError.CodigoDuplicado(producto.codigo)
            }
            productoDao.insertar(producto.toEntity())
        }
        producto
    }

    suspend fun eliminarProducto(codigo: String): Result<Unit> = runCatching {
        productoDao.eliminarPorCodigo(codigo)
    }

    /**
     * Registra el movimiento y actualiza el stock de forma atómica.
     * Si algo falla se hace rollback de ambas operaciones.
     */
    suspend fun registrarMovimiento(movimiento: Movimiento): Result<Producto> = runCatching {
        db.withTransaction {
            val producto = productoDao.obtenerPorCodigo(movimiento.productoCodigo)
                ?: throw AppError.ProductoNoExiste()
            val nuevoStock = if (movimiento.tipo.esSalida) {
                producto.stockActual - movimiento.cantidad
            } else {
                producto.stockActual + movimiento.cantidad
            }
            if (nuevoStock < 0) throw AppError.StockInsuficiente(producto.stockActual)
            movimientoDao.insertar(movimiento.toEntity())
            productoDao.actualizarStock(producto.codigo, nuevoStock)
            producto.copy(stockActual = nuevoStock).toModel()
        }
    }
}
