package com.example.notes.data

import androidx.annotation.Keep
import java.time.Instant

@Keep
class Converters {
    @androidx.room.TypeConverter
    fun fromInstant(value: Long): Instant = Instant.ofEpochMilli(value)
    @androidx.room.TypeConverter
    fun toInstant(instant: Instant): Long = instant.toEpochMilli()
}
