package cl.duoc.cappucare.viewmodel

import cl.duoc.cappucare.DatosDePrueba
import cl.duoc.cappucare.MainDispatcherRule
import cl.duoc.cappucare.data.local.MovimientoDao
import cl.duoc.cappucare.data.local.ProductoDao
import cl.duoc.cappucare.data.remote.IndicadoresApi
import cl.duoc.cappucare.data.remote.MovimientosApi
import cl.duoc.cappucare.data.remote.ProductosApi
import cl.duoc.cappucare.repository.CappucareRepository
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
import org.junit.Rule
import org.junit.Test

class InventarioViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val productoDao = mockk<ProductoDao>(relaxed = true)
    private val movimientoDao = mockk<MovimientoDao>(relaxed = true)
    private val productosApi = mockk<ProductosApi>()
    private val movimientosApi = mockk<MovimientosApi>()
    private val indicadoresApi = mockk<IndicadoresApi>()

    @Before
    fun preparar() {
        every { productoDao.observarTodos() } returns flowOf(
            listOf(DatosDePrueba.entidad, DatosDePrueba.entidadCritica)
        )
        every { movimientoDao.observarTodos() } returns flowOf(emptyList())
        coEvery { productosApi.listar() } returns listOf(DatosDePrueba.dto)
        coEvery { movimientosApi.listar() } returns emptyList()
    }

    private fun crearViewModel() = InventarioViewModel(
        CappucareRepository(productoDao, movimientoDao, productosApi, movimientosApi, indicadoresApi)
    )

    @Test
    fun alIniciar_sincronizaConLosMicroservicios() = runTest {
        crearViewModel()

        coVerify(exactly = 1) { productosApi.listar() }
        coVerify(exactly = 1) { movimientosApi.listar() }
    }

    @Test
    fun uiState_muestraTotalesYCriticos() = runTest {
        val estado = crearViewModel().uiState.first { !it.cargando && it.totalProductos == 2 }

        assertEquals(2, estado.productos.size)
        assertEquals(1, estado.totalCriticos)
        assertNull(estado.error)
    }

    @Test
    fun onConsultaChange_filtraPorNombreCodigoOCategoria() = runTest {
        val vm = crearViewModel()

        vm.onConsultaChange("guantes")
        assertEquals(listOf("INS-001"), vm.uiState.first { it.consulta == "guantes" }.productos.map { it.codigo })

        vm.onConsultaChange("ins-002")
        assertEquals(listOf("INS-002"), vm.uiState.first { it.consulta == "ins-002" }.productos.map { it.codigo })

        vm.onConsultaChange("protección")
        assertEquals(2, vm.uiState.first { it.consulta == "protección" }.productos.size)
    }

    @Test
    fun onToggleCriticos_dejaSoloLosInsumosCriticos() = runTest {
        val vm = crearViewModel()

        vm.onToggleCriticos()
        val filtrado = vm.uiState.first { it.soloCriticos }
        assertEquals(listOf("INS-002"), filtrado.productos.map { it.codigo })

        vm.onToggleCriticos()
        assertEquals(2, vm.uiState.first { !it.soloCriticos }.productos.size)
    }

    @Test
    fun sincronizar_sinConexion_informaYConservaLosDatosLocales() = runTest {
        coEvery { productosApi.listar() } throws IOException("sin red")

        val vm = crearViewModel()

        val mensaje = vm.mensaje.value
        assertTrue(mensaje?.contains("Mostrando los datos guardados en el dispositivo") == true)
        assertEquals(2, vm.uiState.first { it.totalProductos == 2 }.productos.size)
        assertEquals(false, vm.uiState.first().sincronizando)
    }

    @Test
    fun sincronizar_manual_vuelveAConsultarElServidor() = runTest {
        val vm = crearViewModel()

        vm.sincronizar()

        coVerify(exactly = 2) { productosApi.listar() }
    }

    @Test
    fun eliminar_correcto_informaConElNombreDelInsumo() = runTest {
        coEvery { productosApi.eliminar("INS-001") } returns Unit
        val vm = crearViewModel()

        vm.eliminar("INS-001", "Guantes de nitrilo")

        assertEquals("«Guantes de nitrilo» eliminado del inventario", vm.mensaje.value)
        coVerify { productoDao.eliminarPorCodigo("INS-001") }
    }

    @Test
    fun eliminar_conError_muestraElMensajeDelServidor() = runTest {
        coEvery { productosApi.eliminar("INS-001") } throws DatosDePrueba.errorHttp(500, "Falla interna")
        val vm = crearViewModel()

        vm.eliminar("INS-001", "Guantes de nitrilo")

        assertEquals("Falla interna", vm.mensaje.value)
    }

    @Test
    fun consumirMensaje_limpiaElMensaje() = runTest {
        coEvery { productosApi.eliminar(any()) } returns Unit
        val vm = crearViewModel()
        vm.eliminar("INS-001", "Guantes")

        vm.consumirMensaje()

        assertNull(vm.mensaje.value)
    }
}
