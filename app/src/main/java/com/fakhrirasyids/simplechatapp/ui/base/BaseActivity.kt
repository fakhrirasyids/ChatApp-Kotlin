package com.fakhrirasyids.simplechatapp.ui.base

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.fakhrirasyids.simplechatapp.ui.viewmodels.MainViewModel
import com.fakhrirasyids.simplechatapp.ui.viewmodels.MainViewModelFactory
import com.fakhrirasyids.simplechatapp.utils.Constants
import com.fakhrirasyids.simplechatapp.utils.PreferenceManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

open class BaseActivity : AppCompatActivity() {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

    private lateinit var mainViewModel: MainViewModel
    private lateinit var documentReference: DocumentReference

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        init()
    }

    private fun init() {
        val pref = PreferenceManager.getInstance(dataStore)
        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(pref)
        )[MainViewModel::class.java]

        mainViewModel.getString(Constants.KEY_USER_ID).observe(this) { userId ->
            if (userId != null) {
                documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(userId)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        init()
        mainViewModel.getString(Constants.KEY_USER_ID).observe(this) { userId ->
            if (userId != null) {
                documentReference.update(Constants.KEY_AVAILABILITY, 0)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        init()
        mainViewModel.getString(Constants.KEY_USER_ID).observe(this) { userId ->
            if (userId != null) {
                documentReference.update(Constants.KEY_AVAILABILITY, 1)
            }
        }
    }
}