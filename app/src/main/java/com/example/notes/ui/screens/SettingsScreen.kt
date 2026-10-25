package com.example.notes.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notes.prefs.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository
) : androidx.lifecycle.ViewModel() {
    private var darkModeFlowable: Flowable<Boolean>? = null

    fun observeDarkMode(): Flowable<Boolean> {
        return settingsRepo.getDarkMode()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun setDarkMode(enabled: Boolean) {
        settingsRepo.setDarkMode(enabled).subscribe()
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val vm: SettingsViewModel = hiltViewModel()
    val darkModeEnabled by remember { mutableStateOf(false) }

    // Observe DataStore changes
    androidx.compose.runtime.LaunchedEffect(Unit) {
        vm.observeDarkMode()
            .subscribe { enabled ->
                darkModeEnabled = enabled
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(ArrowBack, "Back")
                    }
                }
            )
        },
        content = {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Dark Mode")
                    Switch(
                        checked = darkModeEnabled,
                        onCheckedChange = { enabled ->
                            darkModeEnabled = enabled
                            vm.setDarkMode(enabled)
                        }
                    )
                }
                Divider()
                Text("App Version: 1.0.0", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(8.dp))
            }
        }
    )
}
