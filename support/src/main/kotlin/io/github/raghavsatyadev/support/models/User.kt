package io.github.raghavsatyadev.support.models

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import com.google.firebase.firestore.PropertyName
import io.github.raghavsatyadev.support.Constants.FieldKeys
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Parcelize
@Serializable
data class User(
    var name: String,
    var email: String,

    @SerialName(FieldKeys.USER_ID)
    @get:PropertyName(FieldKeys.USER_ID)
    @set:PropertyName(FieldKeys.USER_ID)
    @ColumnInfo(FieldKeys.USER_ID)
    var userID: String,

    @SerialName(FieldKeys.LOGIN_TOKEN)
    @get:PropertyName(FieldKeys.LOGIN_TOKEN)
    @set:PropertyName(FieldKeys.LOGIN_TOKEN)
    @ColumnInfo(FieldKeys.LOGIN_TOKEN)
    var loginToken: String? = null,
) : Parcelable