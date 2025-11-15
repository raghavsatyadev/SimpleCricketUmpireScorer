package io.github.raghavsatyadev.scus.compose.support.navigation

import androidx.annotation.Keep
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

object AppRoutes {
  @Keep @Serializable data object Dashboard : NavKey

  @Keep @Serializable data object Login : NavKey

  @Keep
  @Serializable
  data class CreateMatch(
    val matchRecord: io.github.raghavsatyadev.support.models.db.match_record.MatchRecord? = null
  ) : NavKey

  @Keep @Serializable data class MatchRecord(val matchId: String) : NavKey

  @Keep @Serializable data class MatchComplete(val matchId: String) : NavKey
}
