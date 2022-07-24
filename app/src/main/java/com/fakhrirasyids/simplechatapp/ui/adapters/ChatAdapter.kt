package com.fakhrirasyids.simplechatapp.ui.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.fakhrirasyids.simplechatapp.databinding.ActivityChatBinding
import com.fakhrirasyids.simplechatapp.databinding.ItemReceivedMessageContainerBinding
import com.fakhrirasyids.simplechatapp.databinding.ItemSentMessageContainerBinding
import com.fakhrirasyids.simplechatapp.models.ChatMessage
import com.fakhrirasyids.simplechatapp.ui.chat.ChatActivity

class ChatAdapter(
    private var chatMessages: List<ChatMessage>,
    private var receiverProfileImage: Bitmap,
    private var senderId: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var sentMessageViewHolder: SentMessageViewHolder
    private lateinit var receivedMessageViewHolder: ReceivedMessageViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        sentMessageViewHolder = SentMessageViewHolder(
            ItemSentMessageContainerBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
        receivedMessageViewHolder = ReceivedMessageViewHolder(
            ItemReceivedMessageContainerBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

        return if (viewType == VIEW_TYPE_SENT) {
            sentMessageViewHolder
        } else {
            receivedMessageViewHolder
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            sentMessageViewHolder.setData(chatMessages[position])
        } else {
            receivedMessageViewHolder.setData(chatMessages[position], receiverProfileImage)
        }
    }

    override fun getItemCount() = chatMessages.size

    override fun getItemViewType(position: Int): Int {
        return if (chatMessages[position].senderId.equals(senderId)) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }


    class SentMessageViewHolder(private var binding: ItemSentMessageContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            binding.tvMessage.text = chatMessage.message
            binding.tvDateTime.text = chatMessage.dateTime
        }
    }

    class ReceivedMessageViewHolder(private var binding: ItemReceivedMessageContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage, receiverProfileImage: Bitmap) {
            binding.tvMessage.text = chatMessage.message
            binding.tvDateTime.text = chatMessage.dateTime
            binding.ivProfile.setImageBitmap(receiverProfileImage)
        }
    }

    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }
}