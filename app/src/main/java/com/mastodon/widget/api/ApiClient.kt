package com.mastodon.widget.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Gson bypasses Kotlin constructors so non-null String fields can end up null.
// This adapter converts null JSON strings to "" so Room's bindText never gets null.
private object NullSafeStringAdapterFactory : TypeAdapterFactory {
    override fun <T : Any> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType != String::class.java) return null
        @Suppress("UNCHECKED_CAST")
        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T?) { out.value(value as String?) }
            override fun read(input: JsonReader): T {
                if (input.peek() == JsonToken.NULL) {
                    input.nextNull()
                    return "" as T
                }
                return input.nextString() as T
            }
        }
    }
}

private val gson: Gson = GsonBuilder()
    .registerTypeAdapterFactory(NullSafeStringAdapterFactory)
    .create()

object ApiClient {
    private var instance: MastodonApiService? = null
    private var baseUrl: String = "https://mastodon.social/api/v1/"

    fun getInstance(serverUrl: String = baseUrl, token: String? = null): MastodonApiService {
        baseUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"

        if (instance == null) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            instance = retrofit.create(MastodonApiService::class.java)
        }

        return instance!!
    }

    fun setBaseUrl(url: String) {
        baseUrl = if (url.endsWith("/")) url else "$url/"
        instance = null // Reset instance to apply new URL
    }

    fun reset() {
        instance = null
    }
}
