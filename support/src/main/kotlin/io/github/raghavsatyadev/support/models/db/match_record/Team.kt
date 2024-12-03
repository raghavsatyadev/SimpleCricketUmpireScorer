package io.github.raghavsatyadev.support.models.db.match_record

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName
import io.github.raghavsatyadev.support.Constants.DB.Tables
import io.github.raghavsatyadev.support.Constants.FieldKeys
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Parcelize
@Serializable
@Entity(tableName = Tables.TEAM_TABLE)
data class Team(
    @PrimaryKey
    @SerialName(FieldKeys.TEAM_ID)
    @ColumnInfo(FieldKeys.TEAM_ID)
    @get:PropertyName(FieldKeys.TEAM_ID)
    @set:PropertyName(FieldKeys.TEAM_ID)
    var teamID: String = "",

    var name: String,
) : Parcelable
