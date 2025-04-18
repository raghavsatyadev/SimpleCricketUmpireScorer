package io.github.raghavsatyadev.support

object Constants {
  object Other {
    const val MEDIA_TYPE_JSON = "application/json; charset=utf-8"
  }

  object DB {
    const val NAME: String = "SCUS"
    const val VERSION = 1

    object Tables {
      const val MATCH_RECORD_TABLE = "match_record"
    }
  }

  object FieldKeys {
    // region Match Record
    const val MATCH_RECORD_ID = "match_record_id"
    const val START_DATE_TIME = "start_date_time"
    const val END_DATE_TIME = "end_date_time"
    const val TEAM_1 = "team_1"
    const val TEAM_2 = "team_2"
    const val BALLS_PER_INNING = "balls_per_inning"
    const val DID_TEAM_1_WON_TOSS = "did_team_1_won_toss"
    const val IS_TEAM_1_BATTING_FIRST = "is_team_1_batting_first"
    const val IS_FIRST_INNING_COMPLETE = "is_first_inning_complete"
    const val RRR_AT_SECOND_INNING_START = "rrr_at_second_inning_start"
    const val STATUS = "status"
    const val LOCATION = "location"
    const val MATCH_ADMIN_ID = "match_admin_id"
    const val MATCH_SHARED_USER_IDS = "match_shared_user_ids"
    const val LOCAL_UPDATE_DATE_TIME = "local_update_date_time"
    const val SERVER_UPDATE_DATE_TIME = "server_update_date_time"
    const val RUNS = "runs"
    const val WICKETS = "wickets"
    const val BALLS = "balls"
    // endregion

    const val TEAM_NAME = "team_name"

    // region User
    const val USER_ID = "user_id"
    const val LOGIN_TOKEN = "login_token"
    // endregion
  }

  object FirebaseConstants {
    object Collections {
      const val USER = "User"
      const val MATCH_RECORD = "Match"
    }
  }

  object NotificationKeys {
    const val MAIN_KEY = "main"
  }
}
