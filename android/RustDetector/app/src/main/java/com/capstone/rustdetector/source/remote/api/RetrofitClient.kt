package com.capstone.rustdetector.source.remote.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://34.101.204.148/"

    // by lazy -> singleton pattern. only created once
    private val retrofit by lazy {
        val loggingInterceptor =
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            .connectTimeout(400, TimeUnit.SECONDS)
            .readTimeout(400, TimeUnit.SECONDS)
            .writeTimeout(400, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val instanceCorrosionSegmentationApi : CorrosionSegmentationApi by lazy {
        retrofit.create(CorrosionSegmentationApi::class.java)
    }
}