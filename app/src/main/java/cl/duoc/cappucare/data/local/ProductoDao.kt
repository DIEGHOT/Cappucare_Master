package cl.duoc.cappucare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ProductoDao {

    @Query("SELECT * FROM productos ORDER BY nombre COLLATE NOCASE ASC")
    abstract fun observarTodos(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE codigo = :codigo LIMIT 1")
    abstract suspend fun obtenerPorCodigo(codigo: String): ProductoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertar(producto: ProductoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarTodos(productos: List<ProductoEntity>)

    @Query("DELETE FROM productos WHERE codigo = :codigo")
    abstract suspend fun eliminarPorCodigo(codigo: String)

    @Query("DELETE FROM productos")
    abstract suspend fun eliminarTodos()

    /** Reemplaza todo el caché de forma atómica con los datos recién descargados. */
    @Transaction
    open suspend fun reemplazarTodos(productos: List<ProductoEntity>) {
        eliminarTodos()
        insertarTodos(productos)
    }
}
