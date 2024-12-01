package io.github.raghavsatyadev.support.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class User(
    val name: String,
    val email: String,
    val userID: String,
) : Parcelable