package com.muchbeer.imageuploaderktx

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.muchbeer.imageuploaderktx.api.UploadInstance
import com.muchbeer.imageuploaderktx.databinding.ActivityMainBinding
import com.muchbeer.imageuploaderktx.util.UriFileConverter
import com.muchbeer.imageuploaderktx.util.getFileName
import com.muchbeer.imageuploaderktx.util.getRealPathFromURI
import com.muchbeer.imageuploaderktx.util.snackbar
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*
import java.net.URI


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private var selectedImageUri: Uri? = null
   // private var selectedImageFile : File? = null

   private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess->

        if (isSuccess) {
            selectedImageUri?.let { uri ->
                binding.imageView.setImageURI(uri)
            }
        }
    }

    private val takePhotoImg = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap->
        bitmap?.let {

          val base64Is =  BaseConverter().getBase64FromBitmap(it)
            Log.d(TAG, "tHE base64 : ${base64Is}")

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            imageView.setOnClickListener {
              takeImage()
              //  takePhotoImg.launch(null)
            }

            buttonUpload.setOnClickListener {
                simplifiedUploadImage()
          /*     selectedImageUri?.let { uri->

                  //  uploadImageOkhttp(file)
                   uploadImage()
                }*/
             //  uploadImage()
            }
        }
    }


    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getFileUri("muchbeer").let { uri ->
                selectedImageUri = uri
                takePhoto.launch(uri)
                Log.d(TAG, "The name of the file is : ${uri.path}")
            }
        }
    }



    private fun getFileUri(fileName: String): Uri {
        val tmpFile = File.createTempFile(fileName, ".jpg", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }



    private fun simplifiedUploadImage() {

        if (selectedImageUri == null) {
            binding.layoutRoot.snackbar("Select an Image First")
            return
        }

        binding.progressBar.progress = 0
        val parcelFileDescriptor =
            contentResolver.openFileDescriptor(selectedImageUri!!, "r", null) ?: return


        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(cacheDir, contentResolver.getFileName(selectedImageUri!!))
     //   val file = getRealPathFromURI(this, selectedImageUri!!)
        Log.d(TAG, "tHE filename to be sent is : ${file.absolutePath}")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        //option two
        val mimeType = getMimeType(file)
      //  val description_reqbody = description.toRequestBody("text/plain;charset=utf-8".toMediaType())
        val descBody  = file.name.toRequestBody("text/plain;charset=utf-8".toMediaType())
        val requestFileBody = file.asRequestBody(contentType = mimeType!!.toMediaTypeOrNull())

       // val fileName = getRealPathFromURI(this, selectedImageUri!!)

        lifecycleScope.launchWhenStarted {

            val body = UploadRequestBody(file, "image") { percentage ->
                binding.progressBar.progress = percentage
            }

            try {
             val uploadImage =  UploadInstance.create().uploadSuspendImage(
                    MultipartBody.Part.createFormData(
                        name="image",
                       filename= file.name,
                       body = requestFileBody
                    ),
                    desc = descBody
                )

                if (uploadImage.isSuccessful) {
                    uploadImage.body()!!.let {
                        binding.layoutRoot.snackbar(it.message)
                        binding.progressBar.progress = 100
                        Log.d(TAG, "The json retrieved is : ${it.image}")
                    }
                } else {
                    binding.layoutRoot.snackbar(uploadImage.message())
                    binding.progressBar.progress = 0
                    Log.d(TAG, "receive Error : ${uploadImage.message()}")
                }
            } catch (ex : IOException) {
                Log.d(TAG, "eRROR IS : ${ex.message}")
            }
        }
    }

    fun getMimeType(file: File): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(file.path)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    companion object {
        private  val TAG = MainActivity::class.simpleName
    }


}