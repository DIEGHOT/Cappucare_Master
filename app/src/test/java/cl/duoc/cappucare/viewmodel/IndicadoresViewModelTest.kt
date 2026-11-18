package cl.duoc.cappucare.viewmodel

import cl.duoc.cappucare.MainDispatcherRule
import cl.duoc.cappucare.data.local.MovimientoDao
import cl.duoc.cappucare.data.local.ProductoDao
import cl.duoc.cappucare.data.remote.IndicadorDto
import cl.duoc.cappucare.data.remote.IndicadoresApi
import cl.duoc.cappucare.data.remote.IndicadoresDto
import cl.duoc.cappucare.data.remote.MovimientosApi
import cl.duoc.cappucare.data.remote.ProductosApi
import cl.duoc.cappucare.model.DataState
import cl.duoc.cappucare.repository.CappucareRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class IndicadoresViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val indicadoresApi = mockk<IndicadoresApi>()
    private val dolar = IndicadorDto("dolar", "Dólar observado", "Pesos", "2026-11-17T03:00:00.000Z", 950.5)

    private fun crearViewModel() = IndicadoresViewModel(
        CappucareRepository(
            mockk<ProductoDao>(relaxed = true), mockk<MovimientoDao>(relaxed = true),
            mockk<ProductosApi>(), mockk<MovimientosApi>(), indicadoresApi
        )
    )

    @Test
    fun alIniciar_cargaLosIndicadoresDeLaApiExterna() = runTest {
        coEvery { indicadoresApi.obtener() } returns IndicadoresDto(uf = null, dolar = dolar, euro = null)

        val estado = crearViewModel().estado.value

        assertTrue(estado is DataState.Success)
        assertEquals("Dólar observado", (estado as DataState.Success).datos.single().nombre)
    }

    @Test
    fun siLaApiFalla_muestraUnEstadoDeError() = runTest {
        coEvery { indicadoresApi.obtener() } throws IOException("sin red")

        val estado = crearViewModel().estado.value

        assertTrue(estado is DataState.Error)
        assertTrue((estado as DataState.Error).mensaje.contains("No se pudo conectar"))
    }

    @Test
    fun cargar_denuevo_recuperaTrasUnError() = runTest {
        coEvery { indicadoresApi.obtener() } throws IOException("sin red")
        val vm = crearViewModel()

        coEvery { indicadoresApi.obtener() } returns IndicadoresDto(uf = null, dolar = dolar, euro = null)
        vm.cargar()

        assertTrue(vm.estado.value is DataState.Success)
        coVerify(exactly = 2) { indicadoresApi.obtener() }
    }
}
