package com.example.cmsc436finalproject

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.text.method.ReplacementTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.example.cmsc436finalproject.databinding.FragmentAccountSettingsBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage


class AccountSettingsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentAccountSettingsBinding
    private var validator = Validators()
    private lateinit var user: FirebaseUser
    private val db = Firebase.firestore
    private lateinit var firestoreUserRef: DocumentReference
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)

        auth = requireNotNull(FirebaseAuth.getInstance())
        user = requireNotNull(auth.currentUser)
        storage = requireNotNull(FirebaseStorage.getInstance())

        firestoreUserRef = db.collection("users").document(user.uid)

        firestoreUserRef.get().addOnSuccessListener {
            // get user profile photo
//            val photoUrl = it.get("profilePhotoUrl").toString()
//            Log.i(TAG, "photo url: {$photoUrl}")
//            val photoRef = storage.getReferenceFromUrl(photoUrl)
//
//            photoRef.getBytes(ONE_MEGABYTE)
//                .addOnSuccessListener { byteArray ->
//                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
//                    binding.profilePicture.setImageBitmap(bitmap)
//                }
//                .addOnFailureListener{ e ->
//                    Log.i(TAG, "failed to get byte array", e)
//                }

            // get user display name
            binding.accountName.setText(it.get("displayName").toString())
            binding.accountName.inputType = InputType.TYPE_NULL
        }

        setUpEditTextViews(user)

//        binding.editProfilePicture.setOnClickListener {
//            when {
//                checkSelfPermission(requireContext(), GALLERY_PERMISSION) == PackageManager.PERMISSION_GRANTED -> {
//                    openGallery()
//                }
//
//                shouldShowRequestPermissionRationale(GALLERY_PERMISSION) -> {
//                    binding.root.showSnackbar(
//                        R.string.need_gallery_permission_string,
//                        Snackbar.LENGTH_INDEFINITE,
//                        android.R.string.ok
//                    ) {
//                        requestGalleryPermissionLauncher.launch(GALLERY_PERMISSION)
//                    }
//                }
//
//                else -> {
//                    requestGalleryPermissionLauncher.launch(GALLERY_PERMISSION)
//                }
//            }
//        }

        binding.editAccountName.setOnClickListener {
            makeEditable(binding.accountName, InputType.TYPE_CLASS_TEXT)

            binding.accountName.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    makeUneditable(binding.accountName)
                    db.collection("users")
                        .document(user.uid)
                        .set("displayName" to binding.accountName.toString())
                        .addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "Display name successfully updated",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .addOnFailureListener {
                            Log.i(TAG, "Error updating display name in firestore", it)
                            binding.accountName.setText(user.displayName)
                            Toast.makeText(
                                requireContext(),
                                "Display name could not be changed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
        }

        binding.editEmail.setOnClickListener {
            makeEditable(binding.email, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)

            binding.email.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    makeUneditable(binding.email)
                    checkValidEmail(user)
                }
            }
        }

        binding.editPassword.setOnClickListener {
            makeEditable(binding.password, InputType.TYPE_TEXT_VARIATION_PASSWORD)

            binding.password.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    makeUneditable(binding.password)
                    binding.password.transformationMethod = PasswordTransformationMethod.getInstance()
                    checkValidPassword(user)
                }
            }
        }

        return binding.root
    }

    private fun setUpEditTextViews(user: FirebaseUser) {
        binding.email.setText(user.email ?: "No email set")
        binding.email.inputType = InputType.TYPE_NULL

        binding.password.setText("********")
        binding.password.inputType = InputType.TYPE_NULL
        binding.password.transformationMethod = PasswordTransformationMethod.getInstance()
    }

    private fun makeEditable(view: EditText, inputType: Int) {
        view.inputType = inputType
        view.requestFocus()
        view.showSoftKeyboard()
        view.hint = view.text
        view.setText("")
    }
    private fun makeUneditable(view: EditText) {
        view.inputType = InputType.TYPE_NULL
        val imm = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.HIDE_IMPLICIT_ONLY)
        view.hint = ""
    }

    private fun EditText.showSoftKeyboard(){
        (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun checkValidEmail(user: FirebaseUser) {
        val email = binding.email.text.toString()
        if (!validator.validEmail(email)) {
            binding.email.setText(user.email)
            Toast.makeText(
                requireContext(),
                "Email could not be changed; "  + getString(R.string.invalid_email),
                Toast.LENGTH_LONG
            ).show()
        } else {
            user.updateEmail(email)
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        db.collection("users")
                            .document(user.uid)
                            .set(hashMapOf("email" to email))
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Email successfully updated",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            .addOnFailureListener {
                                Log.i(TAG, "Error updating email in firestore", it)
                                Toast.makeText(
                                    requireContext(),
                                    "Email could not be changed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Email could not be changed",
                            Toast.LENGTH_LONG
                        ).show()

                        // reset email text view
                        binding.email.setText(user.email)
                    }
                }
        }

    }
    private fun checkValidPassword(user: FirebaseUser) {
        val password = binding.password.text.toString()
        if (!validator.validPassword(password)) {
            binding.password.setText("********")
            Toast.makeText(
                requireContext(),
                "Password could not be changed; " + getString(R.string.invalid_password),
                Toast.LENGTH_LONG
            ).show()

            return
        } else {
            user.updatePassword(password)
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Password successfully updated",
                            Toast.LENGTH_LONG
                        ).show()

                        // reset password view text to 8 characters
                        binding.password.setText("********")
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Password could not be changed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }


//    private fun uploadImage(uid: String, uri: Uri): Task<Uri>? {
//        val storageRef = storage.reference
//        var downloadUrl: Task<Uri>? = null
//        storageRef.child(uid).putFile(uri)
//            .addOnSuccessListener {
//                Log.i(TAG, "upload image to firebase storage successful")
//                downloadUrl = it.storage.downloadUrl
//            }
//            .addOnFailureListener {
//                Log.i(TAG, "upload image to firebase storage failed")
//            }
//
//        return downloadUrl
//    }

    companion object {
        private const val ONE_MEGABYTE = 1024L * 1024L
        private const val TAG = "AccountSettingsFragment.kt"
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val GALLERY_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
        private const val TAKE_PHOTO_ACTION = MediaStore.ACTION_IMAGE_CAPTURE
        private const val PICK_PHOTO_ACTION = Intent.ACTION_PICK
        private const val CAMERA_CODE = 19
        private const val PICK_PHOTO_CODE = 20
    }
}