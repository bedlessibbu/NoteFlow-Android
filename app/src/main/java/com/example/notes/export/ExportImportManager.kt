package com.example.notes.export

import android.content.Context
import androidx.room.RoomDatabase
import com.example.notes.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream

// Simple serialization models for export
@Serializable
data class ExportNotebook(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("createdAt") val createdAt: Long,
    @SerialName("updatedAt") val updatedAt: Long,
    @SerialName("pages") val pages: List<ExportPage>
)

@Serializable
data class ExportPage(
    @SerialName("id") val id: String,
    @SerialName("notebookId") val notebookId: String,
    @SerialName("index") val index: Int,
    @SerialName("layers") val layers: List<ExportLayer>
)

@Serializable
data class ExportLayer(
    @SerialName("id") val id: String,
    @SerialName("pageId") val pageId: String,
    @SerialName("index") val index: Int,
    @SerialName("type") val type: String,
    @SerialName("visible") val visible: Boolean,
    @SerialName("locked") val locked: Boolean,
    @SerialName("strokes") val strokes: List<ExportStroke>
)

@Serializable
data class ExportStroke(
    @SerialName("id") val id: String,
    @SerialName("layerId") val layerId: String,
    @SerialName("toolType") val toolType: String,
    @SerialName("points") val points: String,
    @SerialName("color") val color: String,
    @SerialName("width") val width: Float,
    @SerialName("pressure") val pressure: Float
)

class ExportImportManager @Inject constructor(
    private val db: RoomDatabase
) {
    private val json = Json { prettyPrint = true }

    fun exportNotebooks(context: Context, file: File): Boolean {
        return try {
            val notebooks = db.notebookDao().getAllBlocking()
            val exportData = notebooks.map { nb ->
                val pages = db.pageDao().getPagesForNotebookBlocking(nb.id)
                val exportPages = pages.map { page ->
                    val layers = db.layerDao().getLayersForPageBlocking(page.id)
                    val exportLayers = layers.map { layer ->
                        val strokes = db.strokeDao().getStrokesForLayerBlocking(layer.id)
                        val exportStrokes = strokes.map { stroke ->
                            ExportStroke(
                                id = stroke.id,
                                layerId = stroke.layerId,
                                toolType = stroke.toolType,
                                points = stroke.points,
                                color = stroke.color,
                                width = stroke.width,
                                pressure = stroke.pressure
                            )
                        }
                        ExportLayer(
                            id = layer.id,
                            pageId = layer.pageId,
                            index = layer.index,
                            type = layer.type,
                            visible = layer.visible,
                            locked = layer.locked,
                            strokes = exportStrokes
                        )
                    }
                    ExportPage(
                        id = page.id,
                        notebookId = page.notebookId,
                        index = page.index,
                        layers = exportLayers
                    )
                }
                ExportNotebook(
                    id = nb.id,
                    title = nb.title,
                    createdAt = nb.createdAt.toEpochMilli(),
                    updatedAt = nb.updatedAt.toEpochMilli(),
                    pages = exportPages
                )
            }
            val jsonString = json.encodeToString(exportData)
            val fos = FileOutputStream(file)
            fos.write(jsonString.toByteArray())
            fos.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importNotebooks(context: Context, file: File): Boolean {
        return try {
            val fis = FileInputStream(file)
            val jsonString = fis.readBytes().decodeToString()
            fis.close()
            val exportData = json.decodeFromString<List<ExportNotebook>>(jsonString)

            // Clear existing data (or merge - here we clear)
            db.clearAllTables()

            // Re-insert
            db.runInTransaction {
                exportData.forEach { expNb ->
                    val nb = NotebookEntity(
                        id = expNb.id,
                        title = expNb.title,
                        createdAt = java.time.Instant.ofEpochMilli(expNb.createdAt),
                        updatedAt = java.time.Instant.ofEpochMilli(expNb.updatedAt)
                    )
                    db.notebookDao().insert(nb)

                    expNb.pages.forEach { expPage ->
                        val page = PageEntity(
                            id = expPage.id,
                            notebookId = expPage.notebookId,
                            index = expPage.index
                        )
                        db.pageDao().insert(page)

                        expPage.layers.forEach { expLayer ->
                            val layer = LayerEntity(
                                id = expLayer.id,
                                pageId = expLayer.pageId,
                                index = expLayer.index,
                                type = expLayer.type,
                                visible = expLayer.visible,
                                locked = expLayer.locked
                            )
                            db.layerDao().insert(layer)

                            expLayer.strokes.forEach { expStroke ->
                                val stroke = StrokeEntity(
                                    id = expStroke.id,
                                    layerId = expStroke.layerId,
                                    toolType = expStroke.toolType,
                                    points = expStroke.points,
                                    color = expStroke.color,
                                    width = expStroke.width,
                                    pressure = expStroke.pressure
                                )
                                db.strokeDao().insert(stroke)
                            }
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
