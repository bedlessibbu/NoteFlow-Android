package com.example.notes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notes.canvas.CanvasView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider

@Composable
fun NotebookScreen(notebookId: String, onBack: () -> Unit) {
    val viewModel: NotebookViewModel = hiltViewModel()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = "Notebook", color = MaterialTheme.colorScheme.onSurface) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* undo */ }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Undo,
                        contentDescription = "Undo"
                    )
                }
                IconButton(onClick = { /* redo */ }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Redo,
                        contentDescription = "Redo"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        // Toolbar row with tools
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { /* pen */ }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                    contentDescription = "Pen"
                )
            }
            IconButton(onClick = { /* highlighter */ }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Highlight,
                    contentDescription = "Highlighter"
                )
            }
            IconButton(onClick = { /* eraser */ }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                    contentDescription = "Eraser"
                )
            }
            IconButton(onClick = { /* shape */ }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.CropSquare,
                    contentDescription = "Shape"
                )
            }
            IconButton(onClick = { /* text */ }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.TextFields,
                    contentDescription = "Text"
                )
            }
            IconButton(onClick = { /* lasso */ }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.SelectAll,
                    contentDescription = "Lasso"
                )
            }
        }
        // Canvas area with zoom/pan
        CanvasView(modifier = Modifier.fillMaxSize())
    }
}
