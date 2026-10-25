package com.example.notes.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notes.data.NotebookEntity
import com.example.notes.repository.NotebookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: NotebookRepository
) : androidx.lifecycle.ViewModel() {
    val notebooks = repo.getAll()
    fun createNotebook(title: String) = repo.create(title)
}

@Composable
fun HomeScreen(
    onNotebookClick: (String) -> Unit,
    onBack: () -> Unit,
    onPageManagerClick: (String) -> Unit,
    onBackupRestoreClick: () -> Unit
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val notebooks by viewModel.notebooks.collectAsStateWithLifecycle(emptyList())
    var showNewNotebookDialog by remember { mutableStateOf(false) }
    var newNotebookTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "NoteFlow") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onBackupRestoreClick) {
                        Icon(Backup, "Backup")
                    }
                }
            )
        },
        content = {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                items(notebooks) { nb ->
                    NotebookItem(
                        notebook = nb,
                        onClick = { onNotebookClick(nb.id) },
                        onLongClick = { onPageManagerClick(nb.id) }
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewNotebookDialog = true },
                text = { Text("New Notebook") },
                icon = { Icon(Add, "Add") }
            )
        }
    )

    if (showNewNotebookDialog) {
        NewNotebookDialog(
            onDismiss = { showNewNotebookDialog = false; newNotebookTitle = "" },
            onConfirm = { title ->
                viewModel.createNotebook(title)
                showNewNotebookDialog = false
                newNotebookTitle = ""
            },
            title = newNotebookTitle,
            onTitleChange = { newNotebookTitle = it }
        )
    }
}

@Composable
private fun NotebookItem(
    notebook: NotebookEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        ListItem(
            headlineText = { Text(notebook.title) },
            trailingContent = { Icon(ExpandMore, "More") }
        )
    }
}

@Composable
private fun NewNotebookDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "New Notebook", style = MaterialTheme.typography.titleLarge) },
        text = {
            TextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Notebook title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onConfirm(title) }) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
