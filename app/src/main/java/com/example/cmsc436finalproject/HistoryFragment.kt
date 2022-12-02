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
    private val db = Firebase.firestore

    //add user name to account history title
    //


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(inflater, container, false)

        binding.history.movementMethod = ScrollingMovementMethod()

        val userID = FirebaseAuth.getInstance().currentUser!!.uid

        userID?.let {
            db.collection("users").document(it)
                .get()
                .addOnSuccessListener { document ->
                    val newName = StringBuilder();
                    if (document != null) {
                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                        newName.append(document.data?.get("displayName"))
                        newName.append("'s History")
                        binding.title.setText(newName)
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }

        //history collection reference
        val histRef = userID?.let {
            db.collection("users").document(it).collection("history")
        }

        histRef
            .get()
            .addOnSuccessListener { result ->
                val newText = StringBuilder();
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data.get("From").toString()}")
                    if(!(document.data.get("From").toString().equals(""))) {
                        newText.append("[From: ")
                        newText.append(document.data.get("From"))
                        newText.append("]\n")
                        newText.append(document.data.get("transFrom"))
                        newText.append("\n[To: ")
                        newText.append(document.data.get("To"))
                        newText.append("]\n")
                        newText.append(document.data.get("transTo"))
                        newText.append("\n\n")
                    }
                }
                binding.history.setText(newText);
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }



        binding.settingButton.setOnClickListener{
            Log.i(HistoryFragment.TAG, "Setting button clicked")
            findNavController().navigate(R.id.action_historyFragment_to_settingsFragment)
        }

        return binding.root
    }

//    private fun toStringMap(map: Map<String, Any>): String {
//        val result = map.toString();
//        val result2 = result.replace("}", "");
//        val result3 = result2.replace("{", "");
//        val result4 = result3.replace(", ", "\n");
//        val result5 = result4.replace("translateTo", "Translated Text");
//        val result6 = result5.replace("translateFrom", "Original Text");
//        val result7 = result6.replace("=", ": ");
//
//        return result
//    }

    companion object {
        private const val TAG = "HistoryFragment.kt"
    }
}