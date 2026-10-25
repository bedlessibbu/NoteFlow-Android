package com.example.notes.repository

import com.example.notes.data.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface NotebookRepository {
    fun getAll(): Flow<List<NotebookEntity>>
    suspend fun create(title: String): String // returns id
    suspend fun delete(id: String)
}

class NotebookRepositoryImpl @Inject constructor(
    private val dao: NotebookDao
) : NotebookRepository {
    override fun getAll(): Flow<List<NotebookEntity>> = dao.getAll()
    override suspend fun create(title: String): String {
        val id = UUID.randomUUID().toString()
        dao.insert(NotebookEntity(id, title))
        return id
    }
    override suspend fun delete(id: String) = dao.delete(id)
}
