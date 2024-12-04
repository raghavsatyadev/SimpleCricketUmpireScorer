package io.github.raghavsatyadev.support.models.db.match_record

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName
import io.github.raghavsatyadev.support.Constants.DB.Tables
import io.github.raghavsatyadev.support.Constants.FieldKeys
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Match record
 *
 * @constructor Create empty Match record
 * @property id
 * @property startDateTime
 * @property endDateTime
 * @property team1Detail
 * @property team2Detail
 * @property ballsPerInning
 * @property didTeam1WonToss true if toss won by team 1
 * @property isTeam1BattingFirst true if team 1 is batting first, false if
 *    team 2 is batting first
 * @property status status of the match [MatchStatus]
 * @property location
 * @property matchAdminID
 * @property matchSharedUserIDs
 * @property isFirstInningComplete
 * @property rrrAtSecondInningStart
 */
@Keep
@Parcelize
@Serializable
@Entity(tableName = Tables.MATCH_RECORD_TABLE)
data class MatchRecord(
    @SerialName(FieldKeys.MATCH_RECORD_ID)
    @ColumnInfo(FieldKeys.MATCH_RECORD_ID)
    @get:PropertyName(FieldKeys.MATCH_RECORD_ID)
    @set:PropertyName(FieldKeys.MATCH_RECORD_ID)
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @SerialName(FieldKeys.START_DATE_TIME)
    @ColumnInfo(FieldKeys.START_DATE_TIME)
    @get:PropertyName(FieldKeys.START_DATE_TIME)
    @set:PropertyName(FieldKeys.START_DATE_TIME)
    var startDateTime: Long,

    @SerialName(FieldKeys.END_DATE_TIME)
    @ColumnInfo(FieldKeys.END_DATE_TIME)
    @get:PropertyName(FieldKeys.END_DATE_TIME)
    @set:PropertyName(FieldKeys.END_DATE_TIME)
    var endDateTime: Long = 0L,

    @Embedded(prefix = "${FieldKeys.TEAM_1}_")
    @SerialName(FieldKeys.TEAM_1)
    @get:PropertyName(FieldKeys.TEAM_1)
    @set:PropertyName(FieldKeys.TEAM_1)
    var team1Detail: TeamDetail,

    @Embedded(prefix = "${FieldKeys.TEAM_2}_")
    @SerialName(FieldKeys.TEAM_2)
    @get:PropertyName(FieldKeys.TEAM_2)
    @set:PropertyName(FieldKeys.TEAM_2)
    var team2Detail: TeamDetail,

    @SerialName(FieldKeys.BALLS_PER_INNING)
    @ColumnInfo(FieldKeys.BALLS_PER_INNING)
    @get:PropertyName(FieldKeys.BALLS_PER_INNING)
    var ballsPerInning: Int,

    @SerialName(FieldKeys.DID_TEAM_1_WON_TOSS)
    @ColumnInfo(FieldKeys.DID_TEAM_1_WON_TOSS)
    @get:PropertyName(FieldKeys.DID_TEAM_1_WON_TOSS)
    @set:PropertyName(FieldKeys.DID_TEAM_1_WON_TOSS)
    var didTeam1WonToss: Boolean = true,

    @SerialName(FieldKeys.IS_TEAM_1_BATTING_FIRST)
    @ColumnInfo(FieldKeys.IS_TEAM_1_BATTING_FIRST)
    @get:PropertyName(FieldKeys.IS_TEAM_1_BATTING_FIRST)
    @set:PropertyName(FieldKeys.IS_TEAM_1_BATTING_FIRST)
    var isTeam1BattingFirst: Boolean = true,

    @SerialName(FieldKeys.IS_FIRST_INNING_COMPLETE)
    @ColumnInfo(FieldKeys.IS_FIRST_INNING_COMPLETE)
    @get:PropertyName(FieldKeys.IS_FIRST_INNING_COMPLETE)
    @set:PropertyName(FieldKeys.IS_FIRST_INNING_COMPLETE)
    var isFirstInningComplete: Boolean = false,

    @SerialName(FieldKeys.RRR_AT_SECOND_INNING_START)
    @ColumnInfo(FieldKeys.RRR_AT_SECOND_INNING_START)
    @get:PropertyName(FieldKeys.RRR_AT_SECOND_INNING_START)
    @set:PropertyName(FieldKeys.RRR_AT_SECOND_INNING_START)
    var rrrAtSecondInningStart: String = "",

    @SerialName(FieldKeys.STATUS)
    @ColumnInfo(FieldKeys.STATUS)
    @get:PropertyName(FieldKeys.STATUS)
    @set:PropertyName(FieldKeys.STATUS)
    var status: MatchStatus = MatchStatus.NOT_STARTED,

    @SerialName(FieldKeys.LOCATION)
    @ColumnInfo(FieldKeys.LOCATION)
    @get:PropertyName(FieldKeys.LOCATION)
    @set:PropertyName(FieldKeys.LOCATION)
    var location: String = "",

    @SerialName(FieldKeys.MATCH_ADMIN_ID)
    @ColumnInfo(FieldKeys.MATCH_ADMIN_ID)
    @get:PropertyName(FieldKeys.MATCH_ADMIN_ID)
    @set:PropertyName(FieldKeys.MATCH_ADMIN_ID)
    var matchAdminID: String = "",

    @SerialName(FieldKeys.MATCH_SHARED_USER_IDS)
    @ColumnInfo(FieldKeys.MATCH_SHARED_USER_IDS)
    @get:PropertyName(FieldKeys.MATCH_SHARED_USER_IDS)
    @set:PropertyName(FieldKeys.MATCH_SHARED_USER_IDS)
    var matchSharedUserIDs: List<String> = emptyList(),
) : Parcelable
