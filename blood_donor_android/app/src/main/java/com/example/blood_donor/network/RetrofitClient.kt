package com.example.blood_donor.network

import com.example.blood_donor.data.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    

    private var tokenManager: TokenManager? = null
    private var _apiService: ApiService? = null

    fun initialize(manager: TokenManager) {
        tokenManager = manager
        resetClient()
    }

    fun resetClient() {
        _apiService = null
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        val token = runBlocking { tokenManager?.tokenFlow?.firstOrNull() }
        
        android.util.Log.d("RetrofitClient", "Token from TokenManager: $token")
        
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
            android.util.Log.d("RetrofitClient", "Added Authorization header")
        }
        
        chain.proceed(requestBuilder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ApiService
        get() {
            if (_apiService == null) {
                val savedUrl = runBlocking { tokenManager?.serverUrlFlow?.firstOrNull() }
                val resolvedUrl = if (!savedUrl.isNullOrBlank()) savedUrl else DEFAULT_URL
                val cleanUrl = if (resolvedUrl.endsWith("/")) resolvedUrl else "$resolvedUrl/"
                
                android.util.Log.d("RetrofitClient", "Building Retrofit with Base URL: $cleanUrl")
                
                _apiService = Retrofit.Builder()
                    .baseUrl(cleanUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)
            }
            return _apiService!!
        }
}
private const val DEFAULT_URL = "http://10.143.142.1:5000/"