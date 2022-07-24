package com.fakhrirasyids.simplechatapp.ui.users
//
//import android.content.Context
//import android.content.Intent
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.view.View
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.Preferences
//import androidx.datastore.preferences.preferencesDataStore
//import androidx.lifecycle.ViewModelProvider
//import com.fakhrirasyids.simplechatapp.databinding.ActivityUsersBinding
//import com.fakhrirasyids.simplechatapp.listeners.UserListener
//import com.fakhrirasyids.simplechatapp.models.User
//import com.fakhrirasyids.simplechatapp.ui.adapters.UsersAdapter
//import com.fakhrirasyids.simplechatapp.ui.chat.ChatActivity
//import com.fakhrirasyids.simplechatapp.ui.viewmodels.MainViewModel
//import com.fakhrirasyids.simplechatapp.ui.viewmodels.MainViewModelFactory
//import com.fakhrirasyids.simplechatapp.utils.Constants
//import com.fakhrirasyids.simplechatapp.utils.PreferenceManager
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.QueryDocumentSnapshot
//
//class UsersActivity : AppCompatActivity() {
//    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")
//
//    private lateinit var binding: ActivityUsersBinding
//    private lateinit var mainViewModel: MainViewModel
//    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityUsersBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val pref = PreferenceManager.getInstance(dataStore)
//        mainViewModel = ViewModelProvider(
//            this,
//            MainViewModelFactory(pref)
//        )[MainViewModel::class.java]
//
//        setListener()
//        getUsers()
//    }
//
//    private fun setListener() {
//        binding.ivBack.setOnClickListener { onBackPressed() }
//    }
//
//    private fun getUsers() {
//        showLoading(true)
//        mainViewModel.getString(Constants.KEY_USER_ID).observe(this) { userId ->
//            if (userId != null) {
//                database.collection(Constants.KEY_COLLECTION_USERS)
//                    .get()
//                    .addOnCompleteListener { task ->
//                        showLoading(false)
//                        val currentUserId = userId
//                        if (task.isSuccessful && task.result != null) {
//                            val users = ArrayList<User>()
//                            for (queryDocumentSnapshot: QueryDocumentSnapshot in task.result) {
//                                if (currentUserId.equals(queryDocumentSnapshot.id)) {
//                                    continue
//                                }
//                                val user = User()
//                                user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME)
//                                user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL)
//                                user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE)
//                                user.token =
//                                    queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN)
//                                user.id = queryDocumentSnapshot.id
//                                users.add(user)
//                            }
//                            if (users.size > 0) {
//                                val userAdapter = UsersAdapter(users)
//                                binding.rvUser.adapter = userAdapter
//                                binding.rvUser.visibility = View.VISIBLE
//
//                                userAdapter.setUserListener(object : UserListener {
//                                        override fun onUserClicked(user: User) {
//                                            val intent = Intent(this@UsersActivity, ChatActivity::class.java)
//                                            intent.putExtra(Constants.KEY_USER, user)
//                                            startActivity(intent)
//                                            finish()
//                                        }
//                                    }
//                                )
//                            } else {
//                                showErrorMessage()
//                            }
//                        } else {
//                            showErrorMessage()
//                        }
//                    }
//            }
//        }
//    }
//
//    private fun showErrorMessage() {
//        binding.tvErrorMessage.text = StringBuilder("No user available")
//        binding.tvErrorMessage.visibility = View.VISIBLE
//    }
//
//    private fun showLoading(isLoading: Boolean) {
//        if (isLoading) {
//            binding.progressBar.visibility = View.VISIBLE
//        } else {
//            binding.progressBar.visibility = View.GONE
//        }
//    }
//}