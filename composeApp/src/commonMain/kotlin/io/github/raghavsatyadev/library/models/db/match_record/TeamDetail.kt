package io.github.raghavsatyadev.library.models.db.match_record

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import io.github.raghavsatyadev.library.Constants
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
)
