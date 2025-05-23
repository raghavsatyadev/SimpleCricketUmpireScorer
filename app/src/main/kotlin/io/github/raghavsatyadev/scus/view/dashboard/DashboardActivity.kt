package io.github.raghavsatyadev.scus.view.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.scus.databinding.ActivityDashboardBinding
import io.github.raghavsatyadev.scus.view.create_match.CreateMatchActivity
import io.github.raghavsatyadev.scus.view.login.LoginActivity
import io.github.raghavsatyadev.scus.view.match_complete.MatchCompleteActivity
import io.github.raghavsatyadev.scus.view.match_record.MatchRecordActivity
import io.github.raghavsatyadev.support.core.CoreActivity
import io.github.raghavsatyadev.support.extensions.activity_result.ActivityResultExtensions.registerActivityForResult
import io.github.raghavsatyadev.support.extensions.activity_result.ResultType
import io.github.raghavsatyadev.support.extensions.ads.AdExtensions.loadAds
import io.github.raghavsatyadev.support.extensions.ads.AdExtensions.showInterstitialAd
import io.github.raghavsatyadev.support.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.list.CustomClickListener
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.isMatchCompleted
import io.github.raghavsatyadev.support.models.essential.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Date

class DashboardActivity : CoreActivity<ActivityDashboardBinding>() {
  private val viewModel: DashboardViewModel by viewModels()
  private val adapter: MatchRecordAdapter by lazy { MatchRecordAdapter(this) }

  companion object {
    fun getIntentObject(context: Context, bundle: Bundle = Bundle.EMPTY): Intent =
      Intent(context, DashboardActivity::class.java).apply { putExtras(bundle) }
  }

  private val launcher = registerActivityForResult { resultType, _ ->
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
      if (FirebaseAuthUtil.getInstance().isLoggedIn()) {
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
        viewModel.getMatchRecordsEvent().collectLatest {
          withContext(mainDispatcher) {
            when (it.status) {
              Resource.Status.SUCCESS -> {
                adapter.replaceAll(it.data)
              }

              else -> {}
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
        showInterstitialAd { startActivity(CreateMatchActivity.getIntentObject(this)) }
      }
      adapter.itemClickListener =
        CustomClickListener(
          onClick = { position, view, _ ->
            val record = adapter.getItem(position)
            when (view?.id) {
              R.id.btn_copy -> {
                launch {
                  val matchRecord = MatchRecord(
                    location = record.location,
                    startDateTime = Instant
                      .now()
                      .toEpochMilli(),
                    ballsPerInning = record.ballsPerInning,
                    team1Detail = record.team1Detail,
                    team2Detail = record.team2Detail,
                    didTeam1WonToss = record.didTeam1WonToss,
                    isTeam1BattingFirst = record.isTeam1BattingFirst,
                    localUpdateDateTime = Date(),
                    serverUpdateDateTime = Date(),
                    matchAdminID = record.matchAdminID,
                    )
                  startActivity(
                    CreateMatchActivity.getIntentObject(
                      this@DashboardActivity,
                      Bundle().apply {
                        putParcelable(CreateMatchActivity.MATCH_RECORD, matchRecord)
                      },
                    )
                  )
                }
              }

              R.id.btn_delete -> {
                showDeleteDialog(record)
              }

              else -> {
                if (record.isMatchCompleted()) {
                  startActivity(MatchCompleteActivity.getIntentObject(this, record.matchRecordId))
                } else {
                  startActivity(MatchRecordActivity.getIntentObject(this, record))
                }
              }
            }
          }
        )
    } else {
      binding.btnAddMatch.setOnClickListener(null)
    }
  }

  private fun showDeleteDialog(record: MatchRecord) {
    MaterialAlertDialogBuilder(this)
      .setTitle(getString(R.string.delete_match_record))
      .setMessage(getString(R.string.delete_match_record_message))
      .setPositiveButton(getString(R.string.yes)) { _, _ ->
        launch { viewModel.deleteMatchRecord(record) }
      }
      .setNegativeButton(getString(R.string.no)) { _, _ -> }
      .show()
  }
}
