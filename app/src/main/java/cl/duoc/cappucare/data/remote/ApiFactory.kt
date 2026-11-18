package cl.duoc.cappucare.data.remote

import cl.duoc.cappucare.BuildConfig
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Construye los clientes Retrofit. Las URL base se configuran en gradle.properties. */
object ApiFactory {

    private val cliente: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                }
            }
            .build()
    }

    private fun retrofit(urlBase: String): Retrofit = Retrofit.Builder()
        .baseUrl(urlBase)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun productos(): ProductosApi = retrofit(BuildConfig.PRODUCTOS_URL).create(ProductosApi::class.java)

    fun movimientos(): MovimientosApi = retrofit(BuildConfig.MOVIMIENTOS_URL).create(MovimientosApi::class.java)

    fun indicadores(): IndicadoresApi = retrofit(BuildConfig.INDICADORES_URL).create(IndicadoresApi::class.java)
}
