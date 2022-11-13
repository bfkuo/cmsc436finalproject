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

    var translateFromLanguage: String = ""
    var translateToLanguage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init firebase APIs
        FirebaseInitProvider()

        setContentView(R.layout.activity_main)

        val saveButton: Button = findViewById(R.id.saveMenu)
        saveButton.setOnClickListener { showPopup(it) }

        setupDropdowns()
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

            R.id.copy_image -> {
                Toast.makeText(this, "Copy image selected", Toast.LENGTH_SHORT).show()
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
            when (it.itemId) {
                R.id.copy_text -> Toast.makeText(this, "Copy text selected", Toast.LENGTH_SHORT).show()
                R.id.copy_image -> Toast.makeText(this, "Copy image selected", Toast.LENGTH_SHORT).show()
                R.id.save_image -> Toast.makeText(this, "Save image selected", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    private fun setupDropdowns() {

        val translateFrom: Spinner = findViewById(R.id.translateFrom)
        val translateTo : Spinner = findViewById(R.id.translateTo)

//        TO DO: replace with translatable languages later
//        for now, dummy dropdowns
        val list : MutableList<String> = ArrayList()
        list.add("Hint")
        for (i in 1..10)
            list.add("Item $i")

        // LOOK INTO: mini text-view "hint" above
        val adapter = object : ArrayAdapter<String>(this,androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, list) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item from Spinner
                // First item will be used for hint
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: TextView = super.getDropDownView(position, convertView, parent) as TextView
                //set the color of first item in the drop down list to gray
                if(position == 0) {
                    view.setTextColor(Color.GRAY)
                }
                return view
            }
        }
        translateFrom.adapter = adapter
        translateTo.adapter = adapter

        translateFrom.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val item = list[pos]
                if(pos == 0){
                    (view as TextView).setTextColor(Color.GRAY)
                }

                Toast.makeText(this@MainActivity, "TranslateFrom $item selected", Toast.LENGTH_SHORT).show()
            }
        }

        translateTo.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val item = list[pos]
                if(pos == 0){
                    (view as TextView).setTextColor(Color.GRAY)
                }

                Toast.makeText(this@MainActivity, "TranslateTo $item selected", Toast.LENGTH_SHORT).show()
            }
        }

        limitDropDownHeight(translateFrom)
        limitDropDownHeight(translateTo)
    }

    private fun limitDropDownHeight(dropdown: Spinner) {
        val popup = Spinner::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true

        val popupWindow = popup.get(dropdown) as ListPopupWindow
        popupWindow.height = (200 * resources.displayMetrics.density).toInt()
    }
}