package io.github.raghavsatyadev.library.support.database

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Update

interface BaseDao<T> {
  @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertReplace(t: T): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertReplace(ts: List<T>): List<Long>

  @Insert(onConflict = OnConflictStrategy.IGNORE) fun insertIgnore(t: T): Long

  @Insert(onConflict = OnConflictStrategy.IGNORE) fun insertIgnore(ts: List<T>): List<Long>

  @Update fun update(t: T): Int

  @Update fun update(t: List<T>): Int

  @Delete fun delete(t: T): Int

  @Delete fun delete(t: List<T>): Int

  @RawQuery fun delete(query: RoomRawQuery): Int

  @RawQuery fun getAll(query: RoomRawQuery): List<T>

  @RawQuery fun getItem(query: RoomRawQuery): T

  @RawQuery fun getCount(supportSQLiteQuery: RoomRawQuery): Long
}
