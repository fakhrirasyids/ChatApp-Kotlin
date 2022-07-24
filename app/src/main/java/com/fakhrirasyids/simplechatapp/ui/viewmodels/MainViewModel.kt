package com.fakhrirasyids.simplechatapp.ui.viewmodels

import androidx.lifecycle.*
import com.fakhrirasyids.simplechatapp.utils.PreferenceManager
import kotlinx.coroutines.launch

class MainViewModel(private val pref: PreferenceManager) : ViewModel() {
    fun getBoolean(key: String): LiveData<Boolean> {
        return pref.getBoolean(key).asLiveData()
    }

    fun putBoolean(key: String, value: Boolean) {
        viewModelScope.launch {
            pref.putBoolean(key, value)
        }
    }

    fun getString(key: String): LiveData<String?> {
        return pref.getString(key).asLiveData()
    }

    fun putString(key: String, value: String) {
        viewModelScope.launch {
            pref.putString(key, value)
        }
    }

    fun clear() {
        viewModelScope.launch {
            pref.clear()
        }
    }
}