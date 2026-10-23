package io.github.raghavsatyadev.library.support.models.db.match_record

import androidx.room.ColumnInfo
import io.github.raghavsatyadev.library.support.Constants
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
@Serializable
data class TeamDetail(
  @SerialName(Constants.FieldKeys.TEAM_NAME)
  @ColumnInfo(Constants.FieldKeys.TEAM_NAME)
  var teamName: String,
  @SerialName(Constants.FieldKeys.RUNS)
  @ColumnInfo(name = Constants.FieldKeys.RUNS)
  var runs: Int = 0,
  @SerialName(Constants.FieldKeys.WICKETS)
  @ColumnInfo(name = Constants.FieldKeys.WICKETS)
  var wickets: Int = 0,
  @SerialName(Constants.FieldKeys.BALLS)
  @ColumnInfo(name = Constants.FieldKeys.BALLS)
  var balls: Int = 0,
)
