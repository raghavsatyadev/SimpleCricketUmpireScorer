package io.github.raghavsatyadev.support.compose.startup

import android.content.Context
import android.widget.Toast
import androidx.startup.Initializer
import com.google.android.gms.ads.MobileAds
import io.github.raghavsatyadev.support.BuildConfig
import io.github.raghavsatyadev.support.R
import io.github.raghavsatyadev.support.google.GoogleExtensions.checkPlayServiceAvailability

class MobileAdsInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    if (context.checkPlayServiceAvailability()) {
      if (BuildConfig.DEBUG) {
        MobileAds.setRequestConfiguration(
          MobileAds.getRequestConfiguration()
            .toBuilder()
            .setTestDeviceIds(listOf("9CC8AD8C783D84C97DF0A252B0F63285"))
            .build()
        )
      }
      MobileAds.initialize(context)
    } else {
      Toast.makeText(context, R.string.warning_update_play_service, Toast.LENGTH_SHORT).show()
    }
  }

  override fun dependencies() = emptyList<Class<out Initializer<*>?>?>()
}
