package com.example.cmsc436finalproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.provider.FirebaseInitProvider
import com.example.cmsc436finalproject.databinding.ActivityMainBinding
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private var text: String = "Bush's translator is so cool!"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set content to the binding root view.
        setContentView(ActivityMainBinding.inflate(layoutInflater).root)
        // Init firebase APIs
        FirebaseInitProvider()

        binding = ActivityMainBinding.inflate(layoutInflater)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.bindToActivityLifecycle(this)
    }
}