package com.muchbeer.imageuploaderktx.api

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.muchbeer.imageuploaderktx.BuildConfig
import com.muchbeer.imageuploaderktx.CameraxActivity
import com.muchbeer.imageuploaderktx.MainActivity
import com.muchbeer.imageuploaderktx.model.ImageResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class Repository {

    val logger = HttpLoggingInterceptor {
        Log.d(TAG, it)
    }
    private val client = OkHttpClient.Builder()
        .connectTimeout(100, TimeUnit.SECONDS)
        .writeTimeout(100, TimeUnit.SECONDS)
        .readTimeout(100, TimeUnit.SECONDS)
        .callTimeout(100, TimeUnit.SECONDS)
        .addInterceptor(logger)
        .build()

    init {
        logger.level = HttpLoggingInterceptor.Level.BASIC
    }

    fun okhttpUploadFlow(file : File?) = flow{
        emit("")

    }
    suspend fun okhttpRun( file : File?) = coroutineScope {
        val requestFileBody = file!!.asRequestBody(contentType = MEDIA_TYPE_PNG)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("desc", "Gianna")
            .addFormDataPart(
                name = "image",
                filename = file.name,
                body= requestFileBody)
            .build()

        val request = Request.Builder()
            .header("Content-type", "application/json")
            .url(BuildConfig.BASE_URL+ "home/Api.php?apicall=upload")
            .post(requestBody)
            .build()

        launch(Dispatchers.IO) {
            try {
                client
                    .newCall(request).execute().use { response ->

                        if(!response.isSuccessful) throw IOException("Error is : ${response.message}") else
                       {
                           val gson2 = GsonBuilder().setPrettyPrinting()
                               .create()
                           val prettyJson = gson2.toJson(
                               JsonParser.parseString(response.body!!.string())
                           )

                           val imageLink = gson2.fromJson(prettyJson, ImageResponse::class.java)
                           Log.d(TAG, "SUCCESS LINK IS : ${imageLink.image}")
                        }
                    }
            } catch (io: IOException) {
                Log.d(TAG, "Unexpected error is : ${io.message}")
            }
        }

    }

    companion object {
        private val TAG = Repository::class.simpleName
        private val MEDIA_TYPE_PNG = "image/png".toMediaType()
    }
}