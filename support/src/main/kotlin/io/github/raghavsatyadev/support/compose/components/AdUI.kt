package io.github.raghavsatyadev.support.compose.components

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import io.github.raghavsatyadev.support.BuildConfig
import io.github.raghavsatyadev.support.R

@Composable
fun AdUI(modifier: Modifier = Modifier) {
  AndroidView(
    modifier = modifier,
    factory = { context ->
      AdView(context).apply {
        setAdSize(AdSize.BANNER)
        this.adUnitId = getBannerAdID(context)
        layoutParams =
          ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
          )
        loadAd(AdRequest.Builder().build())
      }
    },
  )
}

private fun getBannerAdID(context: Context): String {
  return context.getString(
    if (BuildConfig.DEBUG) {
      R.string.admob_test_banner_ad_unit_id
    } else {
      R.string.admob_banner_ad_unit_id
    }
  )
}
