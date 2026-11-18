package cl.duoc.cappucare.productos.service;

import cl.duoc.cappucare.productos.dto.ProductoRequest;
import cl.duoc.cappucare.productos.dto.ProductoResponse;
import cl.duoc.cappucare.productos.exception.ConflictoException;
import cl.duoc.cappucare.productos.exception.RecursoNoEncontradoException;
import cl.duoc.cappucare.productos.model.Producto;
import cl.duoc.cappucare.productos.repository.ProductoRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository repositorio;

    public ProductoService(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return repositorio.findAll(Sort.by("nombre")).stream().map(ProductoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(String codigo) {
        return ProductoResponse.desde(buscar(codigo));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest datos) {
        String codigo = normalizar(datos.codigo());
        if (repositorio.existsById(codigo)) {
            throw new ConflictoException("Ya existe un insumo con el código " + codigo);
        }
        Producto producto = new Producto();
        producto.setCodigo(codigo);
        aplicar(producto, datos);
        return ProductoResponse.desde(repositorio.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(String codigo, ProductoRequest datos) {
        Producto producto = buscar(codigo);
        aplicar(producto, datos);
        return ProductoResponse.desde(repositorio.save(producto));
    }

    @Transactional
    public void eliminar(String codigo) {
        repositorio.delete(buscar(codigo));
    }

    /** Suma (delta positivo) o resta (delta negativo) unidades; nunca deja el stock bajo cero. */
    @Transactional
    public ProductoResponse ajustarStock(String codigo, int delta) {
        Producto producto = repositorio.buscarParaActualizar(normalizar(codigo))
                .orElseThrow(() -> noExiste(codigo));
        int nuevoStock = producto.getStockActual() + delta;
        if (nuevoStock < 0) {
            throw new ConflictoException("Stock insuficiente. Disponible: "
                    + producto.getStockActual() + " " + producto.getUnidadMedida());
        }
        producto.setStockActual(nuevoStock);
        return ProductoResponse.desde(repositorio.save(producto));
    }

    private Producto buscar(String codigo) {
        return repositorio.findById(normalizar(codigo)).orElseThrow(() -> noExiste(codigo));
    }

    private RecursoNoEncontradoException noExiste(String codigo) {
        return new RecursoNoEncontradoException("No existe un insumo con el código " + codigo);
    }

    private String normalizar(String codigo) {
        return codigo.trim().toUpperCase();
    }

    private void aplicar(Producto producto, ProductoRequest datos) {
        producto.setNombre(datos.nombre().trim());
        producto.setDescripcion(datos.descripcion() == null ? "" : datos.descripcion().trim());
        producto.setCategoria(datos.categoria());
        producto.setStockActual(datos.stockActual());
        producto.setStockMinimo(datos.stockMinimo());
        producto.setUnidadMedida(datos.unidadMedida());
        producto.setUbicacion(datos.ubicacion().trim());
        producto.setFotoUri(datos.fotoUri());
    }
}
