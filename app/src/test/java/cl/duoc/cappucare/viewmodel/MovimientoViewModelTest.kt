package cl.duoc.cappucare.viewmodel

import cl.duoc.cappucare.DatosDePrueba
import cl.duoc.cappucare.MainDispatcherRule
import cl.duoc.cappucare.data.local.MovimientoDao
import cl.duoc.cappucare.data.local.ProductoDao
import cl.duoc.cappucare.data.remote.IndicadoresApi
import cl.duoc.cappucare.data.remote.MovimientosApi
import cl.duoc.cappucare.data.remote.ProductosApi
import cl.duoc.cappucare.model.DataState
import cl.duoc.cappucare.model.TipoMovimiento
import cl.duoc.cappucare.notification.StockNotifier
import cl.duoc.cappucare.repository.CappucareRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MovimientoViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val productoDao = mockk<ProductoDao>(relaxed = true)
    private val movimientoDao = mockk<MovimientoDao>(relaxed = true)
    private val movimientosApi = mockk<MovimientosApi>()
    private val notificador = mockk<StockNotifier>(relaxed = true)

    @Before
    fun preparar() {
        every { productoDao.observarTodos() } returns flowOf(listOf(DatosDePrueba.entidad))
        every { movimientoDao.observarTodos() } returns flowOf(listOf(DatosDePrueba.movimientoEntidad))
        coEvery { productoDao.obtenerPorCodigo("INS-001") } returns DatosDePrueba.entidad
    }

    private fun crearViewModel() = MovimientoViewModel(
        CappucareRepository(productoDao, movimientoDao, mockk<ProductosApi>(), movimientosApi, mockk<IndicadoresApi>()),
        notificador
    )

    private fun MovimientoViewModel.completarFormularioValido(cantidad: String = "3") {
        onProductoChange("INS-001")
        onTipoChange(TipoMovimiento.SALIDA_VENTA)
        onCantidadChange(cantidad)
        onMotivoChange("Venta factura 123")
        onPerfilChange("Vendedor")
    }

    @Test
    fun productos_exponeElCatalogoLocal() = runTest {
        val productos = crearViewModel().productos.first { it.isNotEmpty() }
        assertEquals(listOf(DatosDePrueba.producto), productos)
    }

    @Test
    fun historial_muestraLosMovimientosConNombreYUnidadGuardados() = runTest {
        val estado = crearViewModel().historial.first { it is DataState.Success }

        val item = (estado as DataState.Success).datos.single()
        assertEquals("Guantes de nitrilo", item.nombreProducto)
        assertEquals("Caja", item.unidadMedida)
    }

    @Test
    fun guardar_conFormularioVacio_muestraTodosLosErrores() = runTest {
        val vm = crearViewModel()

        vm.guardar()

        val errores = vm.state.value.errores
        assertNotNull(errores.producto)
        assertNotNull(errores.cantidad)
        assertNotNull(errores.motivo)
        assertNotNull(errores.perfil)
        coVerify(exactly = 0) { movimientosApi.crear(any()) }
    }

    @Test
    fun guardar_salidaMayorAlStock_seRechazaSinLlamarAlServidor() = runTest {
        val vm = crearViewModel()
        vm.completarFormularioValido(cantidad = "11")

        vm.guardar()

        assertEquals("Stock insuficiente. Disponible: 10 Caja", vm.state.value.errores.cantidad)
        coVerify(exactly = 0) { movimientosApi.crear(any()) }
    }

    @Test
    fun guardar_correcto_registraElMovimientoYMuestraElResultado() = runTest {
        coEvery { movimientosApi.crear(any()) } returns DatosDePrueba.movimientoDto
        val vm = crearViewModel()
        vm.completarFormularioValido()

        vm.guardar()

        val resultado = vm.state.value.resultado
        assertNotNull(resultado)
        assertTrue(resultado!!.mensaje.contains("Venta registrado"))
        assertFalse(resultado.stockCritico)
        assertEquals("", vm.state.value.cantidad)
        assertEquals("Vendedor", vm.state.value.perfil)
        coVerify { movimientosApi.crear(match { it.tipo == "SALIDA_VENTA" && it.cantidad == 3 }) }
        verify(exactly = 0) { notificador.notificarStockCritico(any()) }
    }

    @Test
    fun guardar_quedandoEnElMinimo_notificaStockCritico() = runTest {
        coEvery { movimientosApi.crear(any()) } returns DatosDePrueba.movimientoDto.copy(stockResultante = 5)
        val vm = crearViewModel()
        vm.completarFormularioValido(cantidad = "5")

        vm.guardar()

        assertTrue(vm.state.value.resultado!!.stockCritico)
        verify(exactly = 1) { notificador.notificarStockCritico(any()) }
    }

    @Test
    fun guardar_conErrorDelServidor_muestraElMensaje() = runTest {
        coEvery { movimientosApi.crear(any()) } throws DatosDePrueba.errorHttp(409, "Stock insuficiente. Disponible: 2 Caja")
        val vm = crearViewModel()
        vm.completarFormularioValido()

        vm.guardar()

        assertEquals("Stock insuficiente. Disponible: 2 Caja", vm.state.value.errorGeneral)
        assertFalse(vm.state.value.guardando)
        assertNull(vm.state.value.resultado)
    }

    @Test
    fun guardar_sinConexion_muestraErrorGeneral() = runTest {
        coEvery { movimientosApi.crear(any()) } throws IOException("sin red")
        val vm = crearViewModel()
        vm.completarFormularioValido()

        vm.guardar()

        assertTrue(vm.state.value.errorGeneral?.contains("No se pudo conectar") == true)
    }

    @Test
    fun consumirResultado_limpiaElResultado() = runTest {
        coEvery { movimientosApi.crear(any()) } returns DatosDePrueba.movimientoDto
        val vm = crearViewModel()
        vm.completarFormularioValido()
        vm.guardar()

        vm.consumirResultado()

        assertNull(vm.state.value.resultado)
    }

    @Test
    fun cambiarCampos_actualizaElEstadoYLimpiaErrores() = runTest {
        val vm = crearViewModel()
        vm.guardar()

        vm.onProductoChange("INS-001")
        vm.onTipoChange(TipoMovimiento.INGRESO)
        vm.onCantidadChange("1x2")
        vm.onMotivoChange("Recepción")
        vm.onPerfilChange("Bodeguero")

        val estado = vm.state.value
        assertEquals("INS-001", estado.productoCodigo)
        assertEquals(TipoMovimiento.INGRESO, estado.tipo)
        assertEquals("12", estado.cantidad)
        assertFalse(estado.errores.hayErrores)
    }
}
