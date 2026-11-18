package cl.duoc.cappucare.movimientos.dto;

import cl.duoc.cappucare.movimientos.model.Movimiento;
import cl.duoc.cappucare.movimientos.model.TipoMovimiento;

public record MovimientoResponse(
        Long id,
        String productoCodigo,
        String productoNombre,
        String unidadMedida,
        TipoMovimiento tipo,
        int cantidad,
        int stockResultante,
        long fecha,
        String motivo,
        String perfilResponsable
) {
    public static MovimientoResponse desde(Movimiento m) {
        return new MovimientoResponse(
                m.getId(), m.getProductoCodigo(), m.getProductoNombre(), m.getUnidadMedida(),
                m.getTipo(), m.getCantidad(), m.getStockResultante(), m.getFecha(),
                m.getMotivo(), m.getPerfilResponsable());
    }
}
