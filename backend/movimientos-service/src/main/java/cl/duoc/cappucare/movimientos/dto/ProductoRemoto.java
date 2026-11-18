package cl.duoc.cappucare.movimientos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Vista mínima de un insumo, tal como lo entrega productos-service. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductoRemoto(
        String codigo,
        String nombre,
        String unidadMedida,
        int stockActual,
        int stockMinimo
) {
}
