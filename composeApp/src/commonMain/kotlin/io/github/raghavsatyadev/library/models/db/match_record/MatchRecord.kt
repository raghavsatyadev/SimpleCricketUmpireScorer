package io.github.raghavsatyadev.library.models.db.match_record

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.raghavsatyadev.library.Constants.DB.Tables
import io.github.raghavsatyadev.library.Constants.FieldKeys
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Keep
@Serializable
@Entity(tableName = Tables.MATCH_RECORD_TABLE)
data class MatchRecord(
  @SerialName(FieldKeys.MATCH_RECORD_ID)
  @ColumnInfo(FieldKeys.MATCH_RECORD_ID)
  @PrimaryKey
  var matchRecordId: String = "",
  @SerialName(FieldKeys.START_DATE_TIME)
  @ColumnInfo(FieldKeys.START_DATE_TIME)
  var startDateTime: Long,
  @SerialName(FieldKeys.END_DATE_TIME)
  @ColumnInfo(FieldKeys.END_DATE_TIME)
  var endDateTime: Long = 0L,
  @Embedded(prefix = "${FieldKeys.TEAM_1}_")
  @SerialName(FieldKeys.TEAM_1)
  var team1Detail: TeamDetail,
  @Embedded(prefix = "${FieldKeys.TEAM_2}_")
  @SerialName(FieldKeys.TEAM_2)
  var team2Detail: TeamDetail,
  @SerialName(FieldKeys.BALLS_PER_INNING)
  @ColumnInfo(FieldKeys.BALLS_PER_INNING)
  var ballsPerInning: Int,
  @SerialName(FieldKeys.DID_TEAM_1_WON_TOSS)
  @ColumnInfo(FieldKeys.DID_TEAM_1_WON_TOSS)
  var didTeam1WonToss: Boolean = true,
  @SerialName(FieldKeys.IS_TEAM_1_BATTING_FIRST)
  @ColumnInfo(FieldKeys.IS_TEAM_1_BATTING_FIRST)
  var isTeam1BattingFirst: Boolean = true,
  @SerialName(FieldKeys.IS_FIRST_INNING_COMPLETE)
  @ColumnInfo(FieldKeys.IS_FIRST_INNING_COMPLETE)
  var isFirstInningComplete: Boolean = false,
  @SerialName(FieldKeys.RRR_AT_SECOND_INNING_START)
  @ColumnInfo(FieldKeys.RRR_AT_SECOND_INNING_START)
  var rrrAtSecondInningStart: String = "",
  @SerialName(FieldKeys.STATUS)
  @ColumnInfo(FieldKeys.STATUS)
  var status: MatchStatus = MatchStatus.NOT_STARTED,
  @SerialName(FieldKeys.LOCATION) @ColumnInfo(FieldKeys.LOCATION) var location: String = "",
  @SerialName(FieldKeys.MATCH_ADMIN_ID)
  @ColumnInfo(FieldKeys.MATCH_ADMIN_ID)
  var matchAdminID: String,
  @SerialName(FieldKeys.MATCH_SHARED_USER_IDS)
  @ColumnInfo(FieldKeys.MATCH_SHARED_USER_IDS)
  var matchSharedUserIDs: List<String> = emptyList(),
  @SerialName(FieldKeys.LOCAL_UPDATE_DATE_TIME)
  @ColumnInfo(FieldKeys.LOCAL_UPDATE_DATE_TIME)
  @Contextual
  var localUpdateDateTime: Date? = null,
  @SerialName(FieldKeys.SERVER_UPDATE_DATE_TIME)
  @ColumnInfo(FieldKeys.SERVER_UPDATE_DATE_TIME)
  @Contextual
  var serverUpdateDateTime: Date? = null,
)
