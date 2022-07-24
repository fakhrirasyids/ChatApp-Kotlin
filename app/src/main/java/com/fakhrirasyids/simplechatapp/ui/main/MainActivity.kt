package com.fakhrirasyids.simplechatapp.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fakhrirasyids.simplechatapp.databinding.ActivityMainBinding
import com.fakhrirasyids.simplechatapp.listeners.ConversationsListener
import com.fakhrirasyids.simplechatapp.listeners.UserListener
import com.fakhrirasyids.simplechatapp.models.ChatMessage
import com.fakhrirasyids.simplechatapp.models.User
import com.fakhrirasyids.simplechatapp.ui.adapters.RecentConversationAdapter
import com.fakhrirasyids.simplechatapp.ui.adapters.UsersAdapter
import com.fakhrirasyids.simplechatapp.ui.base.BaseActivity
import com.fakhrirasyids.simplechatapp.ui.chat.ChatActivity
import com.fakhrirasyids.simplechatapp.ui.sign.SignInActivity
import com.fakhrirasyids.simplechatapp.ui.viewmodels.MainViewModel
import com.fakhrirasyids.simplechatapp.ui.viewmodels.MainViewModelFactory
//import com.fakhrirasyids.simplechatapp.ui.users.UsersActivity
import com.fakhrirasyids.simplechatapp.utils.Constants
import com.fakhrirasyids.simplechatapp.utils.PreferenceManager
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity() {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
//    private lateinit var conversationsAdapter: RecentConversationAdapter

//    private var conversations = ArrayList<ChatMessage>()
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = PreferenceManager.getInstance(dataStore)
        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(pref)
        )[MainViewModel::class.java]

        loadUserDetails()
        getToken()
        setListeners()
        getUsers()

        // FOR RECENT CONVERSATION

//        listenConversations()
//        init()
    }

    private fun getUsers() {
        showLoading(true)
        mainViewModel.getString(Constants.KEY_USER_ID).observe(this) { userId ->
            if (userId != null) {
                database.collection(Constants.KEY_COLLECTION_USERS)
                    .get()
                    .addOnCompleteListener { task ->
                        showLoading(false)
                        val currentUserId = userId
                        if (task.isSuccessful && task.result != null) {
                            val users = ArrayList<User>()
                            for (queryDocumentSnapshot: QueryDocumentSnapshot in task.result) {
                                if (currentUserId.equals(queryDocumentSnapshot.id)) {
                                    continue
                                }
                                val user = User()
                                user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME)
                                user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE)
                                user.token =
                                    queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN)
                                user.id = queryDocumentSnapshot.id
                                users.add(user)
                            }
                            if (users.size > 0) {
                                val userAdapter = UsersAdapter(users)
                                binding.rvConversations.adapter = userAdapter
                                binding.rvConversations.visibility = View.VISIBLE

                                userAdapter.setUserListener(object : UserListener {
                                    override fun onUserClicked(user: User) {
                                        val intent = Intent(this@MainActivity, ChatActivity::class.java)
                                        intent.putExtra(Constants.KEY_USER, user)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                                )
                            } else {
                                showErrorMessage()
                            }
                        } else {
                            showErrorMessage()
                        }
                    }
            }
        }
    }

    private fun showErrorMessage() {
        binding.tvErrorMessage.text = StringBuilder("No user available")
        binding.tvErrorMessage.visibility = View.VISIBLE
    }

    // FOR RECENT CONVERSATION

//    private fun init() {
//        conversations = ArrayList()
//        conversationsAdapter = RecentConversationAdapter(conversations)
//        binding.rvConversations.adapter = conversationsAdapter
//        conversationsAdapter.setConversationListener(object : ConversationsListener {
//            override fun onConversationClicked(user: User) {
//                val intent = Intent(this@MainActivity, ChatActivity::class.java)
//                intent.putExtra(Constants.KEY_USER, user)
//                startActivity(intent)
//                finish()
//            }
//        })
//    }

    private fun setListeners() {
        binding.ivSignOut.setOnClickListener { signOut() }
//        binding.fabNewChat.setOnClickListener {
//            startActivity(Intent(this, UsersActivity::class.java))
//        }
    }

    private fun loadUserDetails() {
        mainViewModel.getString(Constants.KEY_NAME).observe(this) { name ->
            binding.tvName.text = name
        }

        mainViewModel.getString(Constants.KEY_IMAGE).observe(this) { image ->
            if (image != null) {
                val bytes: ByteArray = Base64.decode(image, Base64.DEFAULT)
                val bitmap: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                binding.ivProfile.setImageBitmap(bitmap)
            }
        }
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)
    }

    private fun updateToken(token: String) {
        mainViewModel.putString(Constants.KEY_FCM_TOKEN, token)
        mainViewModel.getString(Constants.KEY_USER_ID).observe(this) { keyUserId ->
            if (keyUserId != null) {
                val documentReference: DocumentReference =
                    database.collection(Constants.KEY_COLLECTION_USERS).document(keyUserId)

                documentReference.update(Constants.KEY_FCM_TOKEN, token)
                    .addOnFailureListener {
                        showToast("Unable to update token")
                    }
            }
        }
    }

    private fun signOut() {
        showToast("Signing out..")

        mainViewModel.getString(Constants.KEY_USER_ID).observe(this) { keyUserId ->
            if (keyUserId != null) {
                val documentReference: DocumentReference =
                    database.collection(Constants.KEY_COLLECTION_USERS).document(keyUserId)

                val updates = HashMap<String, Any>()
                updates[Constants.KEY_FCM_TOKEN] = FieldValue.delete()

                documentReference.update(updates)
                    .addOnSuccessListener {
                        mainViewModel.clear()
                        startActivity(Intent(this, SignInActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        showToast("Unable to sign out")
                    }
            }
        }
    }

    // FOR RECENT CONVERSATION

//    @SuppressLint("NotifyDataSetChanged")
//    private val eventListener = EventListener<QuerySnapshot> { value, error ->
//        if (error != null) {
//            return@EventListener
//        }
//        if (value != null) {
//            for (documentChange: DocumentChange in value.documentChanges) {
//                if (documentChange.type == DocumentChange.Type.ADDED) {
//                    val senderId =
//                        documentChange.document.getString(Constants.KEY_SENDER_ID)
//                    val receiverId =
//                        documentChange.document.getString(Constants.KEY_RECEIVER_ID)
//                    val chatMessage = ChatMessage()
//                    chatMessage.senderId = senderId
//                    chatMessage.receiverId = receiverId
//
//                    mainViewModel.getString(Constants.KEY_USER_ID).observeOnce(this) { userId ->
//                        if (userId != null) {
//                            if (userId == senderId) {
//                                chatMessage.conversationImage =
//                                    documentChange.document.getString(Constants.KEY_RECEIVER_IMAGE)
//                                chatMessage.conversationName =
//                                    documentChange.document.getString(Constants.KEY_RECEIVER_NAME)
//                                chatMessage.conversationId =
//                                    documentChange.document.getString(Constants.KEY_RECEIVER_ID)
//                            } else {
//                                chatMessage.conversationImage =
//                                    documentChange.document.getString(Constants.KEY_SENDER_IMAGE)
//                                chatMessage.conversationName =
//                                    documentChange.document.getString(Constants.KEY_SENDER_NAME)
//                                chatMessage.conversationId =
//                                    documentChange.document.getString(Constants.KEY_SENDER_ID)
//                            }
//                        }
//                    }
//
//                    chatMessage.message =
//                        documentChange.document.getString(Constants.KEY_LAST_MESSAGE)
//                    chatMessage.dateObject =
//                        documentChange.document.getDate(Constants.KEY_TIMESTAMP)
//
//                    conversations.add(chatMessage)
//                } else if (documentChange.type == DocumentChange.Type.MODIFIED) {
//                    for (i in 0..conversations.size) {
//                        val senderId =
//                            documentChange.document.getString(Constants.KEY_SENDER_ID)
//                        val receiverId =
//                            documentChange.document.getString(Constants.KEY_RECEIVER_ID)
//                        if (conversations[i].senderId.equals(senderId) && conversations[i].receiverId.equals(
//                                receiverId
//                            )
//                        ) {
//                            conversations[i].message =
//                                documentChange.document.getString(Constants.KEY_LAST_MESSAGE)
//                            conversations[i].dateObject =
//                                documentChange.document.getDate(Constants.KEY_TIMESTAMP)
//                            break
//                        }
//                    }
//                }
//            }
//
//            conversations.sortWith { obj1, obj2 ->
//                obj2.dateObject!!.compareTo(obj1.dateObject)
//            }
//            conversationsAdapter.notifyDataSetChanged()
//            binding.rvConversations.smoothScrollToPosition(0)
//            binding.rvConversations.visibility = View.VISIBLE
//            binding.progressBar.visibility = View.GONE
//        }
//    }

    // FOR RECENT CONVERSATION

//    private fun listenConversations() {
//        mainViewModel.getString(Constants.KEY_USER_ID).observe(this) { userId ->
//            if (userId != null) {
//                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
//                    .whereEqualTo(Constants.KEY_SENDER_ID, userId)
//                    .addSnapshotListener(eventListener)
//                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
//                    .whereEqualTo(Constants.KEY_RECEIVER_ID, userId)
//                    .addSnapshotListener(eventListener)
//            }
//        }
//    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }
}