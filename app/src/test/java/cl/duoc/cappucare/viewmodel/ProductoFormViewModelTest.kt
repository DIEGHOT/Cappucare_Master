package cl.duoc.cappucare.viewmodel

import cl.duoc.cappucare.DatosDePrueba
import cl.duoc.cappucare.MainDispatcherRule
import cl.duoc.cappucare.data.local.MovimientoDao
import cl.duoc.cappucare.data.local.ProductoDao
import cl.duoc.cappucare.data.remote.IndicadoresApi
import cl.duoc.cappucare.data.remote.MovimientosApi
import cl.duoc.cappucare.data.remote.ProductosApi
import cl.duoc.cappucare.notification.StockNotifier
import cl.duoc.cappucare.repository.CappucareRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProductoFormViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val productoDao = mockk<ProductoDao>(relaxed = true)
    private val movimientoDao = mockk<MovimientoDao>(relaxed = true)
    private val productosApi = mockk<ProductosApi>()
    private val notificador = mockk<StockNotifier>(relaxed = true)

    private fun crearViewModel() = ProductoFormViewModel(
        CappucareRepository(productoDao, movimientoDao, productosApi, mockk<MovimientosApi>(), mockk<IndicadoresApi>()),
        notificador
    )

    private fun ProductoFormViewModel.completarFormularioValido(stock: String = "10", minimo: String = "5") {
        onCodigoChange("ins-001")
        onNombreChange("Guantes de nitrilo")
        onDescripcionChange("Caja de 100")
        onCategoriaChange("Equipos de protección")
        onStockActualChange(stock)
        onStockMinimoChange(minimo)
        onUnidadChange("Caja")
        onUbicacionChange("Pasillo 1")
    }

    @Test
    fun cargar_sinCodigo_dejaUnFormularioNuevo() = runTest {
        val vm = crearViewModel()

        vm.cargar(null)

        assertFalse(vm.state.value.esEdicion)
        assertEquals("", vm.state.value.codigo)
    }

    @Test
    fun cargar_conCodigo_llenaElFormularioParaEdicion() = runTest {
        coEvery { productoDao.obtenerPorCodigo("INS-001") } returns DatosDePrueba.entidad
        val vm = crearViewModel()

        vm.cargar("INS-001")

        assertTrue(vm.state.value.esEdicion)
        assertEquals("Guantes de nitrilo", vm.state.value.nombre)
        assertEquals("10", vm.state.value.stockActual)
        assertEquals("Caja de 100", vm.state.value.descripcion)
    }

    @Test
    fun cargar_dosVecesConLaMismaClave_noReiniciaLoEscrito() = runTest {
        val vm = crearViewModel()
        vm.cargar(null)
        vm.onNombreChange("Escrito por el usuario")

        vm.cargar(null)

        assertEquals("Escrito por el usuario", vm.state.value.nombre)
    }

    @Test
    fun onCodigoEscaneado_siExiste_cargaElInsumoParaEditar() = runTest {
        coEvery { productoDao.obtenerPorCodigo("INS-001") } returns DatosDePrueba.entidad
        val vm = crearViewModel()

        vm.onCodigoEscaneado("ins-001")

        assertTrue(vm.state.value.esEdicion)
        assertEquals("INS-001", vm.state.value.codigo)
    }

    @Test
    fun onCodigoEscaneado_siNoExiste_soloCompletaElCodigo() = runTest {
        coEvery { productoDao.obtenerPorCodigo("7801234567890") } returns null
        val vm = crearViewModel()

        vm.onCodigoEscaneado(" 7801234567890 ")

        assertEquals("7801234567890", vm.state.value.codigo)
        assertFalse(vm.state.value.esEdicion)
    }

    @Test
    fun cambiarUnCampo_limpiaSuErrorYFiltraLosNumeros() = runTest {
        val vm = crearViewModel()
        vm.guardar()
        assertNotNull(vm.state.value.errores.nombre)

        vm.onNombreChange("Guantes")
        vm.onStockActualChange("1a2b")
        vm.onStockMinimoChange("x5")

        assertNull(vm.state.value.errores.nombre)
        assertEquals("12", vm.state.value.stockActual)
        assertEquals("5", vm.state.value.stockMinimo)
    }

    @Test
    fun guardar_conFormularioInvalido_muestraErroresYNoLlamaAlServidor() = runTest {
        val vm = crearViewModel()

        vm.guardar()

        assertTrue(vm.state.value.errores.hayErrores)
        assertFalse(vm.state.value.guardadoExitoso)
        coVerify(exactly = 0) { productosApi.crear(any()) }
    }

    @Test
    fun guardar_conDescripcionLarga_muestraError() = runTest {
        val vm = crearViewModel()
        vm.completarFormularioValido()
        vm.onDescripcionChange("x".repeat(201))

        vm.guardar()

        assertNotNull(vm.state.value.errores.descripcion)
    }

    @Test
    fun guardar_correcto_marcaExitoYNoNotificaSiElStockEsSuficiente() = runTest {
        coEvery { productosApi.crear(any()) } returns DatosDePrueba.dto
        val vm = crearViewModel()
        vm.completarFormularioValido()

        vm.guardar()

        assertTrue(vm.state.value.guardadoExitoso)
        assertFalse(vm.state.value.alertaStock)
        verify(exactly = 0) { notificador.notificarStockCritico(any()) }
        coVerify { productosApi.crear(match { it.codigo == "INS-001" && it.nombre == "Guantes de nitrilo" }) }
    }

    @Test
    fun guardar_conStockEnElMinimo_enviaNotificacion() = runTest {
        coEvery { productosApi.crear(any()) } returns DatosDePrueba.dto.copy(stockActual = 5)
        val vm = crearViewModel()
        vm.completarFormularioValido(stock = "5")

        vm.guardar()

        assertTrue(vm.state.value.alertaStock)
        verify(exactly = 1) { notificador.notificarStockCritico(any()) }
    }

    @Test
    fun guardar_enEdicion_llamaAActualizar() = runTest {
        coEvery { productoDao.obtenerPorCodigo("INS-001") } returns DatosDePrueba.entidad
        coEvery { productosApi.actualizar("INS-001", any()) } returns DatosDePrueba.dto
        val vm = crearViewModel()
        vm.cargar("INS-001")

        vm.guardar()

        assertTrue(vm.state.value.guardadoExitoso)
        coVerify { productosApi.actualizar("INS-001", any()) }
    }

    @Test
    fun guardar_conCodigoDuplicado_muestraElErrorEnElCampoCodigo() = runTest {
        coEvery { productosApi.crear(any()) } throws DatosDePrueba.errorHttp(409, "Ya existe")
        val vm = crearViewModel()
        vm.completarFormularioValido()

        vm.guardar()

        assertEquals("Ya existe un insumo con el código INS-001", vm.state.value.errores.codigo)
        assertFalse(vm.state.value.guardando)
    }

    @Test
    fun guardar_sinConexion_muestraErrorGeneral() = runTest {
        coEvery { productosApi.crear(any()) } throws IOException("sin red")
        val vm = crearViewModel()
        vm.completarFormularioValido()

        vm.guardar()

        assertTrue(vm.state.value.errorGeneral?.contains("No se pudo conectar") == true)
        assertFalse(vm.state.value.guardadoExitoso)
    }

    @Test
    fun guardar_unaVezExitoso_ignoraNuevosClics() = runTest {
        coEvery { productosApi.crear(any()) } returns DatosDePrueba.dto
        val vm = crearViewModel()
        vm.completarFormularioValido()
        vm.guardar()

        vm.guardar()

        coVerify(exactly = 1) { productosApi.crear(any()) }
    }

    @Test
    fun fotoYErrorExterno_seReflejanEnElEstado() = runTest {
        val vm = crearViewModel()

        vm.onFotoChange("content://foto.jpg")
        assertEquals("content://foto.jpg", vm.state.value.fotoUri)

        vm.mostrarError("Sin permiso de cámara")
        assertEquals("Sin permiso de cámara", vm.state.value.errorGeneral)
    }
}
