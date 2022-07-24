package com.fakhrirasyids.simplechatapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class ChatMessage(
    var senderId: String? = null,
    var receiverId: String? = null,
    var message: String? = null,
    var dateTime: String? = null,
    var dateObject: Date? = null,
    var conversationId: String? = null,
    var conversationName: String? = null,
    var conversationImage: String? = null
) : Parcelable