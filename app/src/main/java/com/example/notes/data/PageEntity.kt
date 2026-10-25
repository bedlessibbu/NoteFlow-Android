package com.example.notes.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "pages")
data class PageEntity(
    @PrimaryKey val id: String,
    val notebookId: String,
    val index: Int,
    val createdAt: Instant = Instant.now()
)
