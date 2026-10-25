package com.example.notes.ui.screens

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.data.NotebookEntity
import com.example.notes.repository.NotebookRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val notebookRepository: NotebookRepository
) : ViewModel() {
    val notebooks: Flow<List<NotebookEntity>> = notebookRepository.getAll()

    fun createNotebook(title: String) {
        viewModelScope.launch {
            notebookRepository.create(title)
        }
    }
}
