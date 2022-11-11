package com.example.cmsc436finalproject

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import com.example.cmsc436finalproject.databinding.FragmentMainBinding
import java.io.File

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var photoFile: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.takePhoto.setOnClickListener{
            // TODO: ask user for camera permission

            val cameraIntent = Intent(ACTION)
            photoFile = getPhotoFile(FILE_NAME)

            // create content URI that allows temporary access of URI to camera app
            val fileProvider = FileProvider.getUriForFile(requireContext(),
                "com.example.cmsc436finalproject.fileprovider", photoFile)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            try {
                startActivityForResult(cameraIntent, REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    requireContext(),
                    "Unable to open camera",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        return binding.root
    }

    private fun getPhotoFile(fileName: String): File {
        // Use "getExternalFilesDir" on Context to access package specific directories
        val storageDirectory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // get image from file
            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            binding.photoView.setImageBitmap(takenImage)

            // TODO: save image to user account

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val ACTION = MediaStore.ACTION_IMAGE_CAPTURE
        private const val PERMISSION = Manifest.permission.CAMERA
        private const val REQUEST_CODE = 19
        private const val FILE_NAME = "photo.jpg"
    }
}