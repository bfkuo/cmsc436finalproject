package com.example.cmsc436finalproject

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.provider.FirebaseInitProvider
import android.graphics.Color
import android.net.Uri
import android.view.*
import androidx.core.content.contentValuesOf

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init firebase APIs
        FirebaseInitProvider()

        setContentView(R.layout.activity_main)

        val saveButton: Button = findViewById(R.id.saveMenu)
        saveButton.setOnClickListener { showPopup(it) }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.image_menu, menu)
        return true
    }

    val test_text = "testing copy text"

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.copy_text -> {
                Toast.makeText(this, "Copy text selected", Toast.LENGTH_SHORT).show()

                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("Copy Translated Text", test_text)
                clipboard.setPrimaryClip(clip)
            }

            R.id.save_image -> {
                Toast.makeText(this, "Save image selected", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(this, view)

        popup.menuInflater.inflate(R.menu.image_menu, popup.menu)
        popup.show()

        popup.setOnMenuItemClickListener {
            onOptionsItemSelected(it)
        }
    }
}