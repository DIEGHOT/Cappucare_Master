package cl.duoc.cappucare.movimientos.repository;

import cl.duoc.cappucare.movimientos.model.Movimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findAllByOrderByFechaDesc();

    List<Movimiento> findByProductoCodigoOrderByFechaDesc(String productoCodigo);
}
