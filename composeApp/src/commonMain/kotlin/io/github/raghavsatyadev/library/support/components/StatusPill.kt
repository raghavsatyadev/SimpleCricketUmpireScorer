@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.support.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.raghavsatyadev.library.support.theme.AppTheme

enum class StatusTone {
  Positive,
  Negative,
  Neutral,
  Active,
}

@Composable
fun StatusPill(text: String, tone: StatusTone, modifier: Modifier = Modifier) {
  val colors = MaterialTheme.colorScheme
  val motion = MaterialTheme.motionScheme
  val container: Color
  val onContainer: Color
  when (tone) {
    StatusTone.Positive -> {
      container = colors.primaryContainer
      onContainer = colors.onPrimaryContainer
    }
    StatusTone.Negative -> {
      container = colors.errorContainer
      onContainer = colors.onErrorContainer
    }
    StatusTone.Neutral -> {
      container = colors.tertiaryContainer
      onContainer = colors.onTertiaryContainer
    }
    StatusTone.Active -> {
      container = colors.secondaryContainer
      onContainer = colors.onSecondaryContainer
    }
  }
  Surface(
    shape = MaterialTheme.shapes.small,
    color = container,
    contentColor = onContainer,
    modifier = modifier.animateContentSize(motion.fastSpatialSpec()),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
    )
  }
}

@Composable
private fun StatusPillSamples() {
  Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      StatusPill("Won", StatusTone.Positive)
      StatusPill("Lost", StatusTone.Negative)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      StatusPill("Draw", StatusTone.Neutral)
      StatusPill("In progress", StatusTone.Active)
    }
  }
}

@LightPreview
@Composable
private fun StatusPillLightPreview() {
  AppTheme(darkTheme = false) { StatusPillSamples() }
}

@DarkPreview
@Composable
private fun StatusPillDarkPreview() {
  AppTheme(darkTheme = true) { StatusPillSamples() }
}
