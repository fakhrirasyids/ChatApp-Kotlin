package com.fakhrirasyids.simplechatapp.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferenceManager private constructor (private val dataStore: DataStore<Preferences>) {

    fun getBoolean(key: String): Flow<Boolean> {
        val BOOLEAN_KEY = booleanPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[BOOLEAN_KEY] ?: false
        }
    }

    suspend fun putBoolean(key: String, value: Boolean) {
        val BOOLEAN_KEY = booleanPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[BOOLEAN_KEY] = value
        }
    }

    fun getString(key: String): Flow<String?> {
        val STRING_KEY = stringPreferencesKey(key)
        return dataStore.data.map { preferences ->
            preferences[STRING_KEY] ?: null
        }
    }

    suspend fun putString(key: String, value: String) {
        val STRING_KEY = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[STRING_KEY] = value
        }
    }

    suspend fun clear() {
        dataStore.edit {
            it.clear()
        }
    }


    companion object {
        @Volatile
        private var INSTANCE: PreferenceManager? = null

        fun getInstance(dataStore: DataStore<Preferences>): PreferenceManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PreferenceManager(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
//    private val sharedPreferences =
//        context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)
//
//
//    fun putBoolean(key: String, value: Boolean) {
//        val editor: SharedPreferences.Editor = sharedPreferences.edit()
//        editor.putBoolean(key, value)
//        editor.apply()
//    }
//
//    fun getBoolean(key: String): Boolean {
//        return sharedPreferences.getBoolean(key, false)
//    }
//
//    fun putString(key: String, value: String) {
//        val editor: SharedPreferences.Editor = sharedPreferences.edit()
//        editor.putString(key, value)
//        editor.apply()
//    }
//
//    fun getString(key: String): String? {
//        return sharedPreferences.getString(key, null)
//    }
//
//    fun clear() {
//        val editor: SharedPreferences.Editor = sharedPreferences.edit()
//        editor.clear()
//        editor.apply()
//    }
}