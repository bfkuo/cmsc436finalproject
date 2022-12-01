package com.example.cmsc436finalproject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cmsc436finalproject.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth


class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = requireNotNull(FirebaseAuth.getInstance())
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("Login fragment", "login fragment created")
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        // auth = requireNotNull(FirebaseAuth.getInstance())

        binding.login.setOnClickListener { loginUserAccount() }
        binding.toRegister.setOnClickListener{
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
            Log.i("loginFragment", "navigated to registration fragment")
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun loginUserAccount() {
        val email: String = binding.email.text.toString()
        val password: String = binding.password.text.toString()

        binding.progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener() { task ->
            binding.progressBar.visibility = View.GONE
            if (task.isSuccessful) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.welcome),
                    Toast.LENGTH_LONG
                ).show()

                // for debugging purposes:
                // findNavController().navigate(R.id.action_loginFragment_to_settingsFragment)
                findNavController().navigate(R.id.action_loginFragment_to_mainFragment)

            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.login_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}

