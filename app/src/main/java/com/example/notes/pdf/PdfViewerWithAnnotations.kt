package com.example.notes.pdf

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FillViewport
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import java.io.File
import java.io.IOException

/**
 * A Pdf viewer that supports multi-page navigation, zoom/pan, thumbnails,
 * and persistent annotations per page.
 */
@Composable
fun PdfViewerWithAnnotations(
    context: Context,
    filePath: String,
    onPageChange: (Int) -> Unit,
    onViewportChange: (ScaleAndOffset) -> Unit = { },
    modifier: Modifier = Modifier
) {
    // ---------- PDF state ----------
    var currentPage by remember { mutableStateOf(0) }
    var pageCount by remember { mutableStateOf(1) }
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // thumbnails – one bitmap per page (lazy loaded)
    val thumbnails by remember { mutableStateOf<ArrayList<ImageBitmap?>>(emptyArrayList()) }

    // ---------- Load PDF once ----------
    DisposableEffect(filePath, currentPage) {
        val file = File(filePath)
        val descriptor: ParcelFileDescriptor
        try {
            descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (e: IOException) {
            pageCount = 1
            return@DisposableEffect onDispose { }
        }
        val renderer = PdfRenderer(descriptor)
        pageCount = renderer.pageCount
        // preload first thumbnail if not present
        if (thumbnails.isEmpty() || thumbnails.size < pageCount) {
            val thumbs = mutableListOf<ImageBitmap?>
            for (i in 0 until Math.min(renderer.pageCount, 5)) { // preload first 5
                try {
                    val page = renderer.openPage(i)
                    val w = page.width.toFloat()
                    val h = page.height.toFloat()
                    val b = android.graphics.Bitmap.createBitmap(w.toInt(), h.toInt(), android.graphics.Bitmap.Config.ARGB_8888)
                    page.render(b, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    thumbs.add(ImageBitmap.imageFromBitmap(b))
                    page.close()
                } catch (e: Exception) {
                    thumbs.add(null)
                }
            }
            thumbnails.value = thumbs
        }
        renderer.close()
        descriptor.close()
        onDispose { }
    }

    // ---------- Zoom / Pan ----------
    // pinch zoom + drag pan handled via graphicsLayer + pointer gestures
    Box(
        modifier = Modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            )
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    scale *= zoom
                    offsetX += pan.x
                    offsetY += pan.y
                    onViewportChange(ScaleAndOffset(scale, offsetX, offsetY))
                }
            }
            .pointerInput(Unit) {
                var currentPoints: MutableList<Offset> = mutableListOf()
                detectDragGestures(
                    onDragStart = { offset ->
                        currentPoints = mutableListOf(offset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consumeAllChanges()
                        currentPoints = currentPoints.toMutableList().apply { add(change.position) }
                        // add annotation for current page
                        annotationEngine.addAnnotation(currentPage, currentPoints)
                    },
                    onDragEnd = {
                        // stroke finalized – nothing extra needed
                    }
                )
            }
    ) {
        // ------- PDF page background -------
        pdfBitmapForPage(currentPage, pageCount, thumbnails).let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = "PDF page ${currentPage + 1}",
                modifier = Modifier.fillMaxSize()
            )
        }

        // ------- Annotation overlay -------
        Canvas(modifier = Modifier.fillMaxSize()) {
            annotationEngine.getAnnotationsForPage(currentPage).forEach { annotation ->
                val path = Path().apply {
                    if (annotation.points.isNotEmpty()) {
                        moveTo(annotation.points.first().x, annotation.points.first().y)
                        annotation.points.drop(1).forEach { p -> lineTo(p.x, p.y) }
                    }
                }
                drawPath(
                    path,
                    color = Color(android.graphics.Color.parseColor(annotation.color)),
                    style = Stroke(width = annotation.width)
                )
            }
        }
    }
}

/**
 * Helper: load bitmap for a given page, caching in the thumbnails list.
 */
private fun pdfBitmapForPage(
    pageIndex: Int,
    pageCount: Int,
    thumbnails: ArrayList<ImageBitmap?>,
    renderer: PdfRenderer
): ImageBitmap? {
    if (pageIndex < 0 || pageIndex >= pageCount) return null
    if (pageIndex < thumbnails.size && thumbnails[pageIndex] != null) return thumbnails[pageIndex]
    try {
        val page = renderer.openPage(pageIndex)
        val w = page.width.toFloat()
        val h = page.height.toFloat()
        val b = android.graphics.Bitmap.createBitmap(w.toInt(), h.toInt(), android.graphics.Bitmap.Config.ARGB_8888)
        page.render(b, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        val out = ImageBitmap.imageFromBitmap(b)
        // update cache
        if (pageIndex < thumbnails.size) thumbnails[pageIndex] = out
        page.close()
        return out
    } catch (e: Exception) {
        return null
    }
}

/** Simple data class to carry scale & offset for UI callbacks. */
private data class ScaleAndOffset(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float
)
