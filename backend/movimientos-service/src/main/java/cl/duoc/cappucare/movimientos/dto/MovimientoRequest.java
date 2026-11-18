package cl.duoc.cappucare.movimientos.dto;

import cl.duoc.cappucare.movimientos.model.TipoMovimiento;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MovimientoRequest(
        @NotBlank(message = "El insumo es obligatorio")
        String productoCodigo,

        @NotNull(message = "El tipo de movimiento es obligatorio")
        TipoMovimiento tipo,

        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        @Max(value = 1_000_000, message = "La cantidad no puede superar 1.000.000")
        int cantidad,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(min = 3, max = 200, message = "El motivo debe tener entre 3 y 200 caracteres")
        String motivo,

        @NotBlank(message = "El responsable es obligatorio")
        @Pattern(regexp = "Bodeguero|Vendedor|Administrador", message = "El responsable debe ser Bodeguero, Vendedor o Administrador")
        String perfilResponsable
) {
}
