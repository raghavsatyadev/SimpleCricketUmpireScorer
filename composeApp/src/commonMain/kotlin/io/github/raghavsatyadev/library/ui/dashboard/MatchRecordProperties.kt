package io.github.raghavsatyadev.library.ui.dashboard

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
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
  val lostColor: Color,
  val winColor: Color,
  val drawColor: Color,
  val inProgressColor: Color,
) {
  companion object {
    @Composable
    fun rememberMatchRecordProperties(): MatchRecordProperties {
      val won: String = stringResource(Res.string.won)
      val lost: String = stringResource(Res.string.lost)
      val draw: String = stringResource(Res.string.draw)
      val inProgress: String = stringResource(Res.string.in_progress)
      val lostColor = Color(0xFFCC0000) // approx holo_red_dark
      val winColor = Color(0xFF669900) // approx holo_green_dark
      val drawColor = Color(0xFF0099CC) // approx holo_blue_dark
      val inProgressColor = MaterialTheme.colorScheme.inverseSurface
      return remember(
        won,
        lost,
        draw,
        inProgress,
        lostColor,
        winColor,
        drawColor,
        inProgressColor,
      ) {
        MatchRecordProperties(
          won = won,
          lost = lost,
          draw = draw,
          inProgress = inProgress,
          lostColor = lostColor,
          winColor = winColor,
          drawColor = drawColor,
          inProgressColor = inProgressColor,
        )
      }
    }
  }
}
