package com.example.notes.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.Instant

@Entity(tableName = "notebooks")
@TypeConverters(Converters::class)
data class NotebookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
