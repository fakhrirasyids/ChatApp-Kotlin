package com.fakhrirasyids.simplechatapp.listeners

import com.fakhrirasyids.simplechatapp.models.User

interface ConversationsListener {
    fun onConversationClicked(user: User)
}