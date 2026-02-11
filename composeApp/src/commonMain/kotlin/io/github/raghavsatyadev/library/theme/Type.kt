package io.github.raghavsatyadev.library.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import scus.composeapp.generated.resources.Res
import scus.composeapp.generated.resources.poppins_bold
import scus.composeapp.generated.resources.poppins_medium
import scus.composeapp.generated.resources.poppins_regular
import scus.composeapp.generated.resources.poppins_semibold

@Composable
fun getAppFontFamily() =
  FontFamily(
    Font(Res.font.poppins_bold, FontWeight.Bold, FontStyle.Normal),
    Font(Res.font.poppins_medium, FontWeight.Medium, FontStyle.Normal),
    Font(Res.font.poppins_regular, FontWeight.Normal, FontStyle.Normal),
    Font(Res.font.poppins_semibold, FontWeight.SemiBold, FontStyle.Normal),
  )

@Composable
fun getAppTypoGraphy(): Typography {
  val fontFamily = getAppFontFamily()

  val baseline = Typography()

  val typography =
    Typography(
      displayLarge = baseline.displayLarge.copy(fontFamily = fontFamily),
      displayMedium = baseline.displayMedium.copy(fontFamily = fontFamily),
      displaySmall = baseline.displaySmall.copy(fontFamily = fontFamily),
      headlineLarge = baseline.headlineLarge.copy(fontFamily = fontFamily),
      headlineMedium = baseline.headlineMedium.copy(fontFamily = fontFamily),
      headlineSmall = baseline.headlineSmall.copy(fontFamily = fontFamily),
      titleLarge = baseline.titleLarge.copy(fontFamily = fontFamily),
      titleMedium = baseline.titleMedium.copy(fontFamily = fontFamily),
      titleSmall = baseline.titleSmall.copy(fontFamily = fontFamily),
      bodyLarge = baseline.bodyLarge.copy(fontFamily = fontFamily),
      bodyMedium = baseline.bodyMedium.copy(fontFamily = fontFamily),
      bodySmall = baseline.bodySmall.copy(fontFamily = fontFamily),
      labelLarge = baseline.labelLarge.copy(fontFamily = fontFamily),
      labelMedium = baseline.labelMedium.copy(fontFamily = fontFamily),
      labelSmall = baseline.labelSmall.copy(fontFamily = fontFamily),
    )

  return typography
}
