package com.example.notes.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.rx.preferencesDataStore
import androidx.datastore.rx.RxDataStore
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @androidx.hilt.android.qualifiers.ApplicationContext private val context: Context
) {
    private val dataStore: RxDataStore<Preferences> = preferencesDataStore(name = "settings")

    companion object {
        private const val DARK_MODE_KEY = "dark_mode_enabled"
        val DARK_MODE_KEY_PREF = booleanPreferencesKey(DARK_MODE_KEY)
    }

    fun getDarkMode(): Flowable<Boolean> {
        return dataStore.data
            .map { prefs -> prefs[DARK_MODE_KEY_PREF] ?: false }
            .distinctUntilChanged()
    }

    fun setDarkMode(enabled: Boolean): Single<Preferences> {
        return dataStore.updateDataAsync { prefs ->
            prefs.edit { it[DARK_MODE_KEY_PREF] = enabled }
        }
    }
}
