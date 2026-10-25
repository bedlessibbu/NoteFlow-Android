package com.example.notes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LayerDao {
    @Query("SELECT * FROM layers WHERE pageId = :pageId ORDER BY index ASC")
    fun getLayersForPage(pageId: String): Flow<List<LayerEntity>>

    @Query("SELECT * FROM layers WHERE pageId = :pageId ORDER BY index ASC")
    suspend fun getLayersForPageBlocking(pageId: String): List<LayerEntity>

    @Insert
    suspend fun insert(layer: LayerEntity)
}
