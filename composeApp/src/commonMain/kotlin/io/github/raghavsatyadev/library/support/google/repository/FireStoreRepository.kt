package io.github.raghavsatyadev.library.support.google.repository

interface FireStoreRepository {
  suspend fun initialize(checkUserToken: Boolean = false)

  suspend fun setUser(
    user: io.github.raghavsatyadev.library.support.models.User
  ): io.github.raghavsatyadev.library.support.models.User

  suspend fun updateUserLoginTokens(): Boolean

  suspend fun signOutUser(): Boolean

  suspend fun getUser(id: String): io.github.raghavsatyadev.library.support.models.User

  suspend fun createMatchRecord(
    matchRecord: io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord
  ): io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord

  suspend fun updateMatchRecords(
    matchRecords: List<io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord>
  ): Boolean

  suspend fun updateMatchRecord(
    matchRecord: io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord
  ): Boolean

  suspend fun deleteMatchRecord(matchRecordId: String): Boolean
}
