package cl.duoc.cappucare.model

import cl.duoc.cappucare.DatosDePrueba
import cl.duoc.cappucare.data.remote.IndicadorDto
import cl.duoc.cappucare.data.remote.IndicadoresDto
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModeloTest {

    @Test
    fun estado_depende_de_las_existencias() {
        val base = DatosDePrueba.producto
        assertEquals(EstadoProducto.DISPONIBLE, base.estado)
        assertEquals(EstadoProducto.STOCK_BAJO, base.copy(stockActual = 5).estado)
        assertEquals(EstadoProducto.STOCK_BAJO, base.copy(stockActual = 1).estado)
        assertEquals(EstadoProducto.AGOTADO, base.copy(stockActual = 0).estado)
    }

    @Test
    fun stockCritico_sePresentaCuandoElStockAlcanzaElMinimo() {
        assertFalse(DatosDePrueba.producto.stockCritico)
        assertTrue(DatosDePrueba.producto.copy(stockActual = 5).stockCritico)
    }

    @Test
    fun tipoMovimiento_clasificaEntradasYSalidas() {
        assertFalse(TipoMovimiento.INGRESO.esSalida)
        assertTrue(TipoMovimiento.SALIDA_VENTA.esSalida)
        assertTrue(TipoMovimiento.SALIDA_MERMA.esSalida)
    }

    @Test
    fun tipoMovimiento_desde_usaIngresoSiElTextoNoExiste() {
        assertEquals(TipoMovimiento.SALIDA_MERMA, TipoMovimiento.desde("SALIDA_MERMA"))
        assertEquals(TipoMovimiento.INGRESO, TipoMovimiento.desde("otro"))
    }

    @Test
    fun mapeoEntidadDtoModelo_esConsistente() {
        assertEquals(DatosDePrueba.entidad, DatosDePrueba.dto.toEntity())
        assertEquals(DatosDePrueba.producto, DatosDePrueba.entidad.toModel())
        assertEquals(DatosDePrueba.dto, DatosDePrueba.producto.toDto())
        assertEquals(DatosDePrueba.movimientoEntidad, DatosDePrueba.movimientoDto.toEntity())
    }

    @Test
    fun dtoSinDescripcion_seGuardaComoTextoVacio() {
        assertEquals("", DatosDePrueba.dto.copy(descripcion = null).toEntity().descripcion)
    }

    @Test
    fun movimiento_seConvierteAlDtoDeEnvio() {
        val dto = Movimiento(
            productoCodigo = "INS-001", tipo = TipoMovimiento.INGRESO, cantidad = 4,
            fecha = 0, motivo = "Recepción", perfilResponsable = "Bodeguero"
        ).toRequestDto()

        assertEquals("INGRESO", dto.tipo)
        assertEquals(4, dto.cantidad)
        assertEquals("Bodeguero", dto.perfilResponsable)
    }

    @Test
    fun entidadDeMovimiento_seConvierteAModelo() {
        val modelo = DatosDePrueba.movimientoEntidad.toModel()
        assertEquals(TipoMovimiento.SALIDA_VENTA, modelo.tipo)
        assertEquals("Guantes de nitrilo", modelo.productoNombre)
    }

    @Test
    fun indicadores_seFiltranYOrdenan() {
        val uf = IndicadorDto("uf", "Unidad de fomento", "Pesos", "2026-11-17", 39000.0)
        val euro = IndicadorDto("euro", "Euro", "Pesos", "2026-11-17", 1050.0)
        val lista = IndicadoresDto(uf = uf, dolar = null, euro = euro).toModel()

        assertEquals(listOf("uf", "euro"), lista.map { it.codigo })
    }

    @Test
    fun comoDataState_emiteCargandoYLuegoExito() = runTest {
        assertEquals(
            listOf(DataState.Loading, DataState.Success(1)),
            flowOf(1).comoDataState().toList()
        )
    }

    @Test
    fun comoDataState_convierteLosErroresEnEstadoDeError() = runTest {
        val estados = flow<Int> { throw IllegalStateException("falló") }.comoDataState().toList()
        assertEquals(listOf(DataState.Loading, DataState.Error("falló")), estados)
    }

    @Test
    fun formularioDeInsumo_detectaErrores() {
        assertFalse(ProductoFormErrores().hayErrores)
        assertTrue(ProductoFormErrores(descripcion = "larga").hayErrores)
        assertFalse(MovimientoFormErrores().hayErrores)
        assertTrue(MovimientoFormErrores(motivo = "falta").hayErrores)
    }
}
