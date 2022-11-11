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
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import com.example.cmsc436finalproject.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar
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
            // if permission has been granted, take photo
            when {
                checkSelfPermission(requireContext(), PERMISSION) == PackageManager.PERMISSION_GRANTED -> {
                    takePhoto()
                }

                // if user has previously denied permission, explain why permission required
                shouldShowRequestPermissionRationale(PERMISSION) -> {
                    binding.root.showSnackbar(
                        R.string.need_permission_string,
                        Snackbar.LENGTH_INDEFINITE,
                        android.R.string.ok
                    ) {
                        requestPermissionLauncher.launch(PERMISSION)
                    }
                }

                // user hasn't given permission yet
                else -> {
                    requestPermissionLauncher.launch(PERMISSION)
                }
            }
        }

        return binding.root
    }

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                takePhoto()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.need_permission_string),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun takePhoto() {
        Log.i("main fragment", "this is the take photo function")
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