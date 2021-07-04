package kibar.cardholder.di

import android.content.Context
import com.huawei.secure.android.common.ssl.SecureSSLSocketFactory
import com.huawei.secure.android.common.ssl.SecureX509TrustManager
import com.huawei.secure.android.common.ssl.hostname.StrictHostnameVerifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kibar.cardholder.api.BinApiService
import kibar.cardholder.api.HMSAuthApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private fun OkHttpClient.Builder.injectDefaults() = apply {
        addInterceptor(HttpLoggingInterceptor().also { interceptor ->
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        })
        callTimeout(40, TimeUnit.SECONDS)
        connectTimeout(40, TimeUnit.SECONDS)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .injectDefaults()
        .build()

    @Provides
    @Singleton
    fun provideApiService(okHttpClient: OkHttpClient): BinApiService = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://lookup.binlist.net")
        .client(okHttpClient)
        .build()
        .create(BinApiService::class.java)

    @Provides
    @Singleton
    fun provideHMAuthService(@ApplicationContext context: Context): HMSAuthApiService {

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://oauth-login.cloud.huawei.com")
            .client(
                OkHttpClient.Builder()
                    .injectDefaults()
                    .sslSocketFactory(
                        SecureSSLSocketFactory.getInstance(context),
                        SecureX509TrustManager(context)
                    )
                    .hostnameVerifier(StrictHostnameVerifier())
                    .retryOnConnectionFailure(true)
                    .build()
            )
            .build()
            .create(HMSAuthApiService::class.java)
    }

}