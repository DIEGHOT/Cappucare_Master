package cl.duoc.cappucare.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Copia local (caché) de un movimiento del microservicio de movimientos. El id lo asigna el servidor.
 * Guarda nombre y unidad del insumo para que el historial siga legible aunque el insumo se elimine.
 */
@Entity(tableName = "movimientos")
data class MovimientoEntity(
    @PrimaryKey val id: Long,
    val productoCodigo: String,
    val productoNombre: String,
    val unidadMedida: String,
    val tipo: String,
    val cantidad: Int,
    val stockResultante: Int,
    val fecha: Long,
    val motivo: String,
    val perfilResponsable: String
)
