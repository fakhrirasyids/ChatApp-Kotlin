package com.fakhrirasyids.simplechatapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var name: String? = null,
    var image: String? = null,
    var email: String? = null,
    var token: String? = null,
    var id: String? = null
) : Parcelable