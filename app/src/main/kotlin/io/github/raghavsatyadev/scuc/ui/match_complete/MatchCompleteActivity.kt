package io.github.raghavsatyadev.scuc.ui.match_complete

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButtonToggleGroup
import io.github.raghavsatyadev.scuc.R
import io.github.raghavsatyadev.scuc.databinding.ActivityMatchCompleteBinding
import io.github.raghavsatyadev.support.core.CoreActivity
import io.github.raghavsatyadev.support.extensions.ViewExtensions.gone
import io.github.raghavsatyadev.support.extensions.ViewExtensions.visible
import io.github.raghavsatyadev.support.extensions.ads.AdExtensions.loadAds
import io.github.raghavsatyadev.support.models.BasicMatchUIDetails
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.toBasicMatchUIDetails
import io.github.raghavsatyadev.support.models.db.match_record.MatchStatus
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MatchCompleteActivity : CoreActivity<ActivityMatchCompleteBinding>() {
    companion object {
        private const val MATCH_RECORD_ID = "match_record_id"
        fun getIntentObject(
            context: Context,
            matchRecordID: Long,
        ): Intent = Intent(context, MatchCompleteActivity::class.java).apply {
            putExtra(MATCH_RECORD_ID, matchRecordID)
        }
    }

    private val viewModel: MatchCompleteViewModel by viewModels()
    private var matchRecordId: Long = -1
    private lateinit var matchRecord: MatchRecord
    private lateinit var basicDetailsTeam1: BasicMatchUIDetails
    private lateinit var basicDetailsTeam2: BasicMatchUIDetails
    private val buttonCheckedListener = object : MaterialButtonToggleGroup.OnButtonCheckedListener {
        override fun onButtonChecked(
            group: MaterialButtonToggleGroup?,
            checkedId: Int,
            isChecked: Boolean,
        ) {
            if (isChecked) {
                when (checkedId) {
                    binding.btnMatchDetailTeam1.id -> {
                        loadTeamDetails(true)
                    }

                    binding.btnMatchDetailTeam2.id -> {
                        loadTeamDetails(false)
                    }
                }
            }
        }
    }

    private fun loadTeamDetails(loadTeam1Details: Boolean) {
        val details = if (loadTeam1Details) basicDetailsTeam1 else basicDetailsTeam2
        with(details) {
            val isBattingFirst = matchRecord.isTeam1BattingFirst
            val showRequiredScore =
                (loadTeam1Details && !isBattingFirst) || (!loadTeam1Details && isBattingFirst)

            if (showRequiredScore) {
                binding.txtRrr.text =
                    getString(R.string.rrr_at_start, matchRecord.rrrAtSecondInningStart)
                binding.txtRequiredRunsBalls.text =
                    getString(R.string.required_runs_balls_at_end, requiredRunsBalls)
                binding.groupRequiredScore.visible()
            } else {
                binding.groupRequiredScore.gone()
            }

            binding.txtTeamName.text = currentTeamName
            binding.txtRunsWickets.text = currentRunsAndWickets
            binding.txtOvers.text = currentOvers
            binding.txtCrr.text = getString(R.string.crr, currentCRR)
        }
    }

    override fun createReference(savedInstanceState: Bundle?) {
        loadAds(binding.adView)
        setToolBarTitle(R.string.match_complete_title)
        matchRecordId = intent.getLongExtra(MATCH_RECORD_ID, 0)
        loadUI()
    }

    private fun loadUI() {
        lifecycleScope.launch {
            withContext(ioDispatcher) {
                matchRecord = viewModel.getMatchRecord(matchRecordId)
                basicDetailsTeam1 = matchRecord.toBasicMatchUIDetails(true)
                basicDetailsTeam2 = matchRecord.toBasicMatchUIDetails(false)
                withContext(mainDispatcher) {
                    loadWinningTeamDetails()
                }
            }
        }
    }

    private fun loadWinningTeamDetails() {
        when (matchRecord.status) {
            MatchStatus.TEAM_1_WON -> {
                binding.toggleMatchDetail.check(binding.btnMatchDetailTeam1.id)
            }

            MatchStatus.TEAM_2_WON -> {
                binding.toggleMatchDetail.check(binding.btnMatchDetailTeam2.id)
            }

            MatchStatus.DRAW -> {
                if (!matchRecord.isTeam1BattingFirst) {
                    binding.toggleMatchDetail.check(binding.btnMatchDetailTeam1.id)
                } else {
                    binding.toggleMatchDetail.check(binding.btnMatchDetailTeam2.id)
                }
            }

            else -> {
                binding.toggleMatchDetail.check(binding.btnMatchDetailTeam1.id)
            }
        }
    }

    override fun getToolBar() = binding.toolbar

    override fun createBinding(savedInstanceState: Bundle?) =
        ActivityMatchCompleteBinding.inflate(layoutInflater)

    override fun setListeners(isEnabled: Boolean) {
        if (isEnabled) {
            binding.toggleMatchDetail.addOnButtonCheckedListener(buttonCheckedListener)
        } else {
            binding.toggleMatchDetail.removeOnButtonCheckedListener(buttonCheckedListener)
        }
    }
}