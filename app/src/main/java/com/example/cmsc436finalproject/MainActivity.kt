package com.example.cmsc436finalproject

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.provider.FirebaseInitProvider
import androidx.lifecycle.ViewModelProvider

import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.bush.translator.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private var text: String = "Bush's translator is so cool!"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init firebase APIs
        FirebaseInitProvider()

        setContentView(R.layout.activity_main)

        setupDropdowns()
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.bindToActivityLifecycle(this)

        val saveButton: Button = findViewById(R.id.saveMenu)
        saveButton.setOnClickListener { showPopup(it) }
    }

    fun translate(text: String) {
        lifecycleScope.launch {
            val translator = Translator()
            val translation = translator.translate(text, viewModel.to.value, viewModel.from.value)
            viewModel.translated.value = translation.translatedText
            Log.i("UPDATED", viewModel.translated.value)
        }
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

    private fun setupDropdowns() {

        val translateFrom: Spinner = findViewById(R.id.translateFrom)
        val translateTo : Spinner = findViewById(R.id.translateTo)

        val fromLanguages = resources.getStringArray(R.array.languages)
        val toLanguages = resources.getStringArray(R.array.languages).drop(1)

        // LOOK INTO: mini text-view "hint" above
        val fromAdapter = object : ArrayAdapter<String>(this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            fromLanguages) {}

        val toAdapter = object : ArrayAdapter<String>(this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            toLanguages) {}

        translateFrom.adapter = fromAdapter
        translateTo.adapter = toAdapter

        translateFrom.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val item = fromLanguages[pos]
                val language = checkNotNull(languageOf(fromLanguages[pos])) {
                    Toast.makeText(this@MainActivity, "Invalid language to translate from", Toast.LENGTH_SHORT).show()
                }

                viewModel.from.value = language
                translate(text)

                Toast.makeText(this@MainActivity, "TranslateFrom $item selected", Toast.LENGTH_SHORT).show()
            }
        }

        translateTo.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                var lang = toLanguages[pos]
                if (lang.equals("Haitian Creole")) lang = "Hatian Creole"
                if (lang.contains(' ')) lang = lang.replace(' ', '_')
                if (lang.contains('(')) lang = lang.replace("(", "").replace(")", "")

                val language = checkNotNull(languageOf(lang)) {
                    Toast.makeText(this@MainActivity, "Invalid language to translate to", Toast.LENGTH_SHORT).show()
                }

                viewModel.to.value = language
                translate(text)

                Toast.makeText(this@MainActivity, "TranslateTo $lang selected", Toast.LENGTH_SHORT).show()
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