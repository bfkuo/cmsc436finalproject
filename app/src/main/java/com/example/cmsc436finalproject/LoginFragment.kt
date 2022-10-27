package com.example.cmsc436finalproject

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.example.cmsc436finalproject.databinding.FragmentLoginBinding


/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        auth = requireNotNull(FirebaseAuth.getInstance())

        binding.login.setOnClickListener { loginUserAccount() }
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

                // TODO: navigate to post login fragment
                // findNavController().navigate()

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

