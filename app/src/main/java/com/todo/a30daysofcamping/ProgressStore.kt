package com.todo.a30daysofcamping

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "progress")

object ProgressStore {

    private fun keyFor(day: Int): Preferences.Key<Boolean> =
        booleanPreferencesKey("checked_day_$day")

    fun allAsFlow(context: Context, days: IntRange): Flow<Map<Int, Boolean>> {
        return context.dataStore.data.map { prefs ->
            buildMap {
                for (d in days) put(d, prefs[keyFor(d)] ?: false)
            }
        }
    }

    suspend fun setChecked(context: Context, day: Int, value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[keyFor(day)] = value
        }
    }
}
