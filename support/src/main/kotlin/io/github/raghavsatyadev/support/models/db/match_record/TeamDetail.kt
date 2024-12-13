package io.github.raghavsatyadev.support.models.db.match_record

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import com.google.firebase.firestore.PropertyName
import io.github.raghavsatyadev.support.Constants
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Team detail
 *
 * @constructor Create empty Team detail
 * @property teamName
 * @property runs
 * @property wickets
 * @property balls
 */
@Keep
@Parcelize
@Serializable
data class TeamDetail(
    @SerialName(Constants.FieldKeys.TEAM_NAME)
    @ColumnInfo(Constants.FieldKeys.TEAM_NAME)
    @get:PropertyName(Constants.FieldKeys.TEAM_NAME)
    @set:PropertyName(Constants.FieldKeys.TEAM_NAME)
    var teamName: String,
    var runs: Int = 0,
    var wickets: Int = 0,
    var balls: Int = 0,
) : Parcelable