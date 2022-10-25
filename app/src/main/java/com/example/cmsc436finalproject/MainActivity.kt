package com.example.cmsc436finalproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.provider.FirebaseInitProvider

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init firebase APIs
        FirebaseInitProvider()

        setContentView(R.layout.activity_main)
    }
}