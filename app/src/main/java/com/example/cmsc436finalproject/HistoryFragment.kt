package com.example.cmsc436finalproject

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cmsc436finalproject.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.Query


class HistoryFragment : Fragment() {
    // TODO: Rename and change types of parameters
//    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentHistoryBinding
    private val db = Firebase.firestore

    //add user name to account history title

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
//        binding.history.movementMethod = ScrollingMovementMethod()

        val userID = FirebaseAuth.getInstance().currentUser!!.uid

        userID.let {
            db.collection("users").document(it)
                .get()
                .addOnSuccessListener { document ->
                    val newName = StringBuilder();
                    if (document != null) {
                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                        newName.append(document.data?.get("displayName"))
                        newName.append("'s History")
                        binding.title.text = newName
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }

        binding.settingButton.setOnClickListener{
            Log.i(HistoryFragment.TAG, "Setting button clicked")
            findNavController().navigate(R.id.action_historyFragment_to_settingsFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userID = FirebaseAuth.getInstance().currentUser!!.uid

        //history collection reference
        val histRef = userID.let {
            db.collection("users")
                .document(it)
                .collection("history")
                .orderBy("timeCreated", Query.Direction.DESCENDING)
        }

        histRef
            .get()
            .addOnSuccessListener { result ->
                var hist = ArrayList<SpannableStringBuilder>()
                val blackText = ForegroundColorSpan(Color.BLACK)
                val smallerText = RelativeSizeSpan(.8f)
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data["From"].toString()}")
                    if(document.data["From"].toString() != "") {

                        val entry = SpannableStringBuilder("${document.data["transFrom"]}\n${document.data["transTo"]}")
                        val sepIndex = document.data["transFrom"].toString().length
                        entry.setSpan(blackText, 0, sepIndex+1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                        entry.setSpan(smallerText, sepIndex+1, entry.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                        hist.add(entry)

                    }
                }
                val recyclerView: RecyclerView = view.findViewById(R.id.recyclerHistory)
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = CustomAdapter(hist)

            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }

    }

    companion object {
        private const val TAG = "HistoryFragment.kt"
    }
}