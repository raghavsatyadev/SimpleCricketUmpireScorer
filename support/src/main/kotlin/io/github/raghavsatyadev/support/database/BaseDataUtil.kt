@file:Suppress("unused")

package io.github.raghavsatyadev.support.database

import androidx.sqlite.db.SimpleSQLiteQuery

abstract class BaseDataUtil<T, D : BaseDao<T>> {
    abstract fun getDao(): D

    abstract fun getTableName(): String

    abstract fun getPrimaryKey(): String

    open fun insertReplace(ts: ArrayList<T>): List<Long> {
        return getDao().insertReplace(ts)
    }

    open fun insertReplace(ts: List<T>): List<Long> {
        return getDao().insertReplace(ts)
    }

    open fun insertReplace(t: T): Long {
        return getDao().insertReplace(t)
    }

    open fun insertIgnore(ts: ArrayList<T>): List<Long> {
        return getDao().insertIgnore(ts)
    }

    open fun insertIgnore(ts: List<T>): List<Long> {
        return getDao().insertIgnore(ts)
    }

    open fun insertIgnore(t: T): Long {
        return getDao().insertIgnore(t)
    }

    open fun update(t: T): Int {
        return getDao().update(t)
    }

    open fun update(ts: List<T>): Int {
        return getDao().update(ts)
    }

    open fun delete(id: Long): Int {
        return getDao()
            .delete(
                SimpleSQLiteQuery("DELETE FROM `${getTableName()}` WHERE `${getPrimaryKey()}` = `$id`")
            )
    }

    open fun delete(t: T): Int {
        return getDao().delete(t)
    }

    open fun delete(ts: List<T>): Int {
        return getDao().delete(ts)
    }

    open fun delete(primaryKeyId: String): Int {
        return getDao()
            .delete(
                SimpleSQLiteQuery(
                    "DELETE FROM `${getTableName()}` WHERE `${getPrimaryKey()}` LIKE '$primaryKeyId'"
                )
            )
    }

    open fun deleteAll(): Int {
        return getDao().delete(SimpleSQLiteQuery("DELETE FROM `${getTableName()}`"))
    }

    open fun replaceAll(ts: List<T>): List<Long> {
        deleteAll()
        return insertReplace(ts)
    }

    open fun replaceAll(ts: ArrayList<T>): List<Long> {
        deleteAll()
        return insertReplace(ts)
    }

    open fun getAll(sortKey: String = ""): ArrayList<T> {
        return ArrayList(getDao().getAll(SimpleSQLiteQuery(buildGetAllSortedQuery(sortKey))))
    }

    open fun getItem(id: String): T {
        return getDao().getItem(SimpleSQLiteQuery(buildGetItemQuery(id)))
    }

    fun buildGetItemQuery(id: String): String =
        "SELECT * FROM `${getTableName()}` WHERE `${getPrimaryKey()}` = '$id' LIMIT 1"

    open fun getItem(id: Long): T {
        return getDao().getItem(SimpleSQLiteQuery(buildGetItemQuery("$id")))
    }

    open fun getCount(): Long {
        return getDao().getCount(SimpleSQLiteQuery(buildGetCountQuery()))
    }

    fun buildGetAllSortedQuery(sortKey: String): String {
        val sortQuery = if (sortKey.isEmpty()) "" else " ORDER BY $sortKey"
        val query = "SELECT * FROM `${getTableName()}`$sortQuery"
        return query
    }

    fun buildGetCountQuery() = "SELECT COUNT(`${getPrimaryKey()}`) FROM `${getTableName()}`"
}
