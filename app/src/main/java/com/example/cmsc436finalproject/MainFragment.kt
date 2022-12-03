package com.example.cmsc436finalproject

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.cmsc436finalproject.databinding.MainFragmentBinding
import com.google.firebase.provider.FirebaseInitProvider
import kotlinx.coroutines.launch
import me.bush.translator.*
import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainFragment : Fragment() {

    private lateinit var binding: MainFragmentBinding
    private lateinit var photoFile: File

    private lateinit var viewModel: MainViewModel
    private var text: String = WELCOME_STRING
    // InputImage needed for text recognition
    private lateinit var inputImage: InputImage
    private lateinit var textRecognizer: TextRecognizer
    private lateinit var auth: FirebaseAuth
    private var db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = MainFragmentBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.bindToActivityLifecycle(requireActivity() as MainActivity)

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val copy: Button = view.findViewById(R.id.copyText)
        copy.setOnClickListener {
            val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("Copying text", binding.photoText.text)
            clipboard.setPrimaryClip(clip)
        }

//        swap languages chosen (from <=> to)
        val swapButton: Button = view.findViewById(R.id.swapIcon)
        swapButton.setOnClickListener {
            if (viewModel.from.value.toString().lowercase() != "auto") {

                val temp = viewModel.to.value
                viewModel.to.value = viewModel.from.value
                viewModel.from.value = temp

            } else {
                Toast.makeText(requireActivity(), "Cannot translate to Auto", Toast.LENGTH_SHORT).show()
            }

        }

        binding.accountButton.setOnClickListener {
            // TODO: change to navigate to account history
           // findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
            findNavController().navigate(R.id.action_mainFragment_to_historyFragment)
        }

        binding.photoText.movementMethod = ScrollingMovementMethod()
        setupDropdowns()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init firebase APIs
        FirebaseInitProvider()
    }
    override fun onResume() {
        super.onResume()
        if (viewModel.translated.value.isNotEmpty())
            binding.photoText.text = viewModel.translated.value
        setupDropdowns()
    }

    fun translate(text: String) {
        binding.translateButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val translator = Translator()
                val translation =
                    translator.translate(text, viewModel.to.value, viewModel.from.value)
                viewModel.translated.value = translation.translatedText

//                update translated text view here for faster performance
                binding.photoText.text = viewModel.translated.value
                addToHistory()
            }
        }
    }

    private fun setupDropdowns() {

        val translateFrom: Spinner = requireView().findViewById(R.id.translateFrom)
        val translateTo : Spinner = requireView().findViewById(R.id.translateTo)

        val fromLanguages = resources.getStringArray(R.array.languages)
        val toLanguages = resources.getStringArray(R.array.languages).drop(1)

        val fromAdapter = object : ArrayAdapter<String>(requireActivity(),
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            fromLanguages) {}

        val toAdapter = object : ArrayAdapter<String>(requireActivity(),
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            toLanguages) {}

        translateFrom.adapter = fromAdapter
        translateTo.adapter = toAdapter

//        process data
        var fromLang = viewModel.from.value.toString()
        if (fromLang.lowercase() == "hatian_creole") fromLang = "Haitian Creole"
        if (fromLang.contains('_')) {
            fromLang = fromLang.split('_').joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }
        }

        var toLang = viewModel.to.value.toString()
        if (toLang.lowercase() == "hatian_creole") toLang = "Haitian Creole"
        if (toLang.contains('_')) {
            toLang = toLang.split('_').joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }
        }

        translateFrom.setSelection(fromAdapter.getPosition(fromLang))
        translateTo.setSelection(toAdapter.getPosition(toLang))

        translateFrom.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                var lang = fromLanguages[pos]
                if (lang.equals("Haitian Creole")) lang = "Hatian Creole"
                if (lang.contains(' ')) lang = lang.replace(' ', '_')

                val language = checkNotNull(languageOf(lang)) {
                    Toast.makeText(requireActivity(), "Invalid language to translate from", Toast.LENGTH_SHORT).show()
                }

                viewModel.from.value = language
            }
        }

        translateTo.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                var lang = toLanguages[pos]
                if (lang.equals("Haitian Creole")) lang = "Hatian Creole"
                if (lang.contains(' ')) lang = lang.replace(' ', '_')

                val language = checkNotNull(languageOf(lang)) {
                    Toast.makeText(requireActivity(), "Invalid language to translate to", Toast.LENGTH_SHORT).show()
                }

                // only translate + update if truly needed
                if (viewModel.to.value != language)
                    viewModel.to.value = language
                    translate(text)

            }
        }

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

            // initialize inputImage for text recognition using bitmap
            inputImage = InputImage.fromBitmap(takenImage, 0)
            recognizeText()

        } else if (requestCode == PICK_PHOTO_CODE && resultCode == Activity.RESULT_OK) {
            // get image from uri
            val photoUri = data?.data;
            binding.photoView.setImageURI(photoUri)

            // initialize inputImage for text recognition using URI
            inputImage = InputImage.fromFilePath(requireContext(), photoUri!!)
            recognizeText()

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun recognizeText() {
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {

            textRecognizer.process(inputImage).addOnSuccessListener { recognized ->

                text = recognized.text
                binding.photoText.text = text
                translate(text)

            }.addOnFailureListener { e->
                Toast.makeText(
                    requireContext(),
                    "Failed to recognize text due to ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Failed to prepare image due to ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun addToHistory() {
        auth = requireNotNull(FirebaseAuth.getInstance())

        val hist = hashMapOf("From" to viewModel.from.value.toString(),
                                "transFrom" to text,
                            "To" to viewModel.to.value.toString(),
                                "transTo" to viewModel.translated.value)

        val userHistory = db.collection("users")
                            .document(auth.currentUser!!.uid)
                            .collection("history")
        userHistory
            .document()
            .set(hist)
    }

    companion object {
        private const val WELCOME_STRING = "Hello! This is where translated text will show up. Upload or take a picture to get started!"
        private const val TAKE_PHOTO_ACTION = MediaStore.ACTION_IMAGE_CAPTURE
        private const val PICK_PHOTO_ACTION = Intent.ACTION_PICK
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val GALLERY_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
        private const val CAMERA_CODE = 19
        private const val PICK_PHOTO_CODE = 20
        private const val FILE_NAME = "photo.jpg"
    }

}