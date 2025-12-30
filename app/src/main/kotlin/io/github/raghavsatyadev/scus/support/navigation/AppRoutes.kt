package io.github.raghavsatyadev.scus.support.navigation



import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

object AppRoutes {
  @Serializable data object Dashboard : NavKey

  @Serializable data object Login : NavKey

  @Serializable
  data class CreateMatch(
    val matchRecord: io.github.raghavsatyadev.support.models.db.match_record.MatchRecord? = null
  ) : NavKey

  @Serializable data class MatchRecord(val matchId: String) : NavKey

  @Serializable data class MatchComplete(val matchId: String) : NavKey
}
