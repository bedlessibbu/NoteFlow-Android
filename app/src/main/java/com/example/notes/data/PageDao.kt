package com.example.notes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PageDao {
    @Query("SELECT * FROM pages WHERE notebookId = :notebookId ORDER BY index ASC")
    fun getPagesForNotebook(notebookId: String): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE notebookId = :notebookId ORDER BY index ASC")
    suspend fun getPagesForNotebookBlocking(notebookId: String): List<PageEntity>

    @Insert
    suspend fun insert(page: PageEntity)

    @Delete
    suspend fun delete(page: PageEntity)

    @Query("DELETE FROM pages WHERE id = :id")
    suspend fun delete(id: String)
}
