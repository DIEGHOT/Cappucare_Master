package cl.duoc.cappucare.viewmodel

import cl.duoc.cappucare.model.ProductoFormState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesProductoTest {

    private val valido = ProductoFormState(
        codigo = "INS-001", nombre = "Guantes de nitrilo", categoria = "Equipos de protección",
        stockActual = "50", stockMinimo = "10", unidadMedida = "Caja", ubicacion = "Pasillo 3"
    )

    @Test
    fun formularioValido_noGeneraErrores() {
        assertFalse(validarProducto(valido).hayErrores)
    }

    @Test
    fun formularioVacio_marcaTodosLosCampos() {
        val errores = validarProducto(ProductoFormState())
        assertTrue(errores.hayErrores)
        assertNotNull(errores.codigo)
        assertNotNull(errores.nombre)
        assertNotNull(errores.categoria)
        assertNotNull(errores.stockActual)
        assertNotNull(errores.stockMinimo)
        assertNotNull(errores.unidadMedida)
        assertNotNull(errores.ubicacion)
    }

    @Test
    fun codigoConCaracteresInvalidos_esRechazado() {
        assertNotNull(validarProducto(valido.copy(codigo = "A B")).codigo)
        assertNotNull(validarProducto(valido.copy(codigo = "AB")).codigo)
    }

    @Test
    fun stockNoNumerico_oNegativo_esRechazado() {
        assertEquals("Debe ser un número entero", validarProducto(valido.copy(stockActual = "abc")).stockActual)
        assertNotNull(validarProducto(valido.copy(stockActual = "-5")).stockActual)
    }

    @Test
    fun stockCero_esValido_peroMinimoCero_no() {
        assertNull(validarProducto(valido.copy(stockActual = "0")).stockActual)
        assertNotNull(validarProducto(valido.copy(stockMinimo = "0")).stockMinimo)
    }
}
