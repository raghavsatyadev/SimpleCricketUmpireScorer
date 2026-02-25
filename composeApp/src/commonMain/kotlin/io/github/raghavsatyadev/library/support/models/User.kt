package io.github.raghavsatyadev.library.support.models

import androidx.room.ColumnInfo
import io.github.raghavsatyadev.library.support.Constants.FieldKeys
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
  var name: String,
  var email: String,
  @SerialName(FieldKeys.USER_ID) @ColumnInfo(FieldKeys.USER_ID) var userID: String,
  @SerialName(FieldKeys.LOGIN_TOKEN)
  @ColumnInfo(FieldKeys.LOGIN_TOKEN)
  var loginToken: String? = null,
)
