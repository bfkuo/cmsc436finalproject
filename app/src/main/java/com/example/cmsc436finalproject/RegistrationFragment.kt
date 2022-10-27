package com.example.cmsc436finalproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.cmsc436finalproject.databinding.FragmentRegistrationBinding
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match

/**
 * A simple [Fragment] subclass.
 * Use the [RegistrationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegistrationFragment : Fragment() {
    private var validator = Validators()
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentRegistrationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        auth = requireNotNull(FirebaseAuth.getInstance())

        binding.register.setOnClickListener{ registerNewUser() }

        return binding.root
    }

    private fun registerNewUser() {
        val email: String = binding.email.text.toString()
        val password: String = binding.password.text.toString()

        if (!validator.validEmail(email)) {
            Toast.makeText(
                requireContext(),
                getString(R.string.invalid_username),
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

        binding.progressBar.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            binding.progressBar.visibility = View.GONE
            if (task.isSuccessful) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.welcome),
                    Toast.LENGTH_LONG
                ).show()

                // TODO: navigate to logged in fragment
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.registration_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}