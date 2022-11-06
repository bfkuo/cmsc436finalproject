package com.example.cmsc436finalproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.provider.FirebaseInitProvider
import android.graphics.Color
import android.view.ViewGroup
import com.example.cmsc436finalproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    var translateFromLanguage: String = ""
    var translateToLanguage: String = ""

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init firebase APIs
        FirebaseInitProvider()

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        supportFragmentManager.beginTransaction()
            .add(R.id.container, MainFragment())
            .commitNow()

        // setupDropdowns()
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