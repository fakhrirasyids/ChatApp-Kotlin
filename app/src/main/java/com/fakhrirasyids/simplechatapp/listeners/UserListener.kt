package com.fakhrirasyids.simplechatapp.listeners

import com.fakhrirasyids.simplechatapp.models.User

interface UserListener {
    fun onUserClicked(user: User)
}