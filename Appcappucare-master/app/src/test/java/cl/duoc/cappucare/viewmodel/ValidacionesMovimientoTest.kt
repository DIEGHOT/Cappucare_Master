package cl.duoc.cappucare.viewmodel

import cl.duoc.cappucare.model.MovimientoFormState
import cl.duoc.cappucare.model.Producto
import cl.duoc.cappucare.model.TipoMovimiento
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesMovimientoTest {

    private val producto = Producto(
        codigo = "INS-001", nombre = "Guantes", categoria = "Equipos de protección",
        stockActual = 20, stockMinimo = 5, unidadMedida = "Caja", ubicacion = "Pasillo 3"
    )
    private val valido = MovimientoFormState(
        productoCodigo = "INS-001", tipo = TipoMovimiento.SALIDA_VENTA,
        cantidad = "5", motivo = "Venta factura 123", perfil = "Vendedor"
    )

    @Test
    fun movimientoValido_noGeneraErrores() {
        assertFalse(validarMovimiento(valido, producto).hayErrores)
    }

    @Test
    fun sinProducto_marcaError() {
        assertNotNull(validarMovimiento(valido, null).producto)
    }

    @Test
    fun salidaMayorAlStock_esRechazada() {
        assertNotNull(validarMovimiento(valido.copy(cantidad = "21"), producto).cantidad)
    }

    @Test
    fun ingresoMayorAlStock_esValido() {
        val errores = validarMovimiento(valido.copy(tipo = TipoMovimiento.INGRESO, cantidad = "500"), producto)
        assertNull(errores.cantidad)
    }

    @Test
    fun cantidadCero_motivoVacio_yPerfilVacio_sonRechazados() {
        val errores = validarMovimiento(valido.copy(cantidad = "0", motivo = "", perfil = ""), producto)
        assertNotNull(errores.cantidad)
        assertNotNull(errores.motivo)
        assertNotNull(errores.perfil)
    }
}
