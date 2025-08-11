package com.todo.a30daysofcamping

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.progressDataStore by preferencesDataStore(name = "progress")

object ProgressStore {
    private fun key(day: Int) = booleanPreferencesKey("day_${day}_checked")

    suspend fun setChecked(context: Context, day: Int, checked: Boolean) {
        context.progressDataStore.edit { it[key(day)] = checked }
    }

    fun allAsFlow(context: Context, days: IntRange): Flow<Map<Int, Boolean>> =
        context.progressDataStore.data.map { prefs ->
            days.associateWith { day -> prefs[key(day)] ?: false }
        }
}
