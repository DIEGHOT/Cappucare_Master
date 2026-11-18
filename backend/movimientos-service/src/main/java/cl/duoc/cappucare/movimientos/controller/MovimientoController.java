package cl.duoc.cappucare.movimientos.controller;

import cl.duoc.cappucare.movimientos.dto.MovimientoRequest;
import cl.duoc.cappucare.movimientos.dto.MovimientoResponse;
import cl.duoc.cappucare.movimientos.service.MovimientoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService servicio;

    public MovimientoController(MovimientoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<MovimientoResponse> listar(@RequestParam(required = false) String productoCodigo) {
        return servicio.listar(productoCodigo);
    }

    @GetMapping("/{id}")
    public MovimientoResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponse registrar(@Valid @RequestBody MovimientoRequest datos) {
        return servicio.registrar(datos);
    }
}
