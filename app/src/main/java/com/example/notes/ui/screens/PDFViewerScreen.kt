package com.example.notes.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
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
import com.example.notes.pdf.PdfAnnotationEngine
import com.example.notes.pdf.PdfViewerWithAnnotations
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PDFViewerViewModel @Inject constructor() : androidx.lifecycle.ViewModel() {
    val annotationEngine = PdfAnnotationEngine()
}

@Composable
fun PDFViewerScreen(
    filePath: String,
    onBack: () -> Unit
) {
    val viewModel: PDFViewerViewModel = hiltViewModel()
    var currentPage by remember { mutableStateOf(0) }
    var pageCount by remember { mutableStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "PDF Viewer - Page ${currentPage + 1}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { if (currentPage > 0) currentPage-- }, enabled = currentPage > 0) {
                        Icon(NavigateBefore, "Previous")
                    }
                    IconButton(onClick = { if (currentPage < pageCount - 1) currentPage++ }, enabled = currentPage < pageCount - 1) {
                        Icon(NavigateNext, "Next")
                    }
                }
            )
        },
        content = {
            PdfViewerWithAnnotations(
                context = it.context,
                filePath = filePath,
                pageIndex = currentPage,
                annotationEngine = viewModel.annotationEngine,
                onPageChange = { currentPage = it },
                onPreviousPage = { if (currentPage > 0) currentPage-- },
                onNextPage = { if (currentPage < pageCount - 1) currentPage++ },
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}
