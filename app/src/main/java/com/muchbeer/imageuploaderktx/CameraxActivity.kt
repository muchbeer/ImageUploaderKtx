package com.muchbeer.imageuploaderktx

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.muchbeer.imageuploaderktx.api.Repository
import com.muchbeer.imageuploaderktx.api.UploadInstance
import com.muchbeer.imageuploaderktx.databinding.ActivityCameraxBinding
import com.muchbeer.imageuploaderktx.model.ImageResponse
import com.muchbeer.imageuploaderktx.util.snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Multipart
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import retrofit2.converter.gson.GsonConverterFactory.create
import java.util.concurrent.TimeUnit


class CameraxActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraxBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService
        private var selectedImageFile : File? = null
    private var selectedImageUri : Uri? = null

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isPermissionGrant ->
            if(isPermissionGrant) {
                startCamera()
            } else {
                Snackbar.make(binding.root, "Permission is required",
                    Snackbar.LENGTH_LONG).show()
               // ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        requestCameraPermission.launch(android.Manifest.permission.CAMERA)


        binding.apply {
            imgCaptureBtn.setOnClickListener {
                takePhoto()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    animateFlash()
                }
            }

            uploadBtn.setOnClickListener {
           // uploadCameraxImage()
            //  uploadImageCall()
                okHttpUpload()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun animateFlash() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50)
        }, 100)
    }

    private fun takePhoto() {

        imageCapture?.let {
            val fileName = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".png"
            val fileName2 = "JPEG_${System.currentTimeMillis()}.png"
            val file = File(externalMediaDirs[0], fileName)
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
            it.takePicture(
                outputFileOptions,
                imgCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val savedUri = Uri.fromFile(file)
                        Log.d(TAG, "The image uri has been saved in ${file.toUri()}")
                        Log.d(TAG, "The image file has been saved in ${file.absolutePath}")
                        selectedImageFile = file
                        selectedImageUri = file.toUri()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(
                            binding.root.context,
                            "Error taking photo",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d(TAG, "Error taking photo:$exception")
                    }

                })
        }
    }

    private fun okHttpUpload() {

        if (selectedImageFile == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launchWhenStarted() {
            Repository().okhttpRun(selectedImageFile)
        }
    }

    private fun uploadImageCall() {
        if (selectedImageFile == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_LONG).show()
            return
        }

        val parcelFileDescriptor =
            contentResolver.openFileDescriptor(selectedImageUri!!, "r", null) ?: return

        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val outputStream = FileOutputStream(selectedImageFile)
        inputStream.copyTo(outputStream)

        // Parsing any Media type file
        // Parsing any Media type file

        val body = UploadRequestBody(selectedImageFile!!, "image") { percentage ->
           Log.d(TAG, "pERCENTAGE done is : ${percentage.toString()}")
        }
        val descBody  = selectedImageFile!!.name.toRequestBody("text/plain;charset=utf-8".toMediaType())
        val nameBody = selectedImageFile!!.name.toRequestBody("text/plain".toMediaTypeOrNull())
        val requestFileBody = selectedImageFile!!.asRequestBody(contentType = "image/*".toMediaTypeOrNull())

        lifecycleScope.launchWhenStarted {
            Log.d(TAG, "oN uPLOADING : the file is : ${selectedImageFile}")

            try {
              val checkResult =  UploadInstance.create().uploadImageBest(
                    file =   MultipartBody.Part.createFormData(
                        name = "image",
                        filename = selectedImageFile!!.name,
                        body = requestFileBody
                    ),
                    name =  nameBody
                )
                if (checkResult.isSuccessful) {
                val serverResponse = checkResult.body()
                    if (serverResponse !=null) {
                        if(serverResponse.success) {
                            Log.d(TAG, "Retrieve response is: ${serverResponse.message}")
                        } else Log.d(TAG, "eRROR message is : ${serverResponse.message}")
                    }

                } else Log.d(TAG, "tHE error is ${checkResult.message()}")

            } catch (io : IOException) {
                Log.d(TAG, "IOeRROR IS : ${io.message}")
            }
        }
    }

    private fun uploadCameraxImage() {
        if (selectedImageFile == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_LONG).show()
            return
        }
        val mimeType = getMimeType(selectedImageFile!!)
        //  val description_reqbody = description.toRequestBody("text/plain;charset=utf-8".toMediaType())
        val descBody  = selectedImageFile!!.name.toRequestBody("text/plain;charset=utf-8".toMediaType())
        val requestFileBody = selectedImageFile!!.asRequestBody(contentType = "image/jpeg".toMediaTypeOrNull())

        lifecycleScope.launchWhenStarted {
            try {
                val uploadImage =  UploadInstance.create().uploadSuspendImage(
                    MultipartBody.Part.createFormData(
                        name="image",
                        filename= selectedImageFile!!.name,
                        body = requestFileBody
                    ),
                    desc = descBody
                )

                if (uploadImage.isSuccessful) {
                    uploadImage.body()!!.let {
                       // binding.layoutRoot.snackbar(it.message)
                       // binding.progressBar.progress = 100
                        Log.d(TAG, "The json retrieved is : ${it.image}")
                    }
                } else {
                 //   binding.layoutRoot.snackbar(uploadImage.message())
                 //   binding.progressBar.progress = 0
                    Log.d(TAG, "receive Error : ${uploadImage.message()}")
                }
            }catch (ex : IOException) {
                Log.d(TAG, "eRROR IS : ${ex.message}")
            }
        }

    }

    private fun startCamera() {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.preview.surfaceProvider)
        }
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.d(TAG, "Use case binding failed")
            }
        }, ContextCompat.getMainExecutor(this))
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
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val TAG = MainActivity::class.simpleName
        private val MEDIA_TYPE_PNG = "image/png".toMediaType()

    }
}