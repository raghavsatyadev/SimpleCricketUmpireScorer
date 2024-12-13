package io.github.raghavsatyadev.scuc.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import io.github.raghavsatyadev.scuc.databinding.ActivityDashboardBinding
import io.github.raghavsatyadev.scuc.ui.create_match.CreateMatchActivity
import io.github.raghavsatyadev.scuc.ui.login.LoginActivity
import io.github.raghavsatyadev.scuc.ui.match_complete.MatchCompleteActivity
import io.github.raghavsatyadev.scuc.ui.match_record.MatchRecordActivity
import io.github.raghavsatyadev.support.core.CoreActivity
import io.github.raghavsatyadev.support.extensions.activity_result.ActivityResultExtensions.registerActivityForResult
import io.github.raghavsatyadev.support.extensions.activity_result.ResultType
import io.github.raghavsatyadev.support.extensions.ads.AdExtensions.loadAds
import io.github.raghavsatyadev.support.extensions.ads.AdExtensions.showInterstitialAd
import io.github.raghavsatyadev.support.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.list.CustomClickListener
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.isMatchCompleted
import io.github.raghavsatyadev.support.models.essential.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardActivity : CoreActivity<ActivityDashboardBinding>() {
    private val viewModel: DashboardViewModel by viewModels()
    private val adapter: MatchRecordAdapter by lazy { MatchRecordAdapter(this) }

    companion object {
        fun getIntentObject(
            context: Context,
            bundle: Bundle = Bundle.EMPTY,
        ): Intent = Intent(
            context,
            DashboardActivity::class.java
        ).apply { putExtras(bundle) }
    }

    val launcher = registerActivityForResult { resultType, _ ->
        if (resultType == ResultType.OK) {
            loadUI()
        } else {
            finishAffinity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
    }

    override fun createReference(savedInstanceState: Bundle?) {
        loadAds(binding.adView)

        setupUI()
    }

    private fun setupUI() {
        binding.listMatchRecords.adapter = adapter

        lifecycleScope.launch {
            if (FirebaseAuthUtil
                    .getInstance()
                    .isLoggedIn()
            ) {
                loadUI()
            } else {
                openLoginActivity()
            }
        }
    }

    private fun openLoginActivity() {
        launcher.launch(LoginActivity.getIntentObject(this))
    }

    private fun loadUI() {
        lifecycleScope.launch {
            withContext(ioDispatcher) {
                viewModel.loadMatchRecords()
                viewModel
                    .getMatchRecordsEvent()
                    .collectLatest {
                        withContext(mainDispatcher) {
                            when (it.status) {
                                Resource.Status.SUCCESS -> {
                                    adapter.replaceAll(it.data)
                                }

                                else -> {

                                }
                            }
                        }
                    }
            }
        }
    }

    override fun createBinding(savedInstanceState: Bundle?) =
        ActivityDashboardBinding.inflate(layoutInflater)

    override fun getToolBar() = binding.toolbar

    override fun setListeners(isEnabled: Boolean) {
        if (isEnabled) {
            binding.btnAddMatch.setOnClickListener {
                showInterstitialAd {
                    startActivity(CreateMatchActivity.getIntentObject(this))
                }
            }
            adapter.itemClickListener = CustomClickListener(onClick = { position, _, _ ->
                val record = adapter.getItem(position)
                if (record.isMatchCompleted()) {
                    startActivity(
                        MatchCompleteActivity.getIntentObject(
                            this,
                            record.matchRecordId
                        )
                    )
                } else {
                    startActivity(
                        MatchRecordActivity.getIntentObject(
                            this,
                            record
                        )
                    )
                }
            })
        } else {
            binding.btnAddMatch.setOnClickListener(null)
        }
    }
}

