package com.fakhrirasyids.simplechatapp.ui.viewmodels

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.*
import com.fakhrirasyids.simplechatapp.utils.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(private val pref: PreferenceManager): ViewModel() {
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