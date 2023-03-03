package com.example.vanillamvvmproject

import android.os.Bundle
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity


class BarcodeScanner : AppCompatActivity()  {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.barcode_scanner_layout)
        val  barcodeTextView = findViewById<TextView>(R.id.txt_result)
        val barcodeValue = intent.getStringExtra("barcodeValue")
        barcodeTextView.text = barcodeValue
    }

}