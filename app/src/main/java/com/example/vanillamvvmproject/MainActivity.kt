package com.example.vanillamvvmproject

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    lateinit var barcodeView: DecoratedBarcodeView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       barcodeView = findViewById(R.id.barcode_image_result1)
        val btn = findViewById<Button>(R.id.btn1)
        val floating_btn = findViewById<FloatingActionButton>(R.id.floating_btn1)
        btn.setOnClickListener(){
            openCamera()
            val integrator = IntentIntegrator(this)
           // integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            integrator.setPrompt("Scan a barcode")
            integrator.setBarcodeImageEnabled(true);
            integrator.setCameraId(0)
            integrator.setBeepEnabled(true)
            integrator.setTorchEnabled(true)
            integrator.setOrientationLocked(false)
            integrator.initiateScan()


        }

        floating_btn.setOnClickListener(){
            val intent = Intent(this, TakeAndSavePhotoActivity::class.java)
            intent.putExtra("savePhoto", 1)
            startActivity(intent)
        }

    }

    private val barcodeLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val contents: String? =
            IntentIntegrator.parseActivityResult(result.resultCode, result.data).contents
        if (contents == null) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Scanned: $contents", Toast.LENGTH_LONG).show()
        }
    }


    fun openCamera(){
        if (ContextCompat.checkSelfPermission(this@MainActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !==
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                    android.Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(android.Manifest.permission.CAMERA), REQUEST_CODE)
            } else {
                ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(android.Manifest.permission.CAMERA), REQUEST_CODE)
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (REQUEST_CODE) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this@MainActivity,
                            android.Manifest.permission.CAMERA) ===
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
                        capturePhoto()
                        barcodeView.barcodeView.decoderFactory = DefaultDecoderFactory(arrayListOf(BarcodeFormat.PDF_417, BarcodeFormat.QR_CODE))
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }


    fun capturePhoto(){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CODE)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            val barcodeValue = result.contents
            val decodedBytes = Base64.decode(barcodeValue, Base64.DEFAULT)
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            val intent = Intent(this, BarcodeScanner::class.java)
            intent.putExtra("info", decodedString)
            startActivity(intent)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }



    companion object{
        const val REQUEST_CODE = 1
    }
}










