package io.github.raghavsatyadev.support.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.firebase.firestore.PropertyName
import io.github.raghavsatyadev.support.Constants.FieldKeys
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class User(
    var name: String,
    var email: String,

    @SerialName(FieldKeys.USER_ID)
    @get:PropertyName(FieldKeys.USER_ID)
    @set:PropertyName(FieldKeys.USER_ID)
    @ColumnInfo(FieldKeys.USER_ID)
    var userID: String,
) : Parcelable