package io.github.raghavsatyadev.scus.view.create_match

import android.content.Context
import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.R
import io.github.raghavsatyadev.support.core.CoreViewModel
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.google.FireStoreUtil
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.TeamDetail
import io.github.raghavsatyadev.support.models.essential.CustomError
import io.github.raghavsatyadev.support.models.essential.ErrorCode
import io.github.raghavsatyadev.support.models.essential.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class CreateMatchViewModel : CoreViewModel() {
  private var createMatchRecordEvent: MutableStateFlow<Resource<MatchRecord>> =
    MutableStateFlow(Resource.empty())

  fun getCreateMatchRecordEvent() = createMatchRecordEvent.asStateFlow()

  @Throws(Exception::class)
  fun validateMatchDetails(
    context: Context,
    currentUserId: String?,
    matchLocation: String,
    matchDate: String,
    inningOver: String,
    team1Name: String,
    team2Name: String,
  ): Boolean {
    when {
      currentUserId.isNullOrEmpty() -> {
        throw Exception(context.getString(R.string.warning_please_login))
      }

      matchLocation.isEmpty() -> {
        throw Exception(context.getString(R.string.warning_match_location))
      }

      matchDate.isEmpty() -> {
        throw Exception(context.getString(R.string.warning_match_date_time))
      }

      inningOver.isEmpty() -> {
        throw Exception(context.getString(R.string.warning_overs))
      }

      team1Name.isEmpty() -> {
        throw Exception(context.getString(R.string.warning_team_1_name))
      }

      team2Name.isEmpty() -> {
        throw Exception(context.getString(R.string.warning_team_2_name))
      }

      else -> {
        return true
      }
    }
  }

  /**
   * Create match
   *
   * @param matchLocation
   * @param matchDateTime in milliseconds
   * @param inningOvers
   * @param team1Name
   * @param team2Name
   * @param didTeam1WinToss
   * @param batFirstTeam1
   */
  fun createMatch(
    context: Context,
    currentUserId: String,
    matchLocation: String,
    matchDateTime: Long,
    inningOvers: String,
    team1Name: String,
    team2Name: String,
    didTeam1WinToss: Boolean,
    batFirstTeam1: Boolean,
  ) {
    viewModelScope.launch {
      withContext(ioDispatcher) {
        createMatchRecordEvent.emit(Resource.loading())
        try {
          val matchRecord =
            MatchRecord(
              location = matchLocation,
              startDateTime = matchDateTime,
              ballsPerInning = inningOvers.toInt() * 6,
              team1Detail = TeamDetail(teamName = team1Name),
              team2Detail = TeamDetail(teamName = team2Name),
              didTeam1WonToss = didTeam1WinToss,
              isTeam1BattingFirst = batFirstTeam1,
              localUpdateDateTime = Date(),
              serverUpdateDateTime = Date(),
              matchAdminID = currentUserId,
            )
          FireStoreUtil.getInstance().createMatchRecord(matchRecord)

          createMatchRecordEvent.emit(Resource.success(matchRecord))
        } catch (e: Exception) {
          AppLog.loge(false, kotlinFileName, "createMatch", e, Exception())
          createMatchRecordEvent.emit(
            Resource.error(
              CustomError(
                ErrorCode.UNKNOWN_ERROR,
                Exception(context.getString(R.string.warning_unknown_error)),
              )
            )
          )
        }
      }
    }
  }
}
