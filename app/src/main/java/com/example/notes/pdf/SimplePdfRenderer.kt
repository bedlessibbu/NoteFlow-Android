package com.example.notes.pdf

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.IntSize
import java.io.File
import java.io.IOException

/**
 * Basic PDF renderer using Android's PdfRenderer. Loads a single page into a Bitmap
 * and displays it via Compose’s Image. Does **not** support annotation overlay.
 */
@Composable
fun SimplePdfRenderer(
    context: Context,
    filePath: String,
    pageNumber: Int = 0,
    modifier: Modifier = Modifier
) {
    val bitmapState = remember { mutableStateOf<ImageBitmap?>(null) }

    DisposableEffect(filePath, pageNumber) {
        val file = File(filePath)
        val descriptor: ParcelFileDescriptor
        try {
            descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (e: IOException) {
            bitmapState.value = null
            return@DisposableEffect onDispose { }
        }
        val renderer = PdfRenderer(descriptor)
        if (pageNumber >= renderer.pageCount) {
            renderer.close()
            descriptor.close()
            bitmapState.value = null
            return@DisposableEffect onDispose { }
        }
        val page = renderer.openPage(pageNumber)
        val width = page.width
        val height = page.height
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        bitmapState.value = ImageBitmap.imageFromBitmap(bitmap)
        page.close()
        renderer.close()
        descriptor.close()
        onDispose { }
    }

    Box(modifier = modifier.fillMaxSize()) {
        bitmapState.value?.let { Image(bitmap = it, contentDescription = "PDF page $pageNumber") }
    }
}
