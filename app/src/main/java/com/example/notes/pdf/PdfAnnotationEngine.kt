package com.example.notes.pdf

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import java.util.UUID

/**
 * Represents a single annotation stroke on a PDF page.
 */
data class PdfAnnotation(
    val id: String = UUID.randomUUID().toString(),
    val pageIndex: Int,
    val points: List<Offset>,
    val color: String = "#000000",
    val width: Float = 2f,
    val toolType: String = "pen"
)

/**
 * Manages PDF annotations across multiple pages.
 */
class PdfAnnotationEngine {
    private val _annotations = mutableStateOf<MutableMap<Int, MutableList<PdfAnnotation>>>(
        mutableMapOf()
    )

    val annotations: MutableState<MutableMap<Int, MutableList<PdfAnnotation>>> = _annotations

    fun addAnnotation(pageIndex: Int, points: List<Offset>, color: String = "#000000", width: Float = 2f, toolType: String = "pen") {
        val pageAnnotations = _annotations.value.getOrPut(pageIndex) { mutableListOf() }
        val annotation = PdfAnnotation(pageIndex = pageIndex, points = points, color = color, width = width, toolType = toolType)
        pageAnnotations.add(annotation)
        // Trigger recomposition
        _annotations.value = _annotations.value.toMutableMap()
    }

    fun getAnnotationsForPage(pageIndex: Int): List<PdfAnnotation> {
        return _annotations.value[pageIndex]?.toList() ?: emptyList()
    }

    fun clearPage(pageIndex: Int) {
        _annotations.value.remove(pageIndex)
        _annotations.value = _annotations.value.toMutableMap()
    }

    fun undo(pageIndex: Int) {
        val pageAnnotations = _annotations.value[pageIndex]
        if (pageAnnotations != null && pageAnnotations.isNotEmpty()) {
            pageAnnotations.removeAt(pageAnnotations.lastIndex)
            if (pageAnnotations.isEmpty()) {
                _annotations.value.remove(pageIndex)
            }
            _annotations.value = _annotations.value.toMutableMap()
        }
    }

    fun clearAll() {
        _annotations.value.clear()
        _annotations.value = _annotations.value.toMutableMap()
    }
}
