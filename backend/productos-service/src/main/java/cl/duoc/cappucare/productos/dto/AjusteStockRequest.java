package cl.duoc.cappucare.productos.dto;

import jakarta.validation.constraints.NotNull;

/** Variación de stock: positiva para ingresos, negativa para salidas. */
public record AjusteStockRequest(@NotNull(message = "El ajuste es obligatorio") Integer delta) {
}
