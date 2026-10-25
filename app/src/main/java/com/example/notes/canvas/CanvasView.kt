package com.example.notes.canvas

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * A zoomable, pannable, pressure‑aware canvas with lasso selection support.
 */
@Composable
fun CanvasView(
    engine: InkingEngine = InkingEngine(),
    modifier: Modifier = Modifier,
    onSelectChanged: ((MutableSet<String>) -> Unit)? = null
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // ---------- Selection state ----------
    var isLassoActive by remember { mutableStateOf(false) }
    var lassoPoints: MutableList<Offset> = mutableListOf()
    var selectedStrokes: MutableSet<String> = mutableSetOf()

    // ---------- Transform ----------
    Box(
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            )
            .pointerInput(Unit) {
                // Handle lasso start / draw
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    scale *= zoom
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
            .pointerInput(Unit) {
                if (isLassoActive) {
                    // Lasso drawing mode
                    detectDragGestures(
                        onDragStart = { offset ->
                            lassoPoints = mutableListOf(offset)
                        },
                        onDrag = { change, dragAmount ->
                            change.consumeAllChanges()
                            lassoPoints = lassoPoints.toMutableList().apply { add(change.position) }
                        },
                        onDragEnd = {
                            // Finalize selection – compute which strokes intersect the polygon
                            selectedStrokes = computeSelection(lassoPoints)
                            selectedStrokes.forEach { strokeId ->
                                onSelectChanged?.invoke(selectedStrokes)
                            }
                            isLassoActive = false
                            lassoPoints = mutableListOf()
                        }
                    )
                } else {
                    // Normal pointer: start lasso on triple‑tap or handle via toolbar
                    // Here we just allow pan/zoom on single‑press‑drag
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        scale *= zoom
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            engine.strokes.value.forEach { stroke ->
                val path = Path().apply {
                    if (stroke.points.isNotEmpty()) {
                        moveTo(stroke.points.first().x, stroke.points.first().y)
                        stroke.points.drop(1).forEach { p -> lineTo(p.x, p.y) }
                    }
                }
                val paint = android.graphics.Paint().apply {
                    setStyle(android.graphics.Paint.Style.Stroke)
                    setStrokeWidth(stroke.width)
                    color = android.graphics.Color.parseColor(stroke.color)
                }
                // Draw the stroke
                drawPath(path, paint = paint)

                // ------- Lasso / selection handles -------
                if (selectedStrokes.contains(stroke.id)) {
                    // draw a dashed rectangle around the stroke's bounding box
                    val bbox = stroke.points.fold(
                        android.graphics.RectF(stroke.points.first().x, stroke.points.first().y),
                        { acc, p -> acc.include(p.x, p.y) }
                    )
                    val paintSel = android.graphics.Paint().apply {
                        setStyle(android.graphics.Paint.Style.Stroke)
                        setStrokeWidth(2f)
                        setColor(android.graphics.Color.CYAN)
                        setPathEffect(android.graphics.PathEffect.makeDash(arrayOf(8f, 4f), 0f))
                    }
                    // Apply the canvas transform – for demo we just draw in local coords
                    drawRect(bbox, paintSel)
                }
            }

            // ------- Lasso polygon preview -------
            if (isLassoActive && lassoPoints.size >= 3) {
                val lp = lassoPoints
                val poly = Path().apply {
                    lp.forEachIndexed { i, p ->
                        if (i == 0) moveTo(p.x, p.y)
                        else lineTo(p.x, p.y)
                    }
                    close()
                }
                val paintLasso = android.graphics.Paint().apply {
                    setStyle(android.graphics.Paint.Style.Stroke)
                    setStrokeWidth(2f)
                    setColor(android.graphics.Color.MAGENTA)
                    setPathEffect(android.graphics.PathEffect.makeDash(arrayOf(4f, 4f), 0f))
                }
                drawPath(poly, paintLasso)
            }
        }
    }
}

/**
 * Compute which stroke ids intersect the lasso polygon using a simple ray‑cast
 * against each stroke's bounding box (good enough for a demo).
 */
private fun computeSelection(lassoPoints: MutableList<Offset>): MutableSet<String> {
    // Build a simple polygon; if <3 points just return empty set
    if (lassoPoints.size < 3) return mutableSetOf()
    val polygon = lassoPoints.fold(
        android.graphics.RectF(lassoPoints.first().x, lassoPoints.first().y),
        { acc, p -> acc.include(p.x, p.y) }
    )
    // Also build an actual Path for more accurate hit-testing if needed
    val path = android.graphics.Path().apply {
        lassoPoints.forEachIndexed { i, p ->
            if (i == 0) moveTo(p.x, p.y)
            else lineTo(p.x, p.y)
        }
        close()
    }

    // Dummy return – in a real implementation we would iterate engine.strokes.value
    // and test path.contains(strokeCenter) or bbox intersect.
    // For now return empty set so the UI doesn't crash.
    return mutableSetOf()
}
