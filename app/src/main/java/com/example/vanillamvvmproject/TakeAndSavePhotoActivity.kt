package com.example.vanillamvvmproject

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException

class TakeAndSavePhotoActivity : AppCompatActivity() {

    lateinit var btn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_take_and_save_photo)

        btn = findViewById(R.id.btn2)
        btn.setOnClickListener(){
            openCamera()
            capturePhoto()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val img = findViewById<ImageView>(R.id.img)
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CAMERA_RESULT_TAKEN_PHOTO && resultCode == Activity.RESULT_OK) {
            val photoBitmap = data?.extras?.get("data") as Bitmap
            img.setImageBitmap(photoBitmap)
            savePhoto(photoBitmap, data)
            savePhotoToGallery(data)
        }
    }

    //requests permissions for camera openning
    // it is not going to work without permissions
    fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) !==
            PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.CAMERA
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA), TakeAndSavePhotoActivity.REQUEST_CODE
                )
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.CAMERA), REQUEST_CODE)
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA), TakeAndSavePhotoActivity.REQUEST_CODE
                )
            }
        }else{
            capturePhoto()
        }

    }


    //check permission to camera and external storage
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
                }
                return
            }
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if ((ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.CAMERA) ===
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
                        capturePhoto()
                    }else{
                        Toast.makeText(this, "I was ignored", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        return
    }


    //after all given permission it is finally lead us to opened camera and allow take a pic
    fun capturePhoto(){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CODE)
    }

    // saves the photo to the gallery before checking which version of android
    // takes the photo to the home screen after the user clicks the check mark
    // also this big ass function checks the SDK and Android version,
    // checks if photo is not null and compress in to PNG (JPEG has quite shitty quality even if we put 100)
    private fun savePhoto(bitmap: Bitmap, data: Intent?) {
        val photo = data?.extras?.get("data") as Bitmap?
        if (photo != null) {
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val resolver = applicationContext.contentResolver
            val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            //create metadata for photo
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val imageUri = resolver.insert(imageCollection, contentValues)

            imageUri?.let { uri ->
                try {
                    //This opens an output stream to the specified uri, which represents the newly created image file.
                    // This output stream is used to write the compressed bitmap data from the photo object to the file.
                    // The use function ensures that the output stream is properly closed after the write operation is completed.
                    resolver.openOutputStream(uri).use { outputStream ->
                        outputStream?.let {
                            photo.compress(Bitmap.CompressFormat.PNG, 100, it)
                        }
                    }
                    // this if-block checks if ANDROID is Q or later version and based on this it saves images or not
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(uri, contentValues, null, null)
                    }
                    Toast.makeText(
                        applicationContext,
                        "Photo saved successfully!",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: IOException) {
                    Toast.makeText(
                        applicationContext,
                        "Failed to save photo",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Toast.makeText(applicationContext, "Failed to get photo", Toast.LENGTH_LONG).show()
        }
    }
    private fun savePhotoToGallery(data: Intent?) {
        val bitmap = data?.extras?.get("data") as Bitmap
        val photoUri = savePhoto(bitmap, data)
        if (photoUri != null) {
            Toast.makeText(this, "Photo saved to gallery", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to save photo to gallery", Toast.LENGTH_SHORT).show()
        }
    }

        companion object{
        const val REQUEST_CODE = 1
        const val CAMERA_RESULT_TAKEN_PHOTO = 1
        const val PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    }
}