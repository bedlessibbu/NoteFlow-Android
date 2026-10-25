package com.example.notes.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notes.data.PageEntity
import com.example.notes.data.PageDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PageManagerViewModel @Inject constructor(
    private val pageDao: PageDao
) : androidx.lifecycle.ViewModel() {
    fun getPagesForNotebook(notebookId: String): Flow<List<PageEntity>> {
        return pageDao.getPagesForNotebook(notebookId)
    }

    suspend fun addPage(notebookId: String, index: Int) {
        val pageId = java.util.UUID.randomUUID().toString()
        pageDao.insert(PageEntity(pageId, notebookId, index))
    }

    suspend fun deletePage(pageId: String) {
        pageDao.delete(pageId)
    }
}

@Composable
fun PageManagerScreen(
    notebookId: String,
    onBack: () -> Unit
) {
    val viewModel: PageManagerViewModel = hiltViewModel()
    val pages by viewModel.getPagesForNotebook(notebookId).collectAsStateWithLifecycle(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Page Manager") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* add page */ }) {
                        Icon(Add, "Add Page")
                    }
                }
            )
        },
        content = {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                items(pages) { page ->
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Page ${page.index + 1}")
                            IconButton(onClick = { viewModel.deletePage(page.id) }) {
                                Icon(Delete, "Delete")
                            }
                        }
                    }
                }
            }
        }
    )
}
