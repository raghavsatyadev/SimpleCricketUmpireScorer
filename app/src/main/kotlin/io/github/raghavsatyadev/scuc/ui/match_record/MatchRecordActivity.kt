package io.github.raghavsatyadev.scuc.ui.match_record

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.raghavsatyadev.scuc.R
import io.github.raghavsatyadev.scuc.databinding.ActivityMatchRecordBinding
import io.github.raghavsatyadev.scuc.databinding.DialogEditTotalOversBinding
import io.github.raghavsatyadev.scuc.ui.match_complete.MatchCompleteActivity
import io.github.raghavsatyadev.support.core.CoreActivity
import io.github.raghavsatyadev.support.extensions.MenuExtensions.setupOptionsMenus
import io.github.raghavsatyadev.support.extensions.OrientationExtensions.enableFullScreen
import io.github.raghavsatyadev.support.extensions.ParcelSerialExtensions.getParcelExtra
import io.github.raghavsatyadev.support.extensions.ViewExtensions.gone
import io.github.raghavsatyadev.support.extensions.ViewExtensions.visible
import io.github.raghavsatyadev.support.models.BasicMatchUIDetails
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.isMatchCompleted
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.toBasicMatchUIDetails
import io.github.raghavsatyadev.support.models.essential.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MatchRecordActivity : CoreActivity<ActivityMatchRecordBinding>() {
    private val viewModel: MatchRecordViewModel by viewModels()
    private var matchRecordID: Long = 0

    companion object {
        private const val MATCH_RECORD = "match_record"

        fun getIntentObject(
            context: Context,
            record: MatchRecord,
        ): Intent = Intent(context, MatchRecordActivity::class.java).apply {
            putExtra(MATCH_RECORD, record)
        }
    }


    override fun createReference(savedInstanceState: Bundle?) {
        enableFullScreen()

        setToolBarTitle(R.string.match_record_title)

        resolveIntentForMatchRecord(intent)

        setupOptionsMenus(R.menu.match_record, onMenuItemClickListener = {
            when (it.itemId) {
                R.id.action_reset -> {
                    showResetDialog()
                }

                else -> return@setupOptionsMenus false
            }
        }, menuPrepareListener = {

        })
    }

    private fun showResetDialog(): Boolean {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.reset_match)
            .setMessage(R.string.reset_match_message)
            .setPositiveButton(R.string.reset_inning) { _, _ ->
                viewModel.reset(matchRecordID)
            }
            .setNeutralButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(R.string.reset_full) { _, _ ->
                viewModel.reset(matchRecordID, true)
            }
            .show()
        return true
    }

    override fun getToolBar() = binding.toolbar

    private fun resolveIntentForMatchRecord(intent: Intent) {
        val matchRecord = intent.getParcelExtra(MATCH_RECORD, MatchRecord::class)
        matchRecordID = matchRecord?.id ?: 0
        if (matchRecord != null && matchRecordID != 0L) {
            lifecycleScope.launch {
                showMatchRecord(matchRecord.toBasicMatchUIDetails())
                withContext(ioDispatcher) {
                    viewModel.getMatchRecord(matchRecord)
                    loadMatchRecord()
                }
            }
        } else {
            finish()
        }
    }

    private suspend fun loadMatchRecord() {
        viewModel.getMatchRecordEvent().collectLatest { value ->
            when (value.status) {
                Resource.Status.SUCCESS -> {
                    val record = value.data ?: return@collectLatest
                    showMatchRecord(record)
                }

                else -> {
                    // Handle error
                }
            }
        }
    }

    private suspend fun showMatchRecord(record: BasicMatchUIDetails) {
        withContext(mainDispatcher) {
            with(record) {
                if (record.isMatchCompleted()) {
                    finish()
                    startMatchCompleteActivity()
                } else {
                    setToolBarTitle("${getString(R.string.team)} $currentTeamName")
                    if (record.isFirstInningComplete) {
                        binding.txtRrr.text = getString(R.string.rrr, currentRRR)
                        binding.txtRequiredRunsBalls.text =
                            getString(R.string.required_runs_balls, requiredRunsBalls)
                        binding.groupSecondInning.visible()
                        binding.groupFirstInning.gone()
                    } else {
                        binding.groupFirstInning.visible()
                        binding.groupSecondInning.gone()
                    }

                    binding.txtRunsWickets.text = currentRunsAndWickets
                    binding.txtOvers.text = currentOvers
                    binding.txtCrr.text = getString(R.string.crr, currentCRR)
                }
            }
        }
    }

    private fun startMatchCompleteActivity() {
        startActivity(MatchCompleteActivity.getIntentObject(this, matchRecordID))
        finish()
    }

    override fun createBinding(savedInstanceState: Bundle?) =
        ActivityMatchRecordBinding.inflate(layoutInflater)

    override fun setListeners(isEnabled: Boolean) {
        if (isEnabled) {
            binding.btnAddRun.setOnClickListener {
                viewModel.setRun(matchRecordID, 1)
            }
            binding.btnAddWicket.setOnClickListener {
                viewModel.setWicket(matchRecordID)
            }
            binding.btnAddBall.setOnClickListener {
                viewModel.setBall(matchRecordID, 1)
            }
            binding.btnEndInning.setOnClickListener {
                viewModel.endInning(matchRecordID)
            }
            binding.btnMinusBall.setOnClickListener {
                viewModel.setBall(matchRecordID, 1, false)
            }
            binding.btnMinusWicket.setOnClickListener {
                viewModel.setWicket(matchRecordID, false)
            }
            binding.btnMinusRun.setOnClickListener {
                viewModel.setRun(matchRecordID, 1, false)
            }
            binding.btnEndMatch.setOnClickListener {
                viewModel.endMatch(matchRecordID)
            }
            binding.btnEditOvers.setOnClickListener {
                showEditOversDialog()
            }
        } else {
            binding.btnAddRun.setOnClickListener(null)
            binding.btnAddWicket.setOnClickListener(null)
            binding.btnAddBall.setOnClickListener(null)
            binding.btnEndInning.setOnClickListener(null)
            binding.btnMinusBall.setOnClickListener(null)
            binding.btnMinusWicket.setOnClickListener(null)
            binding.btnMinusRun.setOnClickListener(null)
            binding.btnEndMatch.setOnClickListener(null)
            binding.btnEditOvers.setOnClickListener(null)
        }
    }

    private fun showEditOversDialog() {
        // make the binding of dialog view
        val dialogBinding = DialogEditTotalOversBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.edit_total_overs)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { dialog, _ ->
                val editedOvers = dialogBinding.edTotalOvers.text.toString().toInt()
                lifecycleScope.launch {
                    viewModel.editTotalOvers(matchRecordID, editedOvers)
                }
                dialog.dismiss()

            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
        dialogBinding.edTotalOvers.setText(extractTotalOvers(binding.txtOvers.text.toString()))
    }

    private fun extractTotalOvers(overString: String): String {
        return overString.split("/")[1].toDouble().toInt().toString()
    }
}