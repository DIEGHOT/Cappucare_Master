package cl.duoc.cappucare.productos.config;

import cl.duoc.cappucare.productos.model.Producto;
import cl.duoc.cappucare.productos.repository.ProductoRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Catálogo simulado (datos ficticios) que se carga solo si la tabla está vacía. */
@Configuration
public class CargaInicial {

    @Bean
    CommandLineRunner cargarCatalogo(ProductoRepository repositorio) {
        return args -> {
            if (repositorio.count() > 0) {
                return;
            }
            repositorio.saveAll(List.of(
                    producto("INS-001", "Guantes de nitrilo talla M", "Caja de 100 guantes sin polvo", "Equipos de protección", 48, 20, "Caja", "Pasillo 1 - Estante A"),
                    producto("INS-002", "Mascarilla quirúrgica 3 capas", "Caja de 50 unidades", "Equipos de protección", 12, 15, "Caja", "Pasillo 1 - Estante B"),
                    producto("INS-003", "Gasa estéril 10x10 cm", "Paquete de 25 sobres", "Material de curación", 80, 30, "Paquete", "Pasillo 2 - Estante A"),
                    producto("INS-004", "Jeringa desechable 5 ml", "Caja de 100 unidades", "Insumos quirúrgicos", 9, 10, "Caja", "Pasillo 2 - Estante C"),
                    producto("INS-005", "Termómetro digital infrarrojo", "Sin contacto, pantalla LCD", "Diagnóstico", 25, 8, "Unidad", "Pasillo 3 - Estante A"),
                    producto("INS-006", "Alcohol gel 500 ml", "Antiséptico para manos", "Material de curación", 60, 25, "Unidad", "Pasillo 3 - Estante B"),
                    producto("INS-007", "Pinza Kocher 14 cm", "Acero inoxidable", "Instrumental", 14, 5, "Unidad", "Pasillo 4 - Estante A"),
                    producto("INS-008", "Tensiómetro de brazo", "Medición automática", "Diagnóstico", 5, 5, "Unidad", "Pasillo 4 - Estante B")
            ));
        };
    }

    private Producto producto(String codigo, String nombre, String descripcion, String categoria,
                              int stock, int minimo, String unidad, String ubicacion) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setCategoria(categoria);
        p.setStockActual(stock);
        p.setStockMinimo(minimo);
        p.setUnidadMedida(unidad);
        p.setUbicacion(ubicacion);
        return p;
    }
}
