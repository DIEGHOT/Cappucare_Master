package cl.duoc.cappucare.data.remote

import retrofit2.http.GET

/** API externa pública de indicadores económicos de Chile: https://mindicador.cl */
interface IndicadoresApi {

    @GET("api")
    suspend fun obtener(): IndicadoresDto
}
