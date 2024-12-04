package io.github.raghavsatyadev.support.models.db.match_record

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Team detail
 *
 * @constructor Create empty Team detail
 * @property team
 * @property runs
 * @property wickets
 * @property balls
 */
@Keep
@Parcelize
@Serializable
data class TeamDetail(
    var team: Team,
    var runs: Int = 0,
    var wickets: Int = 0,
    var balls: Int = 0,
) : Parcelable