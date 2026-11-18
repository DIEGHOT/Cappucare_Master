package cl.duoc.cappucare.repository

import cl.duoc.cappucare.DatosDePrueba
import cl.duoc.cappucare.model.AppError
import cl.duoc.cappucare.model.comoAppError
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ErroresDeRedTest {

    @Test
    fun ioException_seTraduceASinConexion() {
        assertTrue(IOException("timeout").comoErrorDeRed() is AppError.SinConexion)
    }

    @Test
    fun http404_seTraduceAProductoNoExiste() {
        assertTrue(DatosDePrueba.errorHttp(404, "No existe").comoErrorDeRed() is AppError.ProductoNoExiste)
    }

    @Test
    fun http409_usaLaFabricaDeConflictoIndicada() {
        val error = DatosDePrueba.errorHttp(409, "Choque").comoErrorDeRed { AppError.StockInsuficiente(it) }
        assertTrue(error is AppError.StockInsuficiente)
        assertEquals("Choque", error.message)
    }

    @Test
    fun http409_porDefectoEsConflicto() {
        assertTrue(DatosDePrueba.errorHttp(409, "Choque").comoErrorDeRed() is AppError.Conflicto)
    }

    @Test
    fun http500_seTraduceAErrorDeServidorConElMensaje() {
        val error = DatosDePrueba.errorHttp(500, "Falla interna").comoErrorDeRed()
        assertTrue(error is AppError.Servidor)
        assertEquals("Falla interna", error.message)
    }

    @Test
    fun httpSinCuerpoJson_usaUnMensajeGenerico() {
        assertEquals("Error del servidor (503)", DatosDePrueba.errorHttpSinCuerpo(503).comoErrorDeRed().message)
    }

    @Test
    fun errorDesconocido_seEnvuelveEnDesconocido() {
        assertTrue(IllegalStateException("raro").comoErrorDeRed() is AppError.Desconocido)
    }

    @Test
    fun unAppError_seDevuelveTalCual() {
        val original = AppError.ProductoNoExiste()
        assertSame(original, original.comoErrorDeRed())
        assertSame(original, original.comoAppError())
    }

    @Test
    fun comoAppError_envuelveCualquierOtraExcepcion() {
        assertTrue(RuntimeException("x").comoAppError() is AppError.Desconocido)
    }

    @Test
    fun mensajesDeLosErroresDeNegocio() {
        assertEquals("Ya existe un insumo con el código A-1", AppError.CodigoDuplicado("A-1").mensaje)
        assertEquals("Ocurrió un error inesperado", AppError.Desconocido(null).mensaje)
    }
}
