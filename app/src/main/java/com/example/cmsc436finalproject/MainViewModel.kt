package com.example.cmsc436finalproject

import androidx.lifecycle.*
import me.bush.translator.Language
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel : ViewModel(), DefaultLifecycleObserver {

    var from = MutableStateFlow<Language>(Language.AUTO)
    var to = MutableStateFlow<Language>(Language.ENGLISH)

    internal fun bindToActivityLifecycle(mainActivity: MainActivity) {
        // add current instance of ViewModel as LifeCycleObserver to MainActivity
        mainActivity.lifecycle.addObserver(this)
    }

}