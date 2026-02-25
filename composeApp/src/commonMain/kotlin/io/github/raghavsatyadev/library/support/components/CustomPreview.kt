package io.github.raghavsatyadev.library.support.components

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview

@Preview(
  showBackground = true,
  showSystemUi = true,
  uiMode = AndroidUiModes.UI_MODE_NIGHT_NO or AndroidUiModes.UI_MODE_TYPE_NORMAL,
  device = "spec:parent=pixel_9,navigation=buttons",
)
annotation class LightRealDevicePreview

@Preview(
  showBackground = true,
  uiMode = AndroidUiModes.UI_MODE_NIGHT_YES or AndroidUiModes.UI_MODE_TYPE_NORMAL,
  showSystemUi = true,
  device = "spec:parent=pixel_9,navigation=buttons",
)
annotation class DarkRealDevicePreview

@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
annotation class LightPreview

@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
annotation class DarkPreview
