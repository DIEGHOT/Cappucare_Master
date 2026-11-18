package cl.duoc.cappucare.productos.controller;

import cl.duoc.cappucare.productos.dto.AjusteStockRequest;
import cl.duoc.cappucare.productos.dto.ProductoRequest;
import cl.duoc.cappucare.productos.dto.ProductoResponse;
import cl.duoc.cappucare.productos.service.ProductoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ProductoResponse> listar() {
        return servicio.listar();
    }

    @GetMapping("/{codigo}")
    public ProductoResponse obtener(@PathVariable String codigo) {
        return servicio.obtener(codigo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest datos) {
        return servicio.crear(datos);
    }

    @PutMapping("/{codigo}")
    public ProductoResponse actualizar(@PathVariable String codigo, @Valid @RequestBody ProductoRequest datos) {
        return servicio.actualizar(codigo, datos);
    }

    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String codigo) {
        servicio.eliminar(codigo);
    }

    /** Usado por movimientos-service para aplicar ingresos y salidas al stock. */
    @PostMapping("/{codigo}/ajustar-stock")
    public ProductoResponse ajustarStock(@PathVariable String codigo, @Valid @RequestBody AjusteStockRequest datos) {
        return servicio.ajustarStock(codigo, datos.delta());
    }
}
