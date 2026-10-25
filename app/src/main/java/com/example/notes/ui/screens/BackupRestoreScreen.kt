package com.example.notes.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notes.export.ExportImportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val exportImportManager: ExportImportManager
) : androidx.lifecycle.ViewModel() {
    fun export(context: android.content.Context, file: java.io.File): Boolean {
        return exportImportManager.exportNotebooks(context, file)
    }

    fun importBackup(context: android.content.Context, file: java.io.File): Boolean {
        return exportImportManager.importNotebooks(context, file)
    }
}

@Composable
fun BackupRestoreScreen(
    onBack: () -> Unit
) {
    val vm: BackupRestoreViewModel = hiltViewModel()
    var statusText by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(ArrowBack, "Back")
                    }
                }
            )
        },
        content = {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Button(
                    onClick = {
                        // In a real app, use ActivityResultContracts.OpenDocument/CreateDocument
                        // For now, just show placeholder
                        statusText = "Export functionality requires file picker integration"
                    },
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text("Export All Notebooks")
                }
                Button(
                    onClick = {
                        statusText = "Import functionality requires file picker integration"
                    },
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text("Import Notebooks")
                }
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.size(16.dp))
                Text(statusText, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(8.dp))
            }
        }
    )
}
