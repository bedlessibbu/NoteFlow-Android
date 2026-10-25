package com.example.notes.data

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.PrimaryKey
import java.time.Instant

@DatabaseView("SELECT * FROM notebooks WHERE id = :id")
data class NotebookView(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Instant
)
