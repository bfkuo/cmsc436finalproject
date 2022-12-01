package com.example.cmsc436finalproject

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.cmsc436finalproject.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase




class HistoryFragment : Fragment() {
    // TODO: Rename and change types of parameters
//    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentHistoryBinding
//    private val db = Firebase.firestore

    //add user name to account history title
    //


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(inflater, container, false)

        binding.history.movementMethod = ScrollingMovementMethod()

        binding.settingButton.setOnClickListener{
            Log.i(HistoryFragment.TAG, "Setting button clicked")
            findNavController().navigate(R.id.action_historyFragment_to_settingsFragment)
        }

        return binding.root
    }

    companion object {
        private const val TAG = "HistoryFragment.kt"
    }
}