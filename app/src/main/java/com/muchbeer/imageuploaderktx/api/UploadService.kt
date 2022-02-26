package com.muchbeer.imageuploaderktx.api

import com.muchbeer.imageuploaderktx.model.FileResponse
import com.muchbeer.imageuploaderktx.model.ImageResponse
import com.muchbeer.imageuploaderktx.model.UploadModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface UploadService {

 //   @Headers("Content-type:application/json")
    @Multipart
    @POST("home/upload.php")
   suspend fun uploadImageBest(
     @Part file : MultipartBody.Part,
     @Part("file") name : RequestBody
    ): Response<FileResponse>

    @Headers("Content-type:application/json")
    @Multipart
    @POST("home/Api.php?apicall=upload")
    fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("desc") desc: String,
    ): Call<ImageResponse>


    @Multipart
    @POST("home/Api.php?apicall=upload")
   suspend fun uploadSuspendImage(
        @Part image: MultipartBody.Part,
        @Part("desc") desc: RequestBody
    ): Response<UploadModel>

  /*  companion object {
        operator fun invoke(): UploadService {
            return Retrofit.Builder()
                .baseUrl("https://muchbeer.ngrok.io/family/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UploadService::class.java)
        }
    }*/
}