package cl.duoc.cappucare.movimientos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.cappucare.movimientos.client.ProductosClient;
import cl.duoc.cappucare.movimientos.dto.MovimientoRequest;
import cl.duoc.cappucare.movimientos.dto.MovimientoResponse;
import cl.duoc.cappucare.movimientos.dto.ProductoRemoto;
import cl.duoc.cappucare.movimientos.exception.ConflictoException;
import cl.duoc.cappucare.movimientos.exception.RecursoNoEncontradoException;
import cl.duoc.cappucare.movimientos.model.Movimiento;
import cl.duoc.cappucare.movimientos.model.TipoMovimiento;
import cl.duoc.cappucare.movimientos.repository.MovimientoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    private MovimientoRepository repositorio;

    @Mock
    private ProductosClient productos;

    @InjectMocks
    private MovimientoService servicio;

    private ProductoRemoto producto(int stock) {
        return new ProductoRemoto("INS-001", "Guantes", "Caja", stock, 5);
    }

    private MovimientoRequest solicitud(TipoMovimiento tipo, int cantidad) {
        return new MovimientoRequest("ins-001", tipo, cantidad, "Venta factura 123", "Vendedor");
    }

    @Test
    void registrar_ingreso_sumaAlStockYGuardaElSaldo() {
        when(productos.obtener("INS-001")).thenReturn(producto(10));
        when(productos.ajustarStock("INS-001", 5)).thenReturn(producto(15));
        when(repositorio.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        MovimientoResponse respuesta = servicio.registrar(solicitud(TipoMovimiento.INGRESO, 5));

        assertEquals(15, respuesta.stockResultante());
        assertEquals("Guantes", respuesta.productoNombre());
        assertEquals(TipoMovimiento.INGRESO, respuesta.tipo());
    }

    @Test
    void registrar_venta_restaDelStock() {
        when(productos.obtener("INS-001")).thenReturn(producto(10));
        when(productos.ajustarStock("INS-001", -4)).thenReturn(producto(6));
        when(repositorio.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        MovimientoResponse respuesta = servicio.registrar(solicitud(TipoMovimiento.SALIDA_VENTA, 4));

        assertEquals(6, respuesta.stockResultante());
        verify(productos).ajustarStock("INS-001", -4);
    }

    @Test
    void registrar_conStockInsuficiente_noGuardaElMovimiento() {
        when(productos.obtener("INS-001")).thenReturn(producto(2));
        when(productos.ajustarStock("INS-001", -4)).thenThrow(new ConflictoException("Stock insuficiente. Disponible: 2 Caja"));

        assertThrows(ConflictoException.class, () -> servicio.registrar(solicitud(TipoMovimiento.SALIDA_VENTA, 4)));
        verify(repositorio, never()).save(any());
    }

    @Test
    void registrar_conInsumoInexistente_propagaNoEncontrado() {
        when(productos.obtener("INS-001")).thenThrow(new RecursoNoEncontradoException("No existe"));

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.registrar(solicitud(TipoMovimiento.INGRESO, 1)));
    }

    @Test
    void registrar_siFallaElGuardado_revierteElAjusteDeStock() {
        when(productos.obtener("INS-001")).thenReturn(producto(10));
        when(productos.ajustarStock("INS-001", -3)).thenReturn(producto(7));
        when(repositorio.save(any(Movimiento.class))).thenThrow(new IllegalStateException("BD caída"));

        assertThrows(IllegalStateException.class, () -> servicio.registrar(solicitud(TipoMovimiento.SALIDA_MERMA, 3)));
        verify(productos).ajustarStock("INS-001", 3);
    }

    @Test
    void listar_sinFiltro_devuelveTodos() {
        Movimiento m = new Movimiento();
        m.setProductoCodigo("INS-001");
        m.setProductoNombre("Guantes");
        m.setUnidadMedida("Caja");
        m.setTipo(TipoMovimiento.INGRESO);
        when(repositorio.findAllByOrderByFechaDesc()).thenReturn(List.of(m));

        assertEquals(1, servicio.listar(null).size());
    }

    @Test
    void listar_conFiltro_buscaPorCodigoEnMayusculas() {
        when(repositorio.findByProductoCodigoOrderByFechaDesc("INS-001")).thenReturn(List.of());

        assertEquals(0, servicio.listar("ins-001").size());
    }

    @Test
    void obtener_inexistente_lanzaNoEncontrado() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(99L));
    }
}
