package com.example.notes.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.rx.preferencesDataStore
import androidx.datastore.rx.RxDataStore
import com.google.common.util.concurrent.ListenableFuture
import io.reactivex.Flowable

object SettingsDataStore {
    private const val DARK_MODE_KEY = "dark_mode_enabled"

    val DARK_MODE_KEY_PREF = booleanPreferencesKey(DARK_MODE_KEY)

    @Suppress("UNUSED_PARAMETER")
    fun createDataStore(context: Context): RxDataStore<Preferences> {
        return preferencesDataStore(name = "settings", produceMigrations = { })
    }
}
