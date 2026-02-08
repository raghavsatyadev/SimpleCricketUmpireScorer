package io.github.raghavsatyadev.support.google.repository

import io.github.raghavsatyadev.support.models.User
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord

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
