package com.muchbeer.imageuploaderktx.api

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.muchbeer.imageuploaderktx.BuildConfig
import com.muchbeer.imageuploaderktx.CameraxActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class Repository {

    private val client = OkHttpClient()

    suspend fun okhttpRun( file : File?) {
        val requestFileBody = file!!.asRequestBody(contentType = "image/*".toMediaTypeOrNull())


        // Use the imgur image upload API as documented at https://api.imgur.com/endpoints/image
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("desc", "Giovanna")
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


            Log.d(TAG, "oN uPLOADING : the file is : ${file}")

            try {
                client

                    .newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        //println(response.body!!.string())
                        Log.d(TAG, "SUCCESS RESPONSE IS : ${response.body.toString()}")
                    }
                }
            } catch (io: IOException) {
                Log.d(TAG, "Unexpected error is : ${io.message}")
            }


    }

    companion object {
        private val TAG = Repository::class.simpleName
    }
}