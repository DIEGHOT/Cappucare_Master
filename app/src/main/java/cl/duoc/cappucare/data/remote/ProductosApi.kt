package cl.duoc.cappucare.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Cliente de productos-service (puerto 8081). */
interface ProductosApi {

    @GET("api/productos")
    suspend fun listar(): List<ProductoDto>

    @GET("api/productos/{codigo}")
    suspend fun obtener(@Path("codigo") codigo: String): ProductoDto

    @POST("api/productos")
    suspend fun crear(@Body producto: ProductoDto): ProductoDto

    @PUT("api/productos/{codigo}")
    suspend fun actualizar(@Path("codigo") codigo: String, @Body producto: ProductoDto): ProductoDto

    @DELETE("api/productos/{codigo}")
    suspend fun eliminar(@Path("codigo") codigo: String)
}
