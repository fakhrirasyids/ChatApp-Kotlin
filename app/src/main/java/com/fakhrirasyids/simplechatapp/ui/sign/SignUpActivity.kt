package com.fakhrirasyids.simplechatapp.ui.sign

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.fakhrirasyids.simplechatapp.ui.main.MainActivity
import com.fakhrirasyids.simplechatapp.databinding.ActivitySignUpBinding
import com.fakhrirasyids.simplechatapp.ui.viewmodels.MainViewModel
import com.fakhrirasyids.simplechatapp.ui.viewmodels.MainViewModelFactory
import com.fakhrirasyids.simplechatapp.utils.Constants.Companion.KEY_COLLECTION_USERS
import com.fakhrirasyids.simplechatapp.utils.Constants.Companion.KEY_EMAIL
import com.fakhrirasyids.simplechatapp.utils.Constants.Companion.KEY_IMAGE
import com.fakhrirasyids.simplechatapp.utils.Constants.Companion.KEY_IS_SIGNED_IN
import com.fakhrirasyids.simplechatapp.utils.Constants.Companion.KEY_NAME
import com.fakhrirasyids.simplechatapp.utils.Constants.Companion.KEY_PASSWORD
import com.fakhrirasyids.simplechatapp.utils.Constants.Companion.KEY_USER_ID
import com.fakhrirasyids.simplechatapp.utils.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class SignUpActivity : AppCompatActivity() {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var mainViewModel: MainViewModel

    private var encodedImage: String? = null

    private val pickImage: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultData = result.data
            if (resultData != null) {
                val imageUri: Uri = resultData.data!!
                try {
                    val inputStream: InputStream = contentResolver.openInputStream(imageUri)!!
                    val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.ivProfile.setImageBitmap(bitmap)
                    binding.tvAddImage.visibility = View.GONE
                    encodedImage = encodeImage(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = PreferenceManager.getInstance(dataStore)
        mainViewModel = ViewModelProvider(this, MainViewModelFactory(pref))[MainViewModel::class.java]

        setListeners()
    }

    private fun setListeners() {
        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
        binding.btnSignUp.setOnClickListener {
            if (isValidSignUpDetails()) {
                signUp()
            }
        }
        binding.layoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun signUp() {
        showLoading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val user = HashMap<String, Any>()

        user[KEY_NAME] = binding.inputName.text.toString()
        user[KEY_EMAIL] = binding.inputEmail.text.toString()
        user[KEY_PASSWORD] = binding.inputPassword.text.toString()
        user[KEY_IMAGE] = encodedImage!!

        database.collection(KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener { documentReference ->
                showLoading(false)
                mainViewModel.putBoolean(KEY_IS_SIGNED_IN, true)
                mainViewModel.putString(KEY_USER_ID, documentReference.id)
                mainViewModel.putString(KEY_NAME, binding.inputName.text.toString())
                mainViewModel.putString(KEY_IMAGE, encodedImage!!)

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .addOnFailureListener {
                showLoading(false)
                showToast(it.message!!)
            }
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val previewWidth = 150
        val previewHeight: Int = bitmap.height * previewWidth / bitmap.width
        val previewBitmap: Bitmap =
            Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun isValidSignUpDetails(): Boolean {
        if (encodedImage == null) {
            showToast("Select a profile image")
            return false
        } else if (binding.inputName.text.toString().trim().isEmpty()) {
            showToast("Enter name")
            return false
        } else if (binding.inputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
            showToast("Enter a valid email")
            return false
        } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter password")
            return false
        } else if (binding.inputConfirmPassword.text.toString().trim().isEmpty()) {
            showToast("Enter confirmation password")
            return false
        } else if (binding.inputPassword.text.toString() != binding.inputConfirmPassword.text.toString()) {
            showToast("Password $ Confirmation password must be same")
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
                btnSignUp.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
            }
        } else {
            binding.apply {
                btnSignUp.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
            }
        }
    }
}