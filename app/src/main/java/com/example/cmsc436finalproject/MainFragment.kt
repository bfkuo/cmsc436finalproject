package com.example.cmsc436finalproject

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cmsc436finalproject.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private val Fragment.packageManager get() = activity?.packageManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.takePhoto.setOnClickListener{
            val cameraIntent = Intent(ACTION)
            try {
                startActivityForResult(cameraIntent, REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    requireContext(),
                    "Unable to open camera",
                    Toast.LENGTH_LONG
                ).show()
            }
//            if (cameraIntent.resolveActivity(fragment.packageManager?) != null) {
//                startActivityForResult(cameraIntent, REQUEST_CODE)
//            }

        }
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // get image from intent data
            val image = data?.extras?.get("data") as Bitmap
            binding.photoView.setImageBitmap(image)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val ACTION = MediaStore.ACTION_IMAGE_CAPTURE
        private const val PERMISSION = Manifest.permission.CAMERA
        private const val REQUEST_CODE = 19
    }
}