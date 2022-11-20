package com.example.cmsc436finalproject

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.text.method.ReplacementTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cmsc436finalproject.databinding.FragmentAccountSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class AccountSettingsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentAccountSettingsBinding
    private var validator = Validators()
    private lateinit var user: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)

        auth = requireNotNull(FirebaseAuth.getInstance())
        user = requireNotNull(auth.currentUser)
        setUpEditTextViews(user)


        binding.editAccountName.setOnClickListener {
            makeEditable(binding.accountName, InputType.TYPE_CLASS_TEXT)

            binding.accountName.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    makeUneditable(binding.accountName)
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
        binding.accountName.setText(user.displayName ?: "Account Name")
        binding.accountName.inputType = InputType.TYPE_NULL

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
            binding.email.setText("")
            binding.email.hint = "Enter valid email"
            Toast.makeText(
                requireContext(),
                getString(R.string.invalid_email),
                Toast.LENGTH_LONG
            ).show()
        } else {
            user.updateEmail(email)
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Email successfully updated",
                            Toast.LENGTH_LONG
                        ).show()
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
            binding.password.setText("")
            binding.password.hint = "Enter valid password"
            Toast.makeText(
                requireContext(),
                getString(R.string.invalid_password),
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

    companion object {
        private val TAG = "AccountSettingsFragment.kt"
    }
}