package io.github.raghavsatyadev.library.support.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import scus.composeapp.generated.resources.Res
import scus.composeapp.generated.resources.montserrat_bold
import scus.composeapp.generated.resources.montserrat_medium
import scus.composeapp.generated.resources.montserrat_regular
import scus.composeapp.generated.resources.montserrat_semi_bold

@Composable
fun getAppFontFamily() =
  FontFamily(
    Font(Res.font.montserrat_regular, FontWeight.Normal, FontStyle.Normal),
    Font(Res.font.montserrat_medium, FontWeight.Medium, FontStyle.Normal),
    Font(Res.font.montserrat_semi_bold, FontWeight.SemiBold, FontStyle.Normal),
    Font(Res.font.montserrat_bold, FontWeight.Bold, FontStyle.Normal),
  )

@Composable
fun getAppTypoGraphy(): Typography {
  val fontFamily = getAppFontFamily()

  val baseline = Typography()

  val typography =
    Typography(
      displayLarge =
        baseline.displayLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold),
      displayMedium =
        baseline.displayMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold),
      displaySmall =
        baseline.displaySmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold),
      headlineLarge =
        baseline.headlineLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold),
      headlineMedium =
        baseline.headlineMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold),
      headlineSmall =
        baseline.headlineSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold),
      titleLarge =
        baseline.titleLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
      titleMedium =
        baseline.titleMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
      titleSmall =
        baseline.titleSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
      bodyLarge = baseline.bodyLarge.copy(fontFamily = fontFamily),
      bodyMedium = baseline.bodyMedium.copy(fontFamily = fontFamily),
      bodySmall = baseline.bodySmall.copy(fontFamily = fontFamily),
      labelLarge =
        baseline.labelLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
      labelMedium = baseline.labelMedium.copy(fontFamily = fontFamily),
      labelSmall = baseline.labelSmall.copy(fontFamily = fontFamily),
    )

  return typography
}
