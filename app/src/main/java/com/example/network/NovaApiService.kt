package com.example.network

import com.example.model.ChatApiRequest
import com.example.model.ChatApiResponse
import com.example.model.HealthApiResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface NovaApiService {
    @POST("api/chat")
    suspend fun sendChatMessage(
        @Body request: ChatApiRequest
    ): ChatApiResponse

    @GET("api/health")
    suspend fun checkHealth(): HealthApiResponse
}

object ApiClient {
    // Default emulator host pointing to computer localhost:3000
    const val DEFAULT_BASE_URL = "http://10.0.2.2:3000/"

    private var currentBaseUrl = DEFAULT_BASE_URL
    private var cachedService: NovaApiService? = null

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    fun getService(baseUrl: String? = null): NovaApiService {
        val targetUrl = normalizeUrl(baseUrl ?: currentBaseUrl)
        if (cachedService != null && targetUrl == currentBaseUrl) {
            return cachedService!!
        }

        currentBaseUrl = targetUrl
        val retrofit = Retrofit.Builder()
            .baseUrl(targetUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val service = retrofit.create(NovaApiService::class.java)
        cachedService = service
        return service
    }

    fun updateBaseUrl(newUrl: String) {
        currentBaseUrl = normalizeUrl(newUrl)
        cachedService = null
    }

    fun getBaseUrl(): String = currentBaseUrl

    private fun normalizeUrl(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }
}
