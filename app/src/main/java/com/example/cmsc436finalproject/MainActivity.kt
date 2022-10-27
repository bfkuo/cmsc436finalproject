package com.example.cmsc436finalproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.provider.FirebaseInitProvider
import com.example.cmsc436finalproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init firebase APIs
        FirebaseInitProvider()

        setContentView(ActivityMainBinding.inflate(layoutInflater).root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, LoginFragment())
                .commitNow()
        }
    }
}