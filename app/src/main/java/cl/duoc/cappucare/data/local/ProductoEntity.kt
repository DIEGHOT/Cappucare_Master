package cl.duoc.cappucare.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Copia local (caché) de un insumo del microservicio de productos. */
@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey val codigo: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val stockActual: Int,
    val stockMinimo: Int,
    val unidadMedida: String,
    val ubicacion: String,
    val fotoUri: String?
)
