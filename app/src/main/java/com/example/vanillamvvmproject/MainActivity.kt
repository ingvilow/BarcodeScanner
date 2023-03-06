package com.example.vanillamvvmproject

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager

import android.graphics.Bitmap

import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.Permission


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn = findViewById<Button>(R.id.btn1)
       /* val btn2 = findViewById<Button>(R.id.btn2)
        btn2.setOnClickListener(){

        }*/

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



    fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) !==
            PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    android.Manifest.permission.CAMERA
                )
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(android.Manifest.permission.CAMERA), MainActivity.REQUEST_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(android.Manifest.permission.CAMERA), MainActivity.REQUEST_CODE
                )
            }
        }else{
            capturePhoto()
        }
    }

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
            REQUEST_CODE ->{
                if(grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    if ((ContextCompat.checkSelfPermission(this@MainActivity,
                            android.Manifest.permission.CAMERA) ===
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
                        capturePhoto()
                    }
                }else{
                    Toast.makeText(this, "I was ignored", Toast.LENGTH_LONG).show()
                }
            }
        }
        return
    }
    fun capturePhoto(){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CODE)
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
                    resolver.openOutputStream(uri).use { outputStream ->
                        outputStream?.let {
                            photo.compress(Bitmap.CompressFormat.PNG, 100, it)
                        }
                    }
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



    companion object{
        const val CAMERA_RESULT_TAKEN_PHOTO = 1
        const val  REQUEST_CODE = 1
        const val PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    }
}







