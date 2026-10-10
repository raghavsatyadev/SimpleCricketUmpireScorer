package io.github.raghavsatyadev.support.models.db.match_record

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import io.github.raghavsatyadev.support.Constants
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Team detail
 *
 * @property teamName
 * @property runs
 * @property wickets
 * @property balls
 * @constructor Create empty Team detail
 */
@Keep
@Parcelize
@Serializable
data class TeamDetail(
  @SerialName(Constants.FieldKeys.TEAM_NAME)
  @ColumnInfo(Constants.FieldKeys.TEAM_NAME)
  var teamName: String,
  @SerialName(Constants.FieldKeys.RUNS) @ColumnInfo(Constants.FieldKeys.RUNS) var runs: Int = 0,
  @SerialName(Constants.FieldKeys.WICKETS)
  @ColumnInfo(Constants.FieldKeys.WICKETS)
  var wickets: Int = 0,
  @SerialName(Constants.FieldKeys.BALLS) @ColumnInfo(Constants.FieldKeys.BALLS) var balls: Int = 0,
) : Parcelable
