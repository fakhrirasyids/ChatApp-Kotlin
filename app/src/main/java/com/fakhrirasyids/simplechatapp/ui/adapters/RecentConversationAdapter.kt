package com.fakhrirasyids.simplechatapp.ui.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fakhrirasyids.simplechatapp.databinding.ItemRecentConversationContainerBinding
import com.fakhrirasyids.simplechatapp.listeners.ConversationsListener
import com.fakhrirasyids.simplechatapp.listeners.UserListener
import com.fakhrirasyids.simplechatapp.models.ChatMessage
import com.fakhrirasyids.simplechatapp.models.User

class RecentConversationAdapter(private var chatMessage: List<ChatMessage>) :
    RecyclerView.Adapter<RecentConversationAdapter.ViewHolder>() {
    private lateinit var conversationsListener: ConversationsListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentConversationContainerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(chatMessage[position])
        holder.itemView.setOnClickListener {
            val user = User()
            user.id = chatMessage[position].conversationId
            user.name = chatMessage[position].conversationName
            user.image = chatMessage[position].conversationImage
            conversationsListener.onConversationClicked(user)
        }
    }

    override fun getItemCount() = chatMessage.size

    class ViewHolder(private var binding: ItemRecentConversationContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            binding.ivProfile.setImageBitmap(getConversationImage(chatMessage.conversationImage))
            binding.tvName.text = chatMessage.conversationName
            binding.tvRecentMessage.text = chatMessage.message
        }

        private fun getConversationImage(encodedImage: String?): Bitmap {
            val bytes: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    fun setConversationListener(conversationsListener: ConversationsListener) {
        this.conversationsListener = conversationsListener
    }
}