package com.example.notes.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        NotebookEntity::class,
        PageEntity::class,
        LayerEntity::class,
        StrokeEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun notebookDao(): NotebookDao
    abstract fun pageDao(): PageDao
    abstract fun layerDao(): LayerDao
    abstract fun strokeDao(): StrokeDao

    fun clearAllTables() {
        runInTransaction {
            notebookDao().deleteAll()
            // other tables will be deleted via cascade or we can add deleteAll to each DAO
        }
    }
}
