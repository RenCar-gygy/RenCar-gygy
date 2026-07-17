package com.turkcell.rencarapp.data.network

import com.turkcell.rencarapp.data.network.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import com.turkcell.rencarapp.data.network.api.IyzicoApi
import com.turkcell.rencarapp.data.payment.DefaultIyzicoRepository
import com.turkcell.rencarapp.data.payment.IyzicoRepository

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://rencarv2.halitkalayci.com/"

    @Provides
    @Singleton
    @Named("socketLocationsUrl")
    fun provideSocketLocationsUrl(): String = BASE_URL.trimEnd('/') + "/ws/locations"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(json: Json, okHttpClient: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideLicenseApi(retrofit: Retrofit): LicenseApi = retrofit.create(LicenseApi::class.java)

    @Provides
    @Singleton
    fun provideVehicleApi(retrofit: Retrofit): VehicleApi = retrofit.create(VehicleApi::class.java)

    @Provides
    @Singleton
    fun provideRentalApi(retrofit: Retrofit): RentalApi = retrofit.create(RentalApi::class.java)

    @Provides
    @Singleton
    fun provideReservationApi(retrofit: Retrofit): ReservationApi = retrofit.create(ReservationApi::class.java)

    @Provides
    @Singleton
    fun provideWalletApi(retrofit: Retrofit): WalletApi = retrofit.create(WalletApi::class.java)

    @Provides
    @Singleton
    fun provideCardApi(retrofit: Retrofit): CardApi = retrofit.create(CardApi::class.java)

    @Provides
    @Singleton
    fun provideIyzicoApi(retrofit: Retrofit): IyzicoApi {
        return retrofit.create(IyzicoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideIyzicoRepository(api: IyzicoApi): IyzicoRepository {
        return DefaultIyzicoRepository(api)
    }
}