package com.example.cmsc436finalproject

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.cmsc436finalproject.databinding.FragmentRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegistrationFragment : Fragment() {
    private var validator = Validators()
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentRegistrationBinding
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        auth = requireNotNull(FirebaseAuth.getInstance())

        binding.register.setOnClickListener{
            Log.i(TAG, "Register button clicked")
            registerNewUser()
        }
        return binding.root
    }

    private fun registerNewUser() {
        val email: String = binding.email.text.toString()
        val password: String = binding.password.text.toString()

        if (!validator.validEmail(email)) {
            Toast.makeText(
                requireContext(),
                getString(R.string.invalid_email),
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (!validator.validPassword(password)) {
            Toast.makeText(
                requireContext(),
                getString(R.string.invalid_password),
                Toast.LENGTH_LONG
            ).show()

            return
        }



        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->

            if (task.isSuccessful) {
                //an empty document to initialize user history collection
                val hist = hashMapOf( "transFrom" to "",
                                        "transTo" to "",
                                        "From" to "",
                                        "To" to "")
                val user = hashMapOf("email" to auth.currentUser!!.email,
                                     "displayName" to "Account Name")

                //creates a document labeled with unique user ID in "user" collection
                val userID = db.collection("users").document(auth.currentUser!!.uid)

                userID
                    .set(user)
                    .addOnSuccessListener {
                        userID
                            .collection("history")
                            .document()
                            .set(hist)
                            .addOnSuccessListener {
                                Log.i(TAG, "DocumentSnapshot successfully written!")
                            }
                            .addOnFailureListener { e ->
                                Log.i(TAG, "Error adding document", e)

                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.registration_failed),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        Log.i(TAG, "DocumentSnapshot successfully written!")

                        Toast.makeText(
                            requireContext(),
                            getString(R.string.welcome),
                            Toast.LENGTH_LONG
                        ).show()

                        // for debugging purposes:
                        // findNavController().navigate((R.id.action_registrationFragment_to_settingsFragment))
                        findNavController().navigate(R.id.action_registrationFragment_to_mainFragment)
                    }
                    .addOnFailureListener { e ->
                        Log.i(TAG, "Error adding document", e)

                        Toast.makeText(
                            requireContext(),
                            getString(R.string.registration_failed),
                            Toast.LENGTH_LONG
                        ).show()

                    }

            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.registration_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        private val TAG = "RegistrationFragment.kt"
    }
}