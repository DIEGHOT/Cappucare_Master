package cl.duoc.cappucare.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movimientos",
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["codigo"],
            childColumns = ["productoCodigo"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productoCodigo")]
)
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productoCodigo: String,
    val tipo: String,
    val cantidad: Int,
    val fecha: Long,
    val motivo: String,
    val perfilResponsable: String
)
