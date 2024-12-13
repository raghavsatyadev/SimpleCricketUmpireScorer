package io.github.raghavsatyadev.scuc.ui.create_match

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import io.github.raghavsatyadev.scuc.R
import io.github.raghavsatyadev.scuc.databinding.ActivityCreateMatchBinding
import io.github.raghavsatyadev.scuc.ui.match_record.MatchRecordActivity
import io.github.raghavsatyadev.support.core.CoreActivity
import io.github.raghavsatyadev.support.extensions.DateExtensions.formatDateToMillis
import io.github.raghavsatyadev.support.extensions.DateExtensions.formatMillisToDate
import io.github.raghavsatyadev.support.extensions.ErrorShowExtensions.errorDialog
import io.github.raghavsatyadev.support.extensions.ViewExtensions.isEditable
import io.github.raghavsatyadev.support.extensions.ads.AdExtensions.loadAds
import io.github.raghavsatyadev.support.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.models.essential.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class CreateMatchActivity : CoreActivity<ActivityCreateMatchBinding>() {
    private val viewModel: CreateMatchViewModel by viewModels()
    private var currentUserId: String? = null

    companion object {
        fun getIntentObject(
            context: Context,
            bundle: Bundle = Bundle.EMPTY,
        ): Intent = Intent(context, CreateMatchActivity::class.java).apply { putExtras(bundle) }
    }

    override fun getToolBar() = binding.toolbar

    override fun createReference(savedInstanceState: Bundle?) {
        setToolBarTitle(R.string.create_match_title)

        loadAds(binding.adView)

        binding.edMatchDateTime.isEditable = false

        currentUserId = FirebaseAuthUtil.getInstance().currentUserId
    }

    private fun showDateTimePicker(text: String) {
        val calendar = Calendar.getInstance()
        val dateTime = if (text.isEmpty()) {
            System.currentTimeMillis()
        } else {
            text.formatDateToMillis()
        }
        calendar.timeInMillis = dateTime

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(
                    Calendar.YEAR,
                    year
                )
                calendar.set(
                    Calendar.MONTH,
                    month
                )
                calendar.set(
                    Calendar.DAY_OF_MONTH,
                    dayOfMonth
                )

                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(
                            Calendar.HOUR_OF_DAY,
                            hourOfDay
                        )
                        calendar.set(
                            Calendar.MINUTE,
                            minute
                        )
                        calendar.set(
                            Calendar.SECOND,
                            0
                        )
                        calendar.set(
                            Calendar.MILLISECOND,
                            0
                        )

                        val selectedDateTime = calendar.timeInMillis
                        binding.edMatchDateTime.setText(selectedDateTime.formatMillisToDate())
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false // Set to false to use 12-hour view
                )
                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    override fun createBinding(savedInstanceState: Bundle?) =
        ActivityCreateMatchBinding.inflate(layoutInflater)

    override fun setListeners(isEnabled: Boolean) {
        if (isEnabled) {
            binding.btnSave.setOnClickListener {
                startValidation()
            }
            binding.edMatchDateTime.setOnClickListener {
                showDateTimePicker(binding.edMatchDateTime.text.toString())
            }
            binding.boxMatchDateTime.setOnClickListener {
                showDateTimePicker(binding.edMatchDateTime.text.toString())
            }
        } else {
            binding.edMatchDateTime.setOnClickListener(null)
            binding.boxMatchDateTime.setOnClickListener(null)
            binding.btnSave.setOnClickListener(null)
        }
    }

    private fun startValidation() {
        val matchLocation = binding.edMatchLocation.text.toString()
        val matchDateTime = binding.edMatchDateTime.text.toString()
        val inningOvers = binding.edInningOver.text.toString()
        val team1Name = binding.edTeam1Name.text.toString()
        val team2Name = binding.edTeam2Name.text.toString()

        var isMatchDetailsValid = false
        var didTeam1WinToss = binding.btnTossTeam1.id == binding.toggleToss.checkedButtonId
        var batFirstTeam1 = binding.btnBatFirstTeam1.id == binding.toggleBatFirst.checkedButtonId

        try {
            isMatchDetailsValid = viewModel.validateMatchDetails(
                this,
                currentUserId,
                matchLocation,
                matchDateTime,
                inningOvers,
                team1Name,
                team2Name
            )
        } catch (e: Exception) {
            errorDialog(e.message.toString())
        }
        if (isMatchDetailsValid) {
            saveMatchRecord(
                currentUserId!!,
                matchLocation,
                matchDateTime,
                inningOvers,
                team1Name,
                team2Name,
                didTeam1WinToss,
                batFirstTeam1
            )
        }
    }

    private fun saveMatchRecord(
        currentUserId: String,
        matchLocation: String,
        matchDateTime: String,
        inningOvers: String,
        team1Name: String,
        team2Name: String,
        didTeam1WinToss: Boolean,
        batFirstTeam1: Boolean,
    ) {
        lifecycleScope.launch {
            viewModel.createMatch(
                this@CreateMatchActivity,
                currentUserId,
                matchLocation,
                matchDateTime.formatDateToMillis(),
                inningOvers,
                team1Name,
                team2Name,
                didTeam1WinToss,
                batFirstTeam1
            )
            viewModel.getCreateMatchRecordEvent().collectLatest {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        val record = it.data ?: return@collectLatest
                        startActivity(
                            MatchRecordActivity.getIntentObject(
                                this@CreateMatchActivity,
                                record
                            )
                        )
                        finish()
                    }

                    Resource.Status.ERROR -> {
                        errorDialog(it.error?.exception?.message.toString())
                    }

                    else -> {
                    }
                }
            }
        }
    }
}