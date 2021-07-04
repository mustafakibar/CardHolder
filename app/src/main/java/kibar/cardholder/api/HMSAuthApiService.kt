package kibar.cardholder.api

import kibar.cardholder.model.TokenResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface HMSAuthApiService {

    @FormUrlEncoded
    @POST("/oauth2/v3/token")
    suspend fun requestToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): TokenResponse

}

