package com.example.notes.domain

import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class Notebook(
    val id: String,
    val title: String
)

data class Page(
    val id: String,
    val notebookId: String,
    val index: Int
)

fun createNotebook(title: String): Notebook = Notebook(id = UUID.randomUUID().toString(), title = title)
