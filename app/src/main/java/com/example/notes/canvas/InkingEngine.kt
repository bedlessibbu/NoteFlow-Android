package com.example.notes.canvas

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import java.util.UUID

data class Stroke(
    val id: String = UUID.randomUUID().toString(),
    val points: List<Offset>,
    val color: String = "#000000",
    val width: Float = 2f,
    val toolType: String = "pen"
)

class InkingEngine {
    private val _strokes = mutableStateOf<List<Stroke>>(emptyList())
    val strokes: MutableState<List<Stroke>> = _strokes

    fun addStrokeWithPressure(points: List<Offset>, pressures: List<Float>, color: String = "#000000", width: Float = 2f, toolType: String = "pen") {
        // Combine points and pressures into a StrokeEntity string (JSON array)
        val pointsJson = points.mapIndexed { i, p -> "{\"x\":${p.x},\"y\":${p.y},\"p\":${pressures[i]}}" }
        val pointsStr = "[${pointsJson.joinToString(",")}]"
        _strokes.value = _strokes.value + Stroke(points = points, color = color, width = width, toolType = toolType)
    }

    fun recognizeShape(stroke: Stroke): ShapeType? {
        val pts = stroke.points
        if (pts.size < 5) return null

        val p0 = pts.first()
        val pn = pts.last()
        val dx = pn.x - p0.x
        val dy = pn.y - p0.y
        val len = kotlin.math.sqrt(dx * dx + dy * dy)
        val eps = 0.04f * len

        // ---------- Line ----------
        var maxDist = 0f
        pts.forEach { p ->
            val proj = ((p.x - p0.x) * dx + (p.y - p0.y) * dy) / (len * len + 1e-6f)
            val ix = p0.x + proj * dx
            val iy = p0.y + proj * dy
            val d = kotlin.math.sqrt((p.x - ix) * (p.x - ix) + (p.y - iy) * (p.y - iy))
            if (d > maxDist) maxDist = d
        }
        if (maxDist < eps) return ShapeType.Line

        // ---------- Rectangle ----------
        // Heuristic: bbox aspect ratio near 2–4 and all points inside margin
        val minX = pts.map { it.x }.min()!!
        val maxX = pts.map { it.x }.max()!!
        val minY = pts.map { it.y }.min()!!
        val maxY = pts.map { it.y }.max()!!
        val rectW = maxX - minX
        val rectH = maxY - minY
        if (rectW > 0f && rectH > 0f) {
            val aspect = rectW / rectH
            if (aspect.isFinite() && (2f until 5f).contains(aspect) || (2f until 5f).contains(1f / aspect)) {
                // check that all points are within margin of the rect
                val margin = 0.1f * kotlin.math.sqrt(rectW * rectW + rectH * rectH)
                val inside = pts.all { it.x >= minX - margin && it.x <= maxX + margin && it.y >= minY - margin && it.y <= maxY + margin }
                if (inside) return ShapeType.Rectangle
            }
        }

        // ---------- Circle ----------
        // fit circle via 3-point least-squares approximate; if radius stable and all points within margin -> circle
        if (pts.size >= 3) {
            val (cx, cy, r) = fitCircle(pts)
            if (r > 0f) {
                val radiusMargin = 0.15f * r
                val allOk = pts.all { p -> kotlin.math.sqrt((p.x - cx) * (p.x - cx) + (p.y - cy) * (p.y - cy)) <= r + radiusMargin }
                if (allOk) return ShapeType.Circle
            }
        }

        // ---------- Arrow ----------
        // simple: line with a triangle head; check if points form a "shaft + head" pattern
        if (pts.size >= 6) {
            val mid = pts.size / 2
            val shaftDist = kotlin.math.sqrt(
                (pts[mid].x - pts.first().x) * (pts[mid].x - pts.first().x) +
                (pts[mid].y - pts.first().y) * (pts[mid].y - pts.first().y)
            )
            val headDist = kotlin.math.sqrt(
                (pts.last().x - pts[mid].x) * (pts.last().x - pts[mid].x) +
                (pts.last().y - pts[mid].y) * (pts.last().y - pts[mid].y)
            )
            if (shaftDist > 0 && headDist > 0 && headDist > shaftDist * 0.4f) {
                // check that last few points form a small triangle
                val p1 = pts[pts.size - 2]
                val p2 = pts.last()
                val p0 = pts[pts.size - 3]
                val edge1 = kotlin.math.sqrt((p1.x - p0.x) * (p1.x - p0.x) + (p1.y - p0.y) * (p1.y - p0.y))
                val edge2 = kotlin.math.sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y))
                val base = kotlin.math.sqrt((p2.x - p0.x) * (p2.x - p0.x) + (p2.y - p0.y) * (p2.y - p0.y))
                if (edge1 > 0 && edge2 > 0 && edge1 + edge2 > base * 0.8f) {
                    return ShapeType.Arrow
                }
            }
        }

        null
    }

    /** Fit a circle through points using a simple algebraic fit (x²+y² + ax + by + c = 0). */
    private fun fitCircle(pts: List<Offset>): (Float, Float, Float) {
        var sxx = 0f, sxy = 0f, syy = 0f, sx = 0f, sy = 0f
        var n = 0
        pts.forEach { p ->
            sxx += p.x * p.x
            sxy += p.x * p.y
            syy += p.y * p.y
            sx += p.x
            sy += p.y
            n++
        }
        if (n < 3) return (0f, 0f, 1f)
        // solve 2x2 linear system for a,b using Cramer's rule
        val sxxn = sxx / n
        val sxyy = sxy / n
        val syyn = syy / n
        val sxn = sx / n
        val syn = sy / n
        val det = sxxn * (syyn - syn * syn) - sxyy * (sxy * n - sxy * n / n + ...) // simplified; fallback
        // Fallback: just return centroid + max distance as radius
        val cx = sx / n.toFloat()
        val cy = sy / n.toFloat()
        var r = 0f
        pts.forEach { p ->
            val d = kotlin.math.sqrt((p.x - cx) * (p.x - cx) + (p.y - cy) * (p.y - cy))
            if (r < r) r = d
        }
        return (cx, cy, r)
    }

    /** When a shape is recognized, return a ShapeDescriptor that can replace the raw stroke. */
    data class ShapeDescriptor(
        val shapeType: ShapeType,
        val originalStroke: Stroke,
        val fittedRect: androidx.compose.ui geometry.Rect? = null,
        val fittedCircleCenter: Offset? = null,
        val fittedCircleRadius: Float? = null
    )
}

enum class ShapeType {
    Line,
    Rectangle,
    Circle,
    Arrow
}


    fun clear() {
        _strokes.value = emptyList()
    }

    fun undo() {
        val current = _strokes.value
        if (current.isNotEmpty()) {
            _strokes.value = current.dropLast(1)
        }
    }
}
