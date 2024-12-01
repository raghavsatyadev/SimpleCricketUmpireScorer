package io.github.raghavsatyadev.scuc.ui.other

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import io.github.raghavsatyadev.scuc.databinding.ActivitySplashBinding
import io.github.raghavsatyadev.scuc.ui.dashboard.DashboardActivity
import io.github.raghavsatyadev.support.Constants
import io.github.raghavsatyadev.support.core.CoreActivity
import io.github.raghavsatyadev.support.extensions.OrientationExtensions.enableFullScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CustomSplashScreen")
class SplashActivity : CoreActivity<ActivitySplashBinding>() {
    private var isStopped = false

    private var timerComplete = false

    override fun createReference(savedInstanceState: Bundle?) {
        enableFullScreen()

        startCounter()
    }

    private fun startCounter() {
        Handler(Looper.getMainLooper()).postDelayed({
            timerComplete = true
            startNewActivity()
        }, Constants.Other.SPLASH_COUNTER)
    }

    private fun startNewActivity() {
        launch {
            withContext(mainDispatcher) {
                if (timerComplete && !isStopped) {
                    finish()
                    openNextActivity()
                }
            }
        }
    }

    private fun openNextActivity() {
        startActivity(DashboardActivity.getIntentObject(this))
    }

    override fun createBinding(savedInstanceState: Bundle?) =
        ActivitySplashBinding.inflate(layoutInflater)

    override fun onStop() {
        isStopped = true
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        isStopped = false
        startNewActivity()
    }

    override fun setListeners(isEnabled: Boolean) = Unit
}