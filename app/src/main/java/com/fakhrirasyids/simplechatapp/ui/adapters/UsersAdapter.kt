package com.fakhrirasyids.simplechatapp.ui.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fakhrirasyids.simplechatapp.databinding.ItemUserContainerBinding
import com.fakhrirasyids.simplechatapp.listeners.UserListener
import com.fakhrirasyids.simplechatapp.models.User

class UsersAdapter(private var listUser: List<User>) :
    RecyclerView.Adapter<UsersAdapter.ViewHolder>() {
    private lateinit var userListener: UserListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemUserContainerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listUser[position])
        holder.itemView.setOnClickListener {
            userListener.onUserClicked(listUser[position])
        }
    }

    override fun getItemCount() = listUser.size

    class ViewHolder(private var binding: ItemUserContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(users: User) {
            binding.tvName.text = users.name
            binding.ivProfile.setImageBitmap(getUserImage(users.image!!))
        }

        private fun getUserImage(encodedImage: String): Bitmap {
            val bytes: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    fun setUserListener(userListener: UserListener) {
        this.userListener = userListener
    }
}