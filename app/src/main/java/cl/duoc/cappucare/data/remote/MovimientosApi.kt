package cl.duoc.cappucare.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Cliente de movimientos-service (puerto 8082). */
interface MovimientosApi {

    @GET("api/movimientos")
    suspend fun listar(): List<MovimientoDto>

    @POST("api/movimientos")
    suspend fun crear(@Body movimiento: MovimientoRequestDto): MovimientoDto
}
