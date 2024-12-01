@file:Suppress("unused")

package io.github.raghavsatyadev.support.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.Preferences.Key
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.raghavsatyadev.support.R
import io.github.raghavsatyadev.support.core.CoreApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object AppPrefsExtensions {
    private val APP_PREFS_NAME: String = CoreApp.instance.getString(R.string.app_name)

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(APP_PREFS_NAME)

    suspend inline fun <T> Context.savePref(key: String, value: T) {
        dataStore.edit {
            when (value) {
                is Boolean -> it[booleanPreferencesKey(key)] = value
                is Int -> it[intPreferencesKey(key)] = value
                is String -> it[stringPreferencesKey(key)] = value
                is Float -> it[floatPreferencesKey(key)] = value
                is Double -> it[doublePreferencesKey(key)] = value
                is Long -> it[longPreferencesKey(key)] = value
                else -> throw RuntimeException("Attempting to save non-supported preference")
            }
        }
    }

    inline fun <reified T> getKey(key: String): Key<*> {
        return when (T::class) {
            Boolean::class -> booleanPreferencesKey(key)
            Int::class -> intPreferencesKey(key)
            String::class -> stringPreferencesKey(key)
            Float::class -> floatPreferencesKey(key)
            Double::class -> doublePreferencesKey(key)
            Long::class -> longPreferencesKey(key)
            else -> throw RuntimeException("Attempting to save non-supported preference")
        }
    }

    suspend fun Context.deleteAllPrefs() {
        dataStore.edit { it.clear() }
    }

    suspend inline fun <reified T> Context.deletePref(key: String) {
        dataStore.edit {
            val keyType = getKey<T>(key)
            it.remove(keyType)
        }
    }

    inline fun <reified T> Context.getPrefs(
        key: String,
        defaultValue: T,
    ): Flow<T> {
        return dataStore.data.map {
            (it[getKey<T>(key)] ?: defaultValue) as T
        }
    }

    inline fun <reified T> Context.getPrefs(
        key: String,
    ): Flow<T?> {
        return dataStore.data.map {
            (it[getKey<T>(key)]) as T?
        }
    }
}
