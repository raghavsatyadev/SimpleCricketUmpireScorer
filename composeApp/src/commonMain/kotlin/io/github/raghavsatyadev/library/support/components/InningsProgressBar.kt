@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.support.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import io.github.raghavsatyadev.library.support.theme.AppMotion
import io.github.raghavsatyadev.library.support.theme.AppTheme
import io.github.raghavsatyadev.library.support.theme.animationsEnabled

/**
 * How much of the innings is bowled. Always the same wavy line, in either innings and at any point
 * of it, so it never changes shape under a tap; [behind] (a chase whose required rate is above the
 * current rate) only turns it the error colour. The wave keeps its full height from start to end:
 * the default amplitude flattens it near 0 and 1, which reads as the bar switching style. With
 * animations off it is flat throughout. New values move on [AppMotion.dataSpec] and never
 * overshoot.
 */
@Composable
fun InningsProgressBar(progress: Float, behind: Boolean, modifier: Modifier = Modifier) {
  val animated by
    animateFloatAsState(
      targetValue = progress.coerceIn(0f, 1f),
      animationSpec = AppMotion.dataSpec(),
      label = "innings progress",
    )
  val color: Color =
    if (behind) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
  val track = MaterialTheme.colorScheme.secondaryContainer
  if (animationsEnabled()) {
    LinearWavyProgressIndicator(
      progress = { animated },
      modifier = modifier.fillMaxWidth(),
      color = color,
      trackColor = track,
      amplitude = { 1f },
    )
  } else {
    LinearProgressIndicator(
      progress = { animated },
      modifier = modifier.fillMaxWidth().height(8.dp),
      color = color,
      trackColor = track,
      strokeCap = StrokeCap.Round,
    )
  }
}

@LightPreview
@DarkPreview
@Composable
private fun InningsProgressBarPreview() {
  AppTheme {
    Column(modifier = Modifier.padding(16.dp)) {
      InningsProgressBar(progress = 0.4f, behind = false)
      InningsProgressBar(
        progress = 0.7f,
        behind = true,
        modifier = Modifier.padding(top = 16.dp),
      )
    }
  }
}
