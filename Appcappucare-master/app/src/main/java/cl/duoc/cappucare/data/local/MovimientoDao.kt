package cl.duoc.cappucare.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {

    @Query("SELECT * FROM movimientos ORDER BY fecha DESC")
    fun observarTodos(): Flow<List<MovimientoEntity>>

    @Query("SELECT * FROM movimientos WHERE productoCodigo = :codigo ORDER BY fecha DESC")
    fun observarPorProducto(codigo: String): Flow<List<MovimientoEntity>>

    @Insert
    suspend fun insertar(movimiento: MovimientoEntity): Long

    @Update
    suspend fun actualizar(movimiento: MovimientoEntity)

    @Delete
    suspend fun eliminar(movimiento: MovimientoEntity)
}
