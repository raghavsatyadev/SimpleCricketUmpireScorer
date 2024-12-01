package io.github.raghavsatyadev.support.extensions

import android.app.Activity
import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import io.github.raghavsatyadev.support.extensions.ImplicitIntentExtensions.openPlayServiceUpdate
import io.github.raghavsatyadev.support.extensions.Randoms.randomInt

@Suppress("unused")
object GoogleExtensions {
    private const val REQUIRED_PLAY_SERVICES_VERSION: Int = 241427032

    fun Activity.checkPlayServiceAndShowError(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val (playServiceStatus, availabilityCode) = checkPlayServiceGetResultCode()

        return when (playServiceStatus) {
            PlayServiceStatus.AVAILABLE -> {
                true
            }

            PlayServiceStatus.UPDATE_REQUIRED -> {
                openPlayServiceUpdate()
                finishAffinity()
                false
            }

            else -> {
                val errorDialog =
                    apiAvailability.getErrorDialog(this, availabilityCode, randomInt())

                if (errorDialog != null) {
                    errorDialog.setOnDismissListener {
                        finishAffinity()
                    }
                    errorDialog.show()
                } else {
                    finishAffinity()
                }
                false
            }
        }
    }

    private fun Context.checkPlayServiceGetResultCode(): Pair<PlayServiceStatus, Int> {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val isAvailable = apiAvailability.isGooglePlayServicesAvailable(this)
        val playServicesVersion = apiAvailability.getApkVersion(this)

        return if (isAvailable == ConnectionResult.SUCCESS) {
            if (playServicesVersion >= REQUIRED_PLAY_SERVICES_VERSION) {
                PlayServiceStatus.AVAILABLE to ConnectionResult.SUCCESS
            } else PlayServiceStatus.UPDATE_REQUIRED to isAvailable
        } else PlayServiceStatus.NOT_AVAILABLE to isAvailable
    }

    enum class PlayServiceStatus {
        AVAILABLE,
        NOT_AVAILABLE,
        UPDATE_REQUIRED
    }

    fun Context.checkPlayServiceAvailability(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val isAvailable = apiAvailability.isGooglePlayServicesAvailable(this)
        val playServicesVersion = apiAvailability.getApkVersion(this)

        return isAvailable == ConnectionResult.SUCCESS && playServicesVersion >= REQUIRED_PLAY_SERVICES_VERSION
    }
}