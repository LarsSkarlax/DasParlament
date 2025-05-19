package com.lrs.dasparlament.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "There are currently no settings. So just enjoy simplicity :)"
    }
    val text: LiveData<String> = _text
}