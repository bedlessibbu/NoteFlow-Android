package com.example.notes.di

import com.example.notes.data.NotesDatabase
import com.example.notes.data.NotebookDao
import com.example.notes.export.ExportImportManager
import com.example.notes.prefs.SettingsRepository
import com.example.notes.repository.NotebookRepository
import com.example.notes.repository.NotebookRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import android.content.Context
import androidx.room.Room
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    fun provideDatabase(@androidx.hilt.android.qualifiers.ApplicationContext ctx: Context): NotesDatabase {
        return Room.databaseBuilder(
            ctx,
            NotesDatabase::class.java,
            "notes-db"
        ).build()
    }

    @Provides
    fun provideNotebookDao(db: NotesDatabase): NotebookDao = db.notebookDao()

    @Provides
    @Singleton
    fun provideNotebookRepository(dao: NotebookDao): NotebookRepository = NotebookRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideSettingsRepository(@androidx.hilt.android.qualifiers.ApplicationContext ctx: Context): SettingsRepository = SettingsRepository(ctx)

    @Provides
    @Singleton
    fun provideExportImportManager(db: NotesDatabase): ExportImportManager = ExportImportManager(db)
}
