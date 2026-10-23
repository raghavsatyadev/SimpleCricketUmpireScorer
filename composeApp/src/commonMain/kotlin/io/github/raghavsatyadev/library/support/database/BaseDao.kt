package io.github.raghavsatyadev.library.support.database

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Update

interface BaseDao<T> {
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertReplace(t: T): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReplace(ts: List<T>): List<Long>

  @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertIgnore(t: T): Long

  @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertIgnore(ts: List<T>): List<Long>

  @Update suspend fun update(t: T): Int

  @Update suspend fun update(t: List<T>): Int

  @Delete suspend fun delete(t: T): Int

  @Delete suspend fun delete(t: List<T>): Int

  @RawQuery suspend fun delete(query: RoomRawQuery): Int

  @RawQuery suspend fun getAll(query: RoomRawQuery): List<T>

  @RawQuery suspend fun getItem(query: RoomRawQuery): T

  @RawQuery suspend fun getCount(supportSQLiteQuery: RoomRawQuery): Long
}
