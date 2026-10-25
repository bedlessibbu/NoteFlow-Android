package com.example.notes.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.example.notes.prefs.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.hilt.navigation.compose.hiltViewModel

private val LightColors = lightColorScheme(
    primary = Color(0xFF1A73E8),
    secondary = Color(0xFF8AB4F8),
    background = Color(0xFFF8F9FA),
    surface = Color.White,
    onBackground = Color(0xFF202124),
    onSurface = Color(0xFF202124),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    secondary = Color(0xFF1A73E8),
    background = Color(0xFF1A1C1E),
    surface = Color(0xFF242629),
    onBackground = Color(0xFFE8EAED),
    onSurface = Color(0xFFE8EAED),
)

@Composable
fun NoteFlowTheme(
    content: @Composable () -> Unit
) {
    // For now just use system theme until we wire up SettingsRepository properly
    androidx.compose.material3.MaterialTheme(
        colorScheme = if (androidx.compose.foundation.isSystemInDarkTheme()) DarkColors else LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
