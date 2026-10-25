package com.example.notes.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "strokes")
data class StrokeEntity(
    @PrimaryKey val id: String,
    val layerId: String,
    val toolType: String,
    val points: String, // JSON array of {x,y} points
    val color: String,
    val width: Float,
    val pressure: Float,
    val createdAt: Instant = Instant.now()
)
