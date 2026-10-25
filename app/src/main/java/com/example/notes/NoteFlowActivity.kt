package com.example.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.notes.ui.AppNavHost
import com.example.notes.ui.theme.NoteFlowTheme

/**
 * Main entry point for the NoteFlow app.
 * Uses Jetpack Compose for all UI rendering.
 */
class NoteFlowActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteFlowTheme {
                AppNavHost()
            }
        }
    }
}
