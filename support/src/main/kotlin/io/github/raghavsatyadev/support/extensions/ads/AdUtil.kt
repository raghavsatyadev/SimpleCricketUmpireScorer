package io.github.raghavsatyadev.support.extensions.ads

import android.app.Activity
import android.view.Display
import androidx.core.hardware.display.DisplayManagerCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.github.raghavsatyadev.support.BuildConfig
import io.github.raghavsatyadev.support.R

object AdUtil {
    fun loadBannerAd(activity: Activity, adContainerView: AdView) {
        val adView = AdView(activity)
        adView.setAdSize(getAdSize(activity, adContainerView))
        adView.adUnitId = getBannerAdID(activity)
        adContainerView.removeAllViews()
        adContainerView.addView(adView)

        val adRequest: AdRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun getBannerAdID(activity: Activity): String {
        return activity.getString(
            if (BuildConfig.DEBUG) {
                R.string.admob_test_banner_ad_unit_id
            } else {
                R.string.admob_banner_ad_unit_id
            }
        )
    }

    private fun getAdSize(activity: Activity, adContainerView: AdView): AdSize {
        val defaultDisplay =
            DisplayManagerCompat.getInstance(activity).getDisplay(Display.DEFAULT_DISPLAY)
        val metrics = activity.createDisplayContext(defaultDisplay!!).resources.displayMetrics

        val density = metrics.density

        var adWidthPixels = adContainerView.width

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = metrics.widthPixels
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }


    fun loadInterstitialAd(activity: Activity, listener: ((InterstitialAd?) -> Unit)) {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity, getInterstitialAdID(activity), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    ad.setImmersiveMode(true)
                    listener(ad)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    listener(null)
                }
            })
    }


    private fun getInterstitialAdID(activity: Activity): String {
        return activity.getString(
            if (BuildConfig.DEBUG) {
                R.string.admob_test_interstitial_ad_unit_id
            } else {
                R.string.admob_interstitial_ad_unit_id
            }
        )
    }
}