package com.example.notes

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotesApplication : Application() {
    // Empty class – Hilt will generate components here.
}
