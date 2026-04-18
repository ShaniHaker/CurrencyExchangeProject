package com.example.currencysdk.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * RetrofitClient is responsible for building the single Retrofit instance
 * that the whole SDK uses to talk to the Flask backend.
 *
 * It is an "object" (Kotlin singleton), so the same instance is reused
 * throughout the app — this avoids creating a new HTTP client on every call.
 */
internal object RetrofitClient {

    // The base URL for the Flask API.
    // 10.0.2.2 is the special Android emulator address that points to
    // "localhost" on your development machine.
    private const val BASE_URL = "http://127.0.0.1:5000/"

    // The logging interceptor prints every HTTP request and response to
    // Android Studio's Logcat. Very useful while developing!
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttpClient is the actual HTTP client that sends requests over the network.
    // We attach the logging interceptor so we can see what's happening.
    // The timeouts are raised to 30 s because the Flask /convert endpoint
    // can take 5–10 s to respond; OkHttp's default is only 10 s, which
    // causes a SocketTimeoutException before the response arrives.
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // time to establish the TCP connection
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)    // time to wait for the server's response
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)   // time to send the request body
        .addInterceptor(loggingInterceptor)
        .build()

    // The Retrofit instance is created lazily — only the first time it is
    // accessed. "by lazy" ensures it is only built once even if multiple
    // parts of the app access it at the same time.
    val apiService: CurrencyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // auto-parses JSON
            .build()
            .create(CurrencyApiService::class.java)
    }
}
