package io.github.raghavsatyadev.support.models.db.match_record

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
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
 * @property team1Detail
 * @property team2Detail
 * @property ballsPerInning
 * @property tossWonByTeam1 true if toss won by team 1
 * @property isTeam1BattingFirst true if team 1 is batting first, false if
 *    team 2 is batting first
 * @property status
 * @property location
 */
@Keep
@Parcelize
@Serializable
@Entity(tableName = Tables.MATCH_RECORD_TABLE)
data class MatchRecord(
    @SerialName(FieldKeys.MATCH_RECORD_ID)
    @ColumnInfo(FieldKeys.MATCH_RECORD_ID)
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    var startDateTime: Long,

    var endDateTime: Long = 0L,

    @Embedded(prefix = "team1_")
    var team1Detail: TeamDetail,

    @Embedded(prefix = "team2_")
    var team2Detail: TeamDetail,

    var ballsPerInning: Int,

    var tossWonByTeam1: Boolean = true,

    var isTeam1BattingFirst: Boolean = true,

    var isFirstInningComplete: Boolean = false,

    var rrrAt2ndInningStart: String = "",

    var status: MatchStatus = MatchStatus.NOT_STARTED,

    var location: String = "",
) : Parcelable
