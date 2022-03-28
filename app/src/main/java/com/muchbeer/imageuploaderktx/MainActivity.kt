package com.muchbeer.imageuploaderktx

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.muchbeer.imageuploaderktx.api.Repository
import com.muchbeer.imageuploaderktx.databinding.ActivityMainBinding
import com.muchbeer.imageuploaderktx.util.snackbar
import java.io.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private var selectedImageUri: Uri? = null
    private var selectedImageFile : File? = null

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            imageView.setOnClickListener {
              takeImage()
            }

            buttonUpload.setOnClickListener {
                uploadImageToServer()
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
        val tmpFile = File.createTempFile(fileName, ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        Log.d(TAG, "tHE name of the file is :${tmpFile.absolutePath}")
        selectedImageFile = tmpFile
        return FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }



    private fun uploadImageToServer() {

        if (selectedImageUri == null) {
            binding.layoutRoot.snackbar("Select an Image First")
            return
        }

        lifecycleScope.launchWhenStarted {
            Log.d(TAG, "The file is : ${selectedImageFile!!.absolutePath}")
            Repository().okhttpRun(selectedImageFile)
        }
    }


    companion object {
        private  val TAG = MainActivity::class.simpleName
    }


}