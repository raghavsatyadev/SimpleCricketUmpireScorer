package io.github.raghavsatyadev.support.models.db.match_record

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Keep
@Parcelize
@Serializable
data class Team(
    var name: String,
) : Parcelable
