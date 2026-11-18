package cl.duoc.cappucare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MovimientoDao {

    @Query("SELECT * FROM movimientos ORDER BY fecha DESC")
    abstract fun observarTodos(): Flow<List<MovimientoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertar(movimiento: MovimientoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarTodos(movimientos: List<MovimientoEntity>)

    @Query("DELETE FROM movimientos")
    abstract suspend fun eliminarTodos()

    /** Reemplaza todo el caché de forma atómica con los datos recién descargados. */
    @Transaction
    open suspend fun reemplazarTodos(movimientos: List<MovimientoEntity>) {
        eliminarTodos()
        insertarTodos(movimientos)
    }
}
