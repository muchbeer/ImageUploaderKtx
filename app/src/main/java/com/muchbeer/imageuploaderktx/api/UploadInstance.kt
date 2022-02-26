package com.muchbeer.imageuploaderktx.api

import android.util.Log
import com.google.gson.GsonBuilder
import com.muchbeer.imageuploaderktx.BuildConfig
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import com.google.gson.Gson





class UploadInstance {
    companion object {
        // private const val BASE_URL = "https://muchbeer.ngrok.io/family/Api.php/"
      //  private const val BASE_URL = "http://10.177.0.6:8080/family/"

        private val TAG = UploadInstance::class.simpleName

        fun create(): UploadService {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d(TAG, it)
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(UploadService::class.java)
        }
    }


}