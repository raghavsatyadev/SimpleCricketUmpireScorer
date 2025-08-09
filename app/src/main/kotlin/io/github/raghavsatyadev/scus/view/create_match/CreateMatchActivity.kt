package io.github.raghavsatyadev.scus.view.create_match

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.raghavsatyadev.scus.BuildConfig
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.scus.databinding.ActivityCreateMatchBinding
import io.github.raghavsatyadev.scus.view.match_record.MatchRecordActivity
import io.github.raghavsatyadev.support.core.CoreActivity
import io.github.raghavsatyadev.support.extensions.DateExtensions.formatDateToMillis
import io.github.raghavsatyadev.support.extensions.DateExtensions.formatMillisToDate
import io.github.raghavsatyadev.support.extensions.ErrorShowExtensions.errorDialog
import io.github.raghavsatyadev.support.extensions.ParcelSerialExtensions.getParcelExtra
import io.github.raghavsatyadev.support.extensions.ViewExtensions.isEditable
import io.github.raghavsatyadev.support.extensions.ads.AdExtensions.loadAds
import io.github.raghavsatyadev.support.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.getOvers
import io.github.raghavsatyadev.support.models.essential.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Calendar

class CreateMatchActivity : CoreActivity<ActivityCreateMatchBinding>() {
  private val viewModel: CreateMatchViewModel by viewModels()
  private var currentUserId: String? = null
  private var matchRecord: MatchRecord? = null

  companion object {
    const val MATCH_RECORD = "match_record"

    fun getIntentObject(context: Context, bundle: Bundle = Bundle.EMPTY): Intent =
      Intent(context, CreateMatchActivity::class.java).apply { putExtras(bundle) }
  }

  override fun getToolBar() = binding.toolbar

  override fun getProgressBar() = binding.loader

  override fun createReference(savedInstanceState: Bundle?) {
    setToolBarTitle(R.string.create_match_title)

    loadAds(binding.adView)

    binding.edMatchDateTime.isEditable = false

    currentUserId = FirebaseAuthUtil.getInstance().currentUserId

    listenForStates()

    binding.edMatchDateTime.setText(Instant.now().toEpochMilli().formatMillisToDate())

    matchRecord = intent?.getParcelExtra(MATCH_RECORD, MatchRecord::class)

    if (matchRecord != null) {
      setCopiedValues(
        matchRecord!!.team1Detail.teamName,
        matchRecord!!.team2Detail.teamName,
        matchRecord!!.location,
        matchRecord!!.getOvers(),
      )
    } else if (BuildConfig.DEBUG) {
      setCopiedValues("Team 1", "Team 2", "KwikBox", "10")
    }
  }

  private fun setCopiedValues(
    team1Name: String,
    team2Name: String,
    location: String,
    over: String,
  ) {
    binding.edTeam1Name.setText(team1Name)
    binding.edTeam2Name.setText(team2Name)
    binding.edMatchLocation.setText(location)
    binding.edInningOver.setText(over)
  }

  private fun listenForStates() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.getCreateMatchRecordEvent().collectLatest {
          when (it.status) {
            Resource.Status.SUCCESS -> {
              hideProgressBar()
              val record = it.data ?: return@collectLatest
              startActivity(MatchRecordActivity.getIntentObject(this@CreateMatchActivity, record))
              finish()
            }

            Resource.Status.ERROR -> {
              hideProgressBar()
              errorDialog(it.error?.exception?.message.toString())
            }

            Resource.Status.EMPTY -> {}
            Resource.Status.LOADING -> {
              showProgressBar()
            }
          }
        }
      }
    }
  }

  private fun showDateTimePicker(text: String) {
    val calendar = Calendar.getInstance()
    val dateTime =
      if (text.isEmpty()) {
        System.currentTimeMillis()
      } else {
        text.formatDateToMillis()
      }
    calendar.timeInMillis = dateTime

    val datePickerDialog =
      DatePickerDialog(
        this,
        { _, year, month, dayOfMonth ->
          calendar[Calendar.YEAR] = year
          calendar[Calendar.MONTH] = month
          calendar[Calendar.DAY_OF_MONTH] = dayOfMonth

          val timePickerDialog =
            TimePickerDialog(
              this,
              { _, hourOfDay, minute ->
                calendar[Calendar.HOUR_OF_DAY] = hourOfDay
                calendar[Calendar.MINUTE] = minute
                calendar[Calendar.SECOND] = 0
                calendar[Calendar.MILLISECOND] = 0

                val selectedDateTime = calendar.timeInMillis
                binding.edMatchDateTime.setText(selectedDateTime.formatMillisToDate())
              },
              calendar[Calendar.HOUR_OF_DAY],
              calendar[Calendar.MINUTE],
              false, // Set to false to use 12-hour view
            )
          timePickerDialog.show()
        },
        calendar[Calendar.YEAR],
        calendar[Calendar.MONTH],
        calendar[Calendar.DAY_OF_MONTH],
      )
    datePickerDialog.show()
  }

  override fun createBinding(savedInstanceState: Bundle?) =
    ActivityCreateMatchBinding.inflate(layoutInflater)

  override fun setListeners(isEnabled: Boolean) {
    if (isEnabled) {
      binding.btnSave.setOnClickListener { startValidation() }
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
    val didTeam1WinToss = binding.btnTossTeam1.id == binding.toggleToss.checkedButtonId
    val batFirstTeam1 = binding.btnBatFirstTeam1.id == binding.toggleBatFirst.checkedButtonId

    try {
      isMatchDetailsValid =
        viewModel.validateMatchDetails(
          this,
          currentUserId,
          matchLocation,
          matchDateTime,
          inningOvers,
          team1Name,
          team2Name,
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
        batFirstTeam1,
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
        batFirstTeam1,
      )
    }
  }
}
