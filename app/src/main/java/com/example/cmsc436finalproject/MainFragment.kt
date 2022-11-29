package com.example.cmsc436finalproject

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.cmsc436finalproject.databinding.MainFragmentBinding
import com.google.firebase.provider.FirebaseInitProvider
import kotlinx.coroutines.launch
import me.bush.translator.*


class MainFragment : Fragment() {

    private lateinit var binding: MainFragmentBinding
    private lateinit var viewModel: MainViewModel
    private var text: String = "Bush's translator is so cool!"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = MainFragmentBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.bindToActivityLifecycle(requireActivity() as MainActivity)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val saveButton: Button = view.findViewById(R.id.saveMenu)
        saveButton.setOnClickListener { showPopup(it) }

        setupDropdowns()
        menu()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init firebase APIs
        FirebaseInitProvider()
    }

    fun translate(text: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val translator = Translator()
            val translation = translator.translate(text, viewModel.to.value, viewModel.from.value)
            viewModel.translated.value = translation.translatedText
            Toast.makeText(activity, "UPDATED:" + viewModel.translated.value, Toast.LENGTH_SHORT).show()
        }
    }

    fun menu() {
        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.image_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                when (menuItem.itemId) {
                    R.id.copy_text -> {
                        Toast.makeText(activity, "Copy text selected", Toast.LENGTH_SHORT).show()

                        val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip: ClipData = ClipData.newPlainText("Copy Translated Text", viewModel.translated.value)
                        clipboard.setPrimaryClip(clip)
                        true
                    }

                    R.id.save_image -> {
                        Toast.makeText(activity, "Save image selected", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    //    val test_text = "testing copy text"
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.copy_text -> {
                Toast.makeText(activity, "Copy text selected", Toast.LENGTH_SHORT).show()

                val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("Copy Translated Text", viewModel.translated.value)
                clipboard.setPrimaryClip(clip)
            }

            R.id.save_image -> {
                Toast.makeText(activity, "Save image selected", Toast.LENGTH_SHORT).show()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(requireActivity(), view)

        popup.menuInflater.inflate(R.menu.image_menu, popup.menu)
        popup.show()

        popup.setOnMenuItemClickListener {
            onOptionsItemSelected(it)
        }
    }

    private fun setupDropdowns() {

        val translateFrom: Spinner = requireView().findViewById(R.id.translateFrom)
        val translateTo : Spinner = requireView().findViewById(R.id.translateTo)

        val fromLanguages = resources.getStringArray(R.array.languages)
        val toLanguages = resources.getStringArray(R.array.languages).drop(1)
        
        val fromAdapter = object : ArrayAdapter<String>(requireActivity(),
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            fromLanguages) {}

        val toAdapter = object : ArrayAdapter<String>(requireActivity(),
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            toLanguages) {}

        translateFrom.adapter = fromAdapter
        translateTo.adapter = toAdapter

        translateFrom.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                var lang = toLanguages[pos]
                if (lang.equals("Haitian Creole")) lang = "Hatian Creole"
                if (lang.contains(' ')) lang = lang.replace(' ', '_')
                if (lang.contains('(')) lang = lang.replace("(", "").replace(")", "")

                val language = checkNotNull(languageOf(lang)) {
                    Toast.makeText(requireActivity(), "Invalid language to translate from", Toast.LENGTH_SHORT).show()
                }

                viewModel.from.value = language
                translate(text)

                Toast.makeText(requireActivity(), text, Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(requireActivity(), "Invalid language to translate to", Toast.LENGTH_SHORT).show()
                }

                viewModel.to.value = language
                translate(text)

//                comes super late
                Toast.makeText(requireActivity(), viewModel.translated.value, Toast.LENGTH_SHORT).show()
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