package cl.duoc.cappucare.productos.dto;

import cl.duoc.cappucare.productos.model.Producto;

public record ProductoResponse(
        String codigo,
        String nombre,
        String descripcion,
        String categoria,
        int stockActual,
        int stockMinimo,
        String unidadMedida,
        String ubicacion,
        String fotoUri
) {
    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(
                p.getCodigo(), p.getNombre(), p.getDescripcion(), p.getCategoria(),
                p.getStockActual(), p.getStockMinimo(), p.getUnidadMedida(),
                p.getUbicacion(), p.getFotoUri());
    }
}
