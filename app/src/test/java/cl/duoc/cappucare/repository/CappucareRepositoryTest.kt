package cl.duoc.cappucare.repository

import cl.duoc.cappucare.DatosDePrueba
import cl.duoc.cappucare.data.local.MovimientoDao
import cl.duoc.cappucare.data.local.ProductoDao
import cl.duoc.cappucare.data.remote.IndicadorDto
import cl.duoc.cappucare.data.remote.IndicadoresApi
import cl.duoc.cappucare.data.remote.IndicadoresDto
import cl.duoc.cappucare.data.remote.MovimientosApi
import cl.duoc.cappucare.data.remote.ProductosApi
import cl.duoc.cappucare.model.AppError
import cl.duoc.cappucare.model.Movimiento
import cl.duoc.cappucare.model.TipoMovimiento
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CappucareRepositoryTest {

    private val productoDao = mockk<ProductoDao>(relaxed = true)
    private val movimientoDao = mockk<MovimientoDao>(relaxed = true)
    private val productosApi = mockk<ProductosApi>()
    private val movimientosApi = mockk<MovimientosApi>()
    private val indicadoresApi = mockk<IndicadoresApi>()
    private lateinit var repositorio: CappucareRepository

    private val movimientoNuevo = Movimiento(
        productoCodigo = "INS-001", tipo = TipoMovimiento.SALIDA_VENTA, cantidad = 3,
        fecha = 0, motivo = "Venta factura 123", perfilResponsable = "Vendedor"
    )

    @Before
    fun preparar() {
        every { productoDao.observarTodos() } returns flowOf(listOf(DatosDePrueba.entidad))
        every { movimientoDao.observarTodos() } returns flowOf(listOf(DatosDePrueba.movimientoEntidad))
        repositorio = CappucareRepository(productoDao, movimientoDao, productosApi, movimientosApi, indicadoresApi)
    }

    @Test
    fun productos_conviertePasaEntidadesAModelo() = runTest {
        assertEquals(listOf(DatosDePrueba.producto), repositorio.productos.first())
    }

    @Test
    fun movimientos_conviertePasaEntidadesAModelo() = runTest {
        val movimiento = repositorio.movimientos.first().single()
        assertEquals(TipoMovimiento.SALIDA_VENTA, movimiento.tipo)
        assertEquals(7, movimiento.stockResultante)
    }

    @Test
    fun obtenerProducto_devuelveElModeloOnullSiNoExiste() = runTest {
        coEvery { productoDao.obtenerPorCodigo("INS-001") } returns DatosDePrueba.entidad
        coEvery { productoDao.obtenerPorCodigo("NO") } returns null

        assertEquals(DatosDePrueba.producto, repositorio.obtenerProducto("INS-001"))
        assertNull(repositorio.obtenerProducto("NO"))
    }

    @Test
    fun sincronizar_reemplazaElCacheConLosDatosDelServidor() = runTest {
        coEvery { productosApi.listar() } returns listOf(DatosDePrueba.dto)
        coEvery { movimientosApi.listar() } returns listOf(DatosDePrueba.movimientoDto)

        val resultado = repositorio.sincronizar()

        assertTrue(resultado.isSuccess)
        coVerify { productoDao.reemplazarTodos(listOf(DatosDePrueba.entidad)) }
        coVerify { movimientoDao.reemplazarTodos(listOf(DatosDePrueba.movimientoEntidad)) }
    }

    @Test
    fun sincronizar_sinConexion_conservaElCacheYDevuelveSinConexion() = runTest {
        coEvery { productosApi.listar() } throws IOException("sin red")

        val resultado = repositorio.sincronizar()

        assertTrue(resultado.exceptionOrNull() is AppError.SinConexion)
        coVerify(exactly = 0) { productoDao.reemplazarTodos(any()) }
    }

    @Test
    fun guardarProducto_nuevo_llamaACrearYGuardaEnElCache() = runTest {
        coEvery { productosApi.crear(any()) } returns DatosDePrueba.dto

        val resultado = repositorio.guardarProducto(DatosDePrueba.producto, esEdicion = false)

        assertEquals(DatosDePrueba.producto, resultado.getOrNull())
        coVerify { productosApi.crear(DatosDePrueba.dto) }
        coVerify { productoDao.insertar(DatosDePrueba.entidad) }
    }

    @Test
    fun guardarProducto_edicion_llamaAActualizar() = runTest {
        coEvery { productosApi.actualizar("INS-001", any()) } returns DatosDePrueba.dto

        val resultado = repositorio.guardarProducto(DatosDePrueba.producto, esEdicion = true)

        assertTrue(resultado.isSuccess)
        coVerify { productosApi.actualizar("INS-001", DatosDePrueba.dto) }
        coVerify(exactly = 0) { productosApi.crear(any()) }
    }

    @Test
    fun guardarProducto_conConflicto_devuelveCodigoDuplicado() = runTest {
        coEvery { productosApi.crear(any()) } throws DatosDePrueba.errorHttp(409, "Ya existe")

        val resultado = repositorio.guardarProducto(DatosDePrueba.producto, esEdicion = false)

        assertTrue(resultado.exceptionOrNull() is AppError.CodigoDuplicado)
        coVerify(exactly = 0) { productoDao.insertar(any()) }
    }

    @Test
    fun eliminarProducto_borraEnElServidorYEnElCache() = runTest {
        coEvery { productosApi.eliminar("INS-001") } returns Unit

        val resultado = repositorio.eliminarProducto("INS-001")

        assertTrue(resultado.isSuccess)
        coVerify { productoDao.eliminarPorCodigo("INS-001") }
    }

    @Test
    fun eliminarProducto_conErrorDelServidor_noToCaElCache() = runTest {
        coEvery { productosApi.eliminar("INS-001") } throws DatosDePrueba.errorHttp(500, "Falla interna")

        val resultado = repositorio.eliminarProducto("INS-001")

        assertEquals("Falla interna", resultado.exceptionOrNull()?.message)
        coVerify(exactly = 0) { productoDao.eliminarPorCodigo(any()) }
    }

    @Test
    fun registrarMovimiento_actualizaElStockLocalConElSaldoDelServidor() = runTest {
        coEvery { movimientosApi.crear(any()) } returns DatosDePrueba.movimientoDto
        coEvery { productoDao.obtenerPorCodigo("INS-001") } returns DatosDePrueba.entidad

        val resultado = repositorio.registrarMovimiento(movimientoNuevo)

        assertEquals(7, resultado.getOrNull()?.stockActual)
        coVerify { movimientoDao.insertar(DatosDePrueba.movimientoEntidad) }
        coVerify { productoDao.insertar(DatosDePrueba.entidad.copy(stockActual = 7)) }
    }

    @Test
    fun registrarMovimiento_siElInsumoNoEstaEnElCache_loConsultaAlServidor() = runTest {
        coEvery { movimientosApi.crear(any()) } returns DatosDePrueba.movimientoDto
        coEvery { productoDao.obtenerPorCodigo("INS-001") } returns null
        coEvery { productosApi.obtener("INS-001") } returns DatosDePrueba.dto.copy(stockActual = 7)

        val resultado = repositorio.registrarMovimiento(movimientoNuevo)

        assertEquals(7, resultado.getOrNull()?.stockActual)
        coVerify { productosApi.obtener("INS-001") }
    }

    @Test
    fun registrarMovimiento_conStockInsuficiente_devuelveElMensajeDelServidor() = runTest {
        coEvery { movimientosApi.crear(any()) } throws DatosDePrueba.errorHttp(409, "Stock insuficiente. Disponible: 2 Caja")

        val error = repositorio.registrarMovimiento(movimientoNuevo).exceptionOrNull()

        assertTrue(error is AppError.StockInsuficiente)
        assertEquals("Stock insuficiente. Disponible: 2 Caja", error?.message)
    }

    @Test
    fun obtenerIndicadores_convierteSoloLosIndicadoresDisponibles() = runTest {
        val dolar = IndicadorDto("dolar", "Dólar observado", "Pesos", "2026-11-17T03:00:00.000Z", 950.5)
        coEvery { indicadoresApi.obtener() } returns IndicadoresDto(uf = null, dolar = dolar, euro = null)

        val indicadores = repositorio.obtenerIndicadores().getOrNull()

        assertEquals(1, indicadores?.size)
        assertEquals(950.5, indicadores?.first()?.valor ?: 0.0, 0.0)
    }

    @Test
    fun obtenerIndicadores_sinConexion_devuelveError() = runTest {
        coEvery { indicadoresApi.obtener() } throws IOException("sin red")

        assertTrue(repositorio.obtenerIndicadores().exceptionOrNull() is AppError.SinConexion)
    }
}
