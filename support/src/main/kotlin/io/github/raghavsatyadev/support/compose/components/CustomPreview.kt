package io.github.raghavsatyadev.support.compose.components

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(showSystemUi = true, device = "spec:parent=pixel_9_pro,navigation=buttons")
annotation class LightRealDevicePreview()

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = true,
    device = "spec:parent=pixel_9_pro,navigation=buttons",
)
annotation class DarkRealDevicePreview()
