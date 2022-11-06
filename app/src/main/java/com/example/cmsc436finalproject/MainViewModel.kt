package com.example.cmsc436finalproject

import androidx.lifecycle.*
import kotlinx.coroutines.*
import me.bush.translator.Language
import me.bush.translator.Translator
import android.util.Log
import me.bush.translator.languageOf

class MainViewModel : ViewModel(), DefaultLifecycleObserver {

    private val _transFrom = MutableLiveData<String>()
    internal val transFrom: LiveData<String>
        get() = _transFrom

    private val _transTo = MutableLiveData<String>()
    internal val transTo: LiveData<String>
        get() = _transTo

    init {
        _transFrom.value = ""
        _transTo.value = ""
    }

    fun translate() {//text: String, from: String, to: String) {
        viewModelScope.launch {
            val translator = Translator()
            val translation = translator.translate("Bush's translator is so cool!", Language.RUSSIAN, Language.AUTO)
            Log.i("EQUAL", (languageOf("Russian") == Language.RUSSIAN).toString())
            Log.i("TRANSLATOR", translation.translatedText) // Переводчик Буша такой классный!
            translation.pronunciation?.let { Log.i("TRANSLATOR", it) } // Perevodchik Busha takoy klassnyy!
            Log.i("TRANSLATOR", translation.sourceLanguage.toString()) // English
        }
    }

    internal fun bindToActivityLifecycle(mainActivity: MainActivity) {
        // add current instance of ViewModel as LifeCycleObserver to MainActivity
        mainActivity.lifecycle.addObserver(this)
    }

}