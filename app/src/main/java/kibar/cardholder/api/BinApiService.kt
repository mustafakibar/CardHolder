package kibar.cardholder.api

import kibar.cardholder.model.BinResult
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface BinApiService {
    @Headers("Accept-Version: 3")
    @GET("/{number}")
    suspend fun getBin(@Path("number") binNumber: String): BinResult
}

