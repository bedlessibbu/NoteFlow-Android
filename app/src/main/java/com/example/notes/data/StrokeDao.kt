package com.example.notes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StrokeDao {
    @Query("SELECT * FROM strokes WHERE layerId = :layerId ORDER BY createdAt ASC")
    fun getStrokesForLayer(layerId: String): Flow<List<StrokeEntity>>

    @Query("SELECT * FROM strokes WHERE layerId = :layerId ORDER BY createdAt ASC")
    suspend fun getStrokesForLayerBlocking(layerId: String): List<StrokeEntity>

    @Insert
    suspend fun insert(stroke: StrokeEntity)
}
