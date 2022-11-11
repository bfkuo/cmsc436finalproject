package com.example.cmsc436finalproject

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
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
import androidx.core.graphics.drawable.toBitmap
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
                checkSelfPermission(requireContext(), CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED -> {
                    takePhoto()
                }

                // if user has previously denied permission, explain why permission required
                shouldShowRequestPermissionRationale(CAMERA_PERMISSION) -> {
                    binding.root.showSnackbar(
                        R.string.need_camera_permission_string,
                        Snackbar.LENGTH_INDEFINITE,
                        android.R.string.ok
                    ) {
                        requestCameraPermissionLauncher.launch(CAMERA_PERMISSION)
                    }
                }

                // user hasn't given permission yet
                else -> {
                    requestCameraPermissionLauncher.launch(CAMERA_PERMISSION)
                }
            }
        }

        binding.photoView.setOnClickListener{
            when {
                checkSelfPermission(requireContext(), GALLERY_PERMISSION) == PackageManager.PERMISSION_GRANTED -> {
                    openGallery()
                }

                shouldShowRequestPermissionRationale(GALLERY_PERMISSION) -> {
                    binding.root.showSnackbar(
                        R.string.need_gallery_permission_string,
                        Snackbar.LENGTH_INDEFINITE,
                        android.R.string.ok
                    ) {
                        requestGalleryPermissionLauncher.launch(GALLERY_PERMISSION)
                    }
                }

                else -> {
                    requestGalleryPermissionLauncher.launch(GALLERY_PERMISSION)
                }
            }
        }

        binding.saveMenu.setOnClickListener {
            val bitmap = binding.photoView.drawable.toBitmap()
            // save image to gallery and reset ImageView
            MediaStore.Images.Media.insertImage(requireContext().contentResolver, bitmap, "image", null)
            binding.photoView.setImageResource(R.drawable.ic_baseline_vertical_align_top_24)
        }

        return binding.root
    }

    private val requestCameraPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                takePhoto()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.need_camera_permission_string),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val requestGalleryPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.need_gallery_permission_string),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun takePhoto() {
        Log.i("main fragment", "this is the take photo function")
        val cameraIntent = Intent(TAKE_PHOTO_ACTION)
        photoFile = getPhotoFile(FILE_NAME)

        // create content URI that allows temporary access of URI to camera app
        val fileProvider = FileProvider.getUriForFile(requireContext(),
            "com.example.cmsc436finalproject.fileprovider", photoFile)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

        try {
            startActivityForResult(cameraIntent, CAMERA_CODE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                "Unable to open camera",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(PICK_PHOTO_ACTION, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        try {
            startActivityForResult(galleryIntent, PICK_PHOTO_CODE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                "Unable to open gallery",
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
        if (requestCode == CAMERA_CODE && resultCode == Activity.RESULT_OK) {
            // get image from file
            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            binding.photoView.setImageBitmap(takenImage)

        } else if (requestCode == PICK_PHOTO_CODE && resultCode == Activity.RESULT_OK) {
            // get image from uri
            val photoUri = data?.data;
            binding.photoView.setImageURI(photoUri)

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val TAKE_PHOTO_ACTION = MediaStore.ACTION_IMAGE_CAPTURE
        private const val PICK_PHOTO_ACTION = Intent.ACTION_PICK
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val GALLERY_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
        private const val CAMERA_CODE = 19
        private const val PICK_PHOTO_CODE = 20
        private const val FILE_NAME = "photo.jpg"
    }
}