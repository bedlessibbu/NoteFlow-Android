package com.example.notes.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "layers")
data class LayerEntity(
    @PrimaryKey val id: String,
    val pageId: String,
    val index: Int,
    val type: String,
    val visible: Boolean = true,
    val locked: Boolean = false,
    val createdAt: Instant = Instant.now()
)
