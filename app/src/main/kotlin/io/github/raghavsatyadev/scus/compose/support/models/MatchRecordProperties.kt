package io.github.raghavsatyadev.scus.compose.support.models

import androidx.annotation.Keep
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import io.github.raghavsatyadev.scus.R

@Keep
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
      val won: String = stringResource(R.string.won)
      val lost: String = stringResource(R.string.lost)
      val draw: String = stringResource(R.string.draw)
      val inProgress: String = stringResource(R.string.in_progress)
      val lostColor = colorResource(android.R.color.holo_red_dark)
      val winColor = colorResource(android.R.color.holo_green_dark)
      val drawColor = colorResource(android.R.color.holo_blue_dark)
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
