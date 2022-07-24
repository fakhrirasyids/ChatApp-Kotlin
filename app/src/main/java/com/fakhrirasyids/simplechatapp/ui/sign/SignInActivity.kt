package com.fakhrirasyids.simplechatapp.ui.sign

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.fakhrirasyids.simplechatapp.ui.main.MainActivity
import com.fakhrirasyids.simplechatapp.databinding.ActivitySignInBinding
import com.fakhrirasyids.simplechatapp.ui.viewmodels.MainViewModel
import com.fakhrirasyids.simplechatapp.ui.viewmodels.MainViewModelFactory
import com.fakhrirasyids.simplechatapp.utils.Constants
import com.fakhrirasyids.simplechatapp.utils.PreferenceManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

    private lateinit var binding: ActivitySignInBinding
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = PreferenceManager.getInstance(dataStore)
        mainViewModel = ViewModelProvider(this, MainViewModelFactory(pref)).get(
            MainViewModel::class.java
        )

        mainViewModel.getBoolean(Constants.KEY_IS_SIGNED_IN).observe(this) {
            if (it) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        setListeners()
    }

    private fun setListeners() {
        binding.tvCreateNewAccount.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        binding.btnSignIn.setOnClickListener {
            if (isValidSignInDetails()) {
                signIn()
            }
        }
    }

    private fun signIn() {
        showLoading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()

        database.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.text.toString())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null && task.result.documents.size > 0) {
                    val documentSnapshot: DocumentSnapshot = task.result.documents.get(0)
                    mainViewModel.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    mainViewModel.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                    mainViewModel.putString(Constants.KEY_NAME,
                        documentSnapshot.getString(Constants.KEY_NAME)!!
                    )
                    mainViewModel.putString(Constants.KEY_IMAGE,
                        documentSnapshot.getString(Constants.KEY_IMAGE)!!
                    )

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    showLoading(false)
                    showToast("Unable to Sign In")
                }
            }
    }

    private fun isValidSignInDetails(): Boolean {
        if (binding.inputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
            showToast("Enter valid email")
            return false
        } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter password")
            return false
        } else {
            return true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.apply {
                btnSignIn.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
            }
        } else {
            binding.apply {
                btnSignIn.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
            }
        }
    }
}