package cl.duoc.cappucare.productos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos que envía la app para crear o actualizar un insumo. */
public record ProductoRequest(
        @NotBlank(message = "El código es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9-]{3,20}$", message = "El código debe tener de 3 a 20 caracteres (letras, números o guiones)")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,

        @Size(max = 200, message = "La descripción no puede superar los 200 caracteres")
        String descripcion,

        @NotBlank(message = "La categoría es obligatoria")
        String categoria,

        @Min(value = 0, message = "El stock no puede ser negativo")
        @Max(value = 1_000_000, message = "El stock no puede superar 1.000.000")
        int stockActual,

        @Min(value = 1, message = "El nivel mínimo debe ser mayor a 0")
        @Max(value = 1_000_000, message = "El nivel mínimo no puede superar 1.000.000")
        int stockMinimo,

        @NotBlank(message = "La unidad de medida es obligatoria")
        String unidadMedida,

        @NotBlank(message = "La ubicación es obligatoria")
        @Size(min = 2, max = 100, message = "La ubicación debe tener entre 2 y 100 caracteres")
        String ubicacion,

        @Size(max = 500, message = "La URI de la foto es demasiado larga")
        String fotoUri
) {
}
