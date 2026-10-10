package io.github.raghavsatyadev.library.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.jetbrains.compose.resources.stringResource
import scus.composeapp.generated.resources.Res
import scus.composeapp.generated.resources.draw
import scus.composeapp.generated.resources.in_progress
import scus.composeapp.generated.resources.lost
import scus.composeapp.generated.resources.won

data class MatchRecordProperties(
  val won: String,
  val lost: String,
  val draw: String,
  val inProgress: String,
) {
  companion object {
    @Composable
    fun rememberMatchRecordProperties(): MatchRecordProperties {
      val won: String = stringResource(Res.string.won)
      val lost: String = stringResource(Res.string.lost)
      val draw: String = stringResource(Res.string.draw)
      val inProgress: String = stringResource(Res.string.in_progress)
      return remember(won, lost, draw, inProgress) {
        MatchRecordProperties(won = won, lost = lost, draw = draw, inProgress = inProgress)
      }
    }
  }
}
