package io.github.raghavsatyadev.library.support.google.repository

import io.github.raghavsatyadev.library.support.models.User
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord

interface FireStoreRepository {
  suspend fun initialize(checkUserToken: Boolean = false)

  suspend fun setUser(user: User): User

  suspend fun updateUserLoginTokens(): Boolean

  suspend fun signOutUser(): Boolean

  suspend fun getUser(id: String): User

  suspend fun createMatchRecord(matchRecord: MatchRecord): MatchRecord

  suspend fun updateMatchRecords(matchRecords: List<MatchRecord>): Boolean

  suspend fun updateMatchRecord(matchRecord: MatchRecord): Boolean

  suspend fun deleteMatchRecord(matchRecordId: String): Boolean
}
