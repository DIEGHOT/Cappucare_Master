package cl.duoc.cappucare.movimientos.service;

import cl.duoc.cappucare.movimientos.client.ProductosClient;
import cl.duoc.cappucare.movimientos.dto.MovimientoRequest;
import cl.duoc.cappucare.movimientos.dto.MovimientoResponse;
import cl.duoc.cappucare.movimientos.dto.ProductoRemoto;
import cl.duoc.cappucare.movimientos.exception.RecursoNoEncontradoException;
import cl.duoc.cappucare.movimientos.model.Movimiento;
import cl.duoc.cappucare.movimientos.repository.MovimientoRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MovimientoService {

    private static final Logger LOG = LoggerFactory.getLogger(MovimientoService.class);

    private final MovimientoRepository repositorio;
    private final ProductosClient productos;

    public MovimientoService(MovimientoRepository repositorio, ProductosClient productos) {
        this.repositorio = repositorio;
        this.productos = productos;
    }

    public List<MovimientoResponse> listar(String productoCodigo) {
        List<Movimiento> movimientos = (productoCodigo == null || productoCodigo.isBlank())
                ? repositorio.findAllByOrderByFechaDesc()
                : repositorio.findByProductoCodigoOrderByFechaDesc(productoCodigo.trim().toUpperCase());
        return movimientos.stream().map(MovimientoResponse::desde).toList();
    }

    public MovimientoResponse obtener(Long id) {
        return repositorio.findById(id)
                .map(MovimientoResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el movimiento " + id));
    }

    /**
     * Aplica el movimiento al stock del insumo y lo registra. Si el registro falla después de haber
     * ajustado el stock, se revierte el ajuste para no dejar los datos inconsistentes.
     */
    public MovimientoResponse registrar(MovimientoRequest datos) {
        String codigo = datos.productoCodigo().trim().toUpperCase();
        ProductoRemoto producto = productos.obtener(codigo);

        int delta = datos.tipo().esSalida() ? -datos.cantidad() : datos.cantidad();
        ProductoRemoto actualizado = productos.ajustarStock(codigo, delta);

        Movimiento movimiento = new Movimiento();
        movimiento.setProductoCodigo(codigo);
        movimiento.setProductoNombre(producto.nombre());
        movimiento.setUnidadMedida(producto.unidadMedida());
        movimiento.setTipo(datos.tipo());
        movimiento.setCantidad(datos.cantidad());
        movimiento.setStockResultante(actualizado.stockActual());
        movimiento.setFecha(System.currentTimeMillis());
        movimiento.setMotivo(datos.motivo().trim());
        movimiento.setPerfilResponsable(datos.perfilResponsable());

        try {
            return MovimientoResponse.desde(repositorio.save(movimiento));
        } catch (RuntimeException fallo) {
            revertirAjuste(codigo, delta);
            throw fallo;
        }
    }

    private void revertirAjuste(String codigo, int delta) {
        try {
            productos.ajustarStock(codigo, -delta);
        } catch (RuntimeException e) {
            LOG.error("No se pudo revertir el ajuste de stock de {}: {}", codigo, e.getMessage());
        }
    }
}
