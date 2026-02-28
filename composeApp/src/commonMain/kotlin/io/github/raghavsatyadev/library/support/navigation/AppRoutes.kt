package io.github.raghavsatyadev.library.support.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoutes : NavKey {
  @Serializable data object Dashboard : AppRoutes

  @Serializable data object Login : AppRoutes

  @Serializable
  data class CreateMatch(
    val matchRecord: io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord? =
      null
  ) : AppRoutes

  @Serializable data class MatchRecord(val matchId: String) : AppRoutes

  @Serializable data class MatchComplete(val matchId: String) : AppRoutes
}
