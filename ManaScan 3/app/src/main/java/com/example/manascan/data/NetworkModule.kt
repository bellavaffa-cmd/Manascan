package com.example.manascan.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the singleton Retrofit/OkHttp client used to talk to Scryfall.
 * Kept as a plain object (no DI framework) to keep the sample dependency-light;
 * swap for Hilt/Koin in a larger app.
 */
object NetworkModule {

    private const val BASE_URL = "https://api.scryfall.com/"

    // Scryfall's API etiquette asks integrations to identify themselves.
    private val identifyingHeadersInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("User-Agent", "ManaScan/1.0 (+https://github.com/example/manascan)")
            .header("Accept", "application/json")
            .build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(identifyingHeadersInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val scryfallApi: ScryfallApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ScryfallApi::class.java)
    }
}
