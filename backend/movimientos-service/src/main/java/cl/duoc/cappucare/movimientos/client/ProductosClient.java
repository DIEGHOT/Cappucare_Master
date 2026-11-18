package cl.duoc.cappucare.movimientos.client;

import cl.duoc.cappucare.movimientos.dto.ProductoRemoto;

/** Acceso al microservicio de insumos. Es una interfaz para poder simularla en las pruebas. */
public interface ProductosClient {

    ProductoRemoto obtener(String codigo);

    /** Aplica una variación de stock y devuelve el insumo ya actualizado. */
    ProductoRemoto ajustarStock(String codigo, int delta);
}
