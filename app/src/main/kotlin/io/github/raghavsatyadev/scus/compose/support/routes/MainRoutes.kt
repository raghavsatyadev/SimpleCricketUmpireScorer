package io.github.raghavsatyadev.scus.compose.support.routes

import androidx.annotation.Keep
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import kotlinx.serialization.Serializable

object MainRoutes {
    @Keep
    @Serializable
    data object DashboardScreen

    @Keep
    @Serializable
    data object LoginScreen

    @Keep
    @Serializable
    data object CreateMatchScreen

    @Keep
    @Serializable
    data class MatchRecordScreen(val matchId: String, val matchRecord: MatchRecord)
}
