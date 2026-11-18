package cl.duoc.cappucare.productos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.cappucare.productos.dto.ProductoRequest;
import cl.duoc.cappucare.productos.dto.ProductoResponse;
import cl.duoc.cappucare.productos.exception.ConflictoException;
import cl.duoc.cappucare.productos.exception.RecursoNoEncontradoException;
import cl.duoc.cappucare.productos.model.Producto;
import cl.duoc.cappucare.productos.repository.ProductoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repositorio;

    @InjectMocks
    private ProductoService servicio;

    private Producto existente() {
        Producto p = new Producto();
        p.setCodigo("INS-001");
        p.setNombre("Guantes");
        p.setCategoria("Equipos de protección");
        p.setStockActual(10);
        p.setStockMinimo(5);
        p.setUnidadMedida("Caja");
        p.setUbicacion("Pasillo 1");
        return p;
    }

    private ProductoRequest solicitud(String codigo) {
        return new ProductoRequest(codigo, "Guantes", "", "Equipos de protección", 10, 5, "Caja", "Pasillo 1", null);
    }

    @Test
    void crear_guardaElInsumoConElCodigoEnMayusculas() {
        when(repositorio.existsById("INS-009")).thenReturn(false);
        when(repositorio.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse respuesta = servicio.crear(solicitud("ins-009"));

        assertEquals("INS-009", respuesta.codigo());
        assertEquals(10, respuesta.stockActual());
    }

    @Test
    void crear_conCodigoExistente_lanzaConflicto() {
        when(repositorio.existsById("INS-001")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> servicio.crear(solicitud("INS-001")));
        verify(repositorio, never()).save(any());
    }

    @Test
    void obtener_inexistente_lanzaNoEncontrado() {
        when(repositorio.findById("NO-EXISTE")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener("no-existe"));
    }

    @Test
    void actualizar_modificaLosDatos() {
        Producto p = existente();
        when(repositorio.findById("INS-001")).thenReturn(Optional.of(p));
        when(repositorio.save(p)).thenReturn(p);

        ProductoRequest cambios = new ProductoRequest("INS-001", "Guantes XL", "Talla XL", "Equipos de protección", 3, 5, "Caja", "Pasillo 2", null);
        ProductoResponse respuesta = servicio.actualizar("INS-001", cambios);

        assertEquals("Guantes XL", respuesta.nombre());
        assertEquals(3, respuesta.stockActual());
    }

    @Test
    void eliminar_borraElInsumo() {
        Producto p = existente();
        when(repositorio.findById("INS-001")).thenReturn(Optional.of(p));

        servicio.eliminar("INS-001");

        verify(repositorio).delete(p);
    }

    @Test
    void ajustarStock_sumaIngresos() {
        Producto p = existente();
        when(repositorio.buscarParaActualizar("INS-001")).thenReturn(Optional.of(p));
        when(repositorio.save(p)).thenReturn(p);

        assertEquals(15, servicio.ajustarStock("INS-001", 5).stockActual());
    }

    @Test
    void ajustarStock_restaSalidas() {
        Producto p = existente();
        when(repositorio.buscarParaActualizar("INS-001")).thenReturn(Optional.of(p));
        when(repositorio.save(p)).thenReturn(p);

        assertEquals(4, servicio.ajustarStock("INS-001", -6).stockActual());
    }

    @Test
    void ajustarStock_conSalidaMayorAlStock_lanzaConflicto() {
        when(repositorio.buscarParaActualizar("INS-001")).thenReturn(Optional.of(existente()));

        ConflictoException error = assertThrows(ConflictoException.class, () -> servicio.ajustarStock("INS-001", -11));
        assertEquals("Stock insuficiente. Disponible: 10 Caja", error.getMessage());
    }
}
