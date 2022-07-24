package com.fakhrirasyids.simplechatapp.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.fakhrirasyids.simplechatapp.R
import com.fakhrirasyids.simplechatapp.databinding.ActivityChatBinding
import com.fakhrirasyids.simplechatapp.models.ChatMessage
import com.fakhrirasyids.simplechatapp.models.User
import com.fakhrirasyids.simplechatapp.network.ApiConfig
import com.fakhrirasyids.simplechatapp.ui.adapters.ChatAdapter
import com.fakhrirasyids.simplechatapp.ui.base.BaseActivity
import com.fakhrirasyids.simplechatapp.ui.main.MainActivity
import com.fakhrirasyids.simplechatapp.ui.viewmodels.MainViewModel
import com.fakhrirasyids.simplechatapp.ui.viewmodels.MainViewModelFactory
import com.fakhrirasyids.simplechatapp.utils.Constants
import com.fakhrirasyids.simplechatapp.utils.PreferenceManager
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : BaseActivity() {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

    private lateinit var binding: ActivityChatBinding
    private lateinit var receiverUser: User
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var mainViewModel: MainViewModel

    // FOR RECENT CONVERSATION

//    private var conversationId: String? = null
private var chatMessages = ArrayList<ChatMessage>()
    private var isReceiverAvailable: Boolean = false
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = PreferenceManager.getInstance(dataStore)
        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(pref)
        )[MainViewModel::class.java]

        setListeners()
        loadReceiverDetails()
        listenMessages()
        init()
    }

    private fun listenAvailabilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.id!!)
            .addSnapshotListener(this) { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                        val availability: Int = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                        )!!.toInt()
                        isReceiverAvailable = availability == 1
                    }
                    receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN)
                }
                if (isReceiverAvailable) {
                    binding.tvAvailability.visibility = View.VISIBLE
                } else {
                    binding.tvAvailability.visibility = View.GONE
                }
            }
    }

    private fun loadReceiverDetails() {
        receiverUser = intent.getParcelableExtra(Constants.KEY_USER)!!
        binding.tvName.text = receiverUser.name
    }

    private fun setListeners() {
        binding.ivBack.setOnClickListener { this.onBackPressed() }
        binding.layoutSend.setOnClickListener { sendMessage() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        overridePendingTransition(R.anim.slide_out, R.anim.nothing)
        startActivity(intent)
        finish()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun init() {
        mainViewModel.getString(Constants.KEY_USER_ID).observeOnce(this) { userId ->
            if (userId != null) {
                chatAdapter = ChatAdapter(
                    chatMessages,
                    getBitmapFromEncodedString(receiverUser.image!!),
                    userId
                )
                binding.rvChat.adapter = chatAdapter
            }
        }
    }

    private fun sendMessage() {
        mainViewModel.getString(Constants.KEY_USER_ID).observeOnce(this) { userId ->
            if (userId != null) {
                val message = HashMap<String, Any>()

                message[Constants.KEY_SENDER_ID] = userId
                message[Constants.KEY_RECEIVER_ID] = receiverUser.id!!
                message[Constants.KEY_MESSAGE] = binding.edInputMessage.text.toString()
                message[Constants.KEY_TIMESTAMP] = Date()

                database.collection(Constants.KEY_COLLECTION_CHAT).add(message)

                // FOR RECENT CONVERSATION

//                if (conversationId != null) {
//                    updateConversation(binding.edInputMessage.text.toString())
//                } else {
//                    var lastMessage = binding.edInputMessage.text.toString()
//                    mainViewModel.getString(Constants.KEY_NAME).observeOnce(this) { keyName ->
//                        mainViewModel.getString(Constants.KEY_IMAGE).observeOnce(this) { keyImage ->
//                            if (keyName != null && keyImage != null) {
//                                val conversation = HashMap<String, Any>()
//
//                                conversation[Constants.KEY_SENDER_ID] = userId
//                                conversation[Constants.KEY_SENDER_NAME] = keyName
//                                conversation[Constants.KEY_SENDER_NAME] = keyImage
//                                conversation[Constants.KEY_RECEIVER_ID] = receiverUser.id!!
//                                conversation[Constants.KEY_RECEIVER_NAME] = receiverUser.name!!
//                                conversation[Constants.KEY_RECEIVER_IMAGE] = receiverUser.image!!
//                                conversation[Constants.KEY_LAST_MESSAGE] = lastMessage
//                                conversation[Constants.KEY_TIMESTAMP] = Date()
//
//                                Toast.makeText(this, "INI isi last message:  ${lastMessage}", Toast.LENGTH_SHORT).show()
//                                addConversation(conversation)
//                            }
//                        }
//                    }
//                }

                // FOR NOTIFICATION

//                if (!isReceiverAvailable) {
//                    try {
//                        var nMessage = binding.edInputMessage.text.toString()
//
//                        mainViewModel.getString(Constants.KEY_USER_ID).observeOnce(this) { keyUserId ->
//                            mainViewModel.getString(Constants.KEY_NAME).observeOnce(this) { keyName ->
//                                mainViewModel.getString(Constants.KEY_FCM_TOKEN).observeOnce(this) { keyToken ->
//                                    if (keyUserId != null && keyName != null && keyToken != null) {
//                                        val tokens = JSONArray()
//                                        tokens.put(receiverUser.token)
//
//                                        val data = JSONObject()
//                                        data.put(Constants.KEY_USER_ID, userId)
//                                        data.put(Constants.KEY_NAME, keyName)
//                                        data.put(Constants.KEY_FCM_TOKEN, keyToken)
//                                        data.put(Constants.KEY_MESSAGE, nMessage)
//
//                                        var body = JSONObject()
//                                        body.put(Constants.REMOTE_MSG_DATA, data)
//                                        body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)
//
//                                        sendNotification(body.toString())
//                                    }
//                                }
//                            }
//                        }
//                    } catch (e: Exception) {
//                        e.message?.let { showToast(it) }
//                    }
//                }

                binding.edInputMessage.text = null
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // FOR NOTIFICATION

//    private fun sendNotification(messageBody: String) {
//        val client = ApiConfig.getApiService().sendMessage(
//            Constants.getRemoteMsgHeader(),
//            messageBody
//        )
//
//        client.enqueue(object : retrofit2.Callback<String> {
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                if (response.isSuccessful) {
//                    try {
//                        val responseJson = JSONObject(response.body())
//                        val results = responseJson.getJSONArray("results")
//                        if (responseJson.getInt("failure") == 1) {
//                            var error: JSONObject = results[0] as JSONObject
//                            showToast(error.getString("error"))
//                            return
//                        }
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                    }
//                    showToast("Notification sent successfully")
//                } else {
//                    showToast("Error: " + response.code())
//                }
//            }
//
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                showToast(t.message!!)
//            }
//
//        })
//    }

    @SuppressLint("NotifyDataSetChanged")
    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null) {
            return@EventListener
        }
        if (value != null) {
            val count = chatMessages.size
            for (documentChange: DocumentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    val chatMessage = ChatMessage()
                    chatMessage.senderId =
                        documentChange.document.getString(Constants.KEY_SENDER_ID)
                    chatMessage.receiverId =
                        documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                    chatMessage.message =
                        documentChange.document.getString(Constants.KEY_MESSAGE)
                    chatMessage.dateTime =
                        getReadableDateTime(
                            documentChange.document.getDate(
                                Constants.KEY_TIMESTAMP
                            )!!
                        )
                    chatMessage.dateObject =
                        documentChange.document.getDate(Constants.KEY_TIMESTAMP)

                    chatMessages.add(chatMessage)
                }
            }
            chatMessages.sortWith { obj1, obj2 ->
                obj1.dateObject!!.compareTo(obj2.dateObject)
            }
            if (count == 0) {
                chatAdapter.notifyDataSetChanged()
            } else {
                chatAdapter.notifyItemRangeInserted(
                    chatMessages.size,
                    chatMessages.size
                )
                binding.rvChat.smoothScrollToPosition(chatMessages.size - 1)
            }
            binding.rvChat.visibility = View.VISIBLE
        }
        binding.progressBar.visibility = View.GONE

        // FOR RECENT CONVERSATION

//        if (conversationId == null) {
//            checkForConversation()
//        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun listenMessages() {
        mainViewModel.getString(Constants.KEY_USER_ID).observeOnce(this) { userId ->
            if (userId != null) {
                database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_SENDER_ID, userId)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                    .addSnapshotListener(eventListener)
                database.collection(Constants.KEY_COLLECTION_CHAT)
                    .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, userId)
                    .addSnapshotListener(eventListener)
            }
        }
    }

    private fun getReadableDateTime(date: Date): String {
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }

    private fun getBitmapFromEncodedString(encodedImage: String): Bitmap {
        val bytes: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    // FOR RECENT CONVERSATION

//    private fun addConversation(conversation: HashMap<String, Any>) {
//        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
//            .add(conversation)
//            .addOnSuccessListener { documentReference ->
//                conversationId = documentReference.id
//            }
//    }
//
//    private fun updateConversation(message: String) {
//        if (conversationId != null) {
//            val documentReference: DocumentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(
//                conversationId!!
//            )
//
//            documentReference.update(
//                Constants.KEY_LAST_MESSAGE, message,
//                Constants.KEY_TIMESTAMP, Date()
//            )
//        }
//    }
//
//    private fun checkForConversation() {
//        mainViewModel.getString(Constants.KEY_USER_ID).observe(this) { userId ->
//            if (userId != null) {
//                if (chatMessages.size != 0) {
//                    checkForConversationRemotely(userId, receiverUser.id)
//                    checkForConversationRemotely(receiverUser.id, userId)
//                }
//            }
//        }
//    }
//
//    private fun checkForConversationRemotely(senderId: String?, receiverId: String?) {
//        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
//            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
//            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
//            .get()
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful && task.result != null && task.result.documents.size > 0) {
//                    val documentSnapshot: DocumentSnapshot = task.result.documents[0]
//                    conversationId = documentSnapshot.id
//                }
//            }
//    }

    override fun onResume() {
        super.onResume()
        listenAvailabilityOfReceiver()
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