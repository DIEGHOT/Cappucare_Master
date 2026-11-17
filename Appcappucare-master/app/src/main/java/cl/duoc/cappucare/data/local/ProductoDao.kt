package cl.duoc.cappucare.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Query("SELECT * FROM productos ORDER BY nombre COLLATE NOCASE ASC")
    fun observarTodos(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE codigo = :codigo")
    fun observarPorCodigo(codigo: String): Flow<ProductoEntity?>

    @Query("SELECT * FROM productos WHERE codigo = :codigo LIMIT 1")
    suspend fun obtenerPorCodigo(codigo: String): ProductoEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(producto: ProductoEntity)

    @Update
    suspend fun actualizar(producto: ProductoEntity)

    @Delete
    suspend fun eliminar(producto: ProductoEntity)

    @Query("DELETE FROM productos WHERE codigo = :codigo")
    suspend fun eliminarPorCodigo(codigo: String)

    @Query("UPDATE productos SET stockActual = :nuevoStock WHERE codigo = :codigo")
    suspend fun actualizarStock(codigo: String, nuevoStock: Int)
}
