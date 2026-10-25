# ✨ Architecture Overview

> **Purpose** – A clean, modular design for a feature‑rich Android notes app that imitates Freenotes’ core UX (handwriting, PDFs, notebooks).  The design balances performance, testability, and maintainability while keeping the implementation on‑canvas and offline‑first.

---

## 👩‍💻 Core Technology Stack
| Layer | Tool | Rationale |
|-------|------|-----------|
| **App** | Kotlin + Jetpack Compose (v1.5+) | Modern, declarative UI, expressive DSL, 2026‑safe. |
| **DI / Modules** | Hilt + Dagger‑Designed modules | Compile‑time type safety, zero‑boilerplate injection. |
| **Persistence** | Room (SQLite) + Kotlin Coroutines + Flow | Statically typed schema, safe query APIs, full offline support. |
| **Shared Preferences** | DataStore (proto‑based) | Modern, asynchronous, migration‑friendly. |
| **Canvas / Drawing** | Android `Canvas` API + Skia + optional OpenGL/RenderScript fallback | 60 fps inking, pressure‑aware, cross‑hair precision. |
| **PDF Rendering** | MuPDF (or pdfium) via the official Android wrapper | Hardware accelerated, small footprint, robust rendering. |
| **Networking** | Retrofit + OkHttp + Moshi | Declarative HTTP client, graceful error handling, head‑less offline. |
| **Navigation** | Accompanist Navigation Compose | Declarative route graph; back‑stack aware; supports deep links. |
| **State** | ViewModel + State‑Flow + Model‑View‑ViewModel (MVVM) | Single source of truth, lifecycle‑aware, testable. |
| **Testing** | JUnit + AndroidX Test + Espresso + MockK + Truth + Robolectric | Unit + UI façade with test doubles. |

---

## 📦 Module Structure
```
└─ core
    ├─ repo
    │   ├─ data
    │   │   ├─ dao (Room DAO interfaces)
    │   │   ├─ model (Value objects + JSON adapters)
    │   │   └─ provider (e.g., local notes provider, PDF provider)
    │   ├─ domain
    │   │   ├─ entity (Notebook, Page, Layer, Stroke, PDFDocument, etc.)
    │   │   ├─ repository (interfaces exposing CRUD + sync)
    │   │   ├─ usecase (business logic operators)
    │   │   └─ mapper (DB ↔ domain)
    │   └─ di (Hilt module for repository bindings)
    ├─ ui
    │   ├─ components
    │   │   ├─ toolbar
    │   │   ├─ canvas
    │   │   ├─ pdfview
    │   │   └─ dialogs
    │   ├─ screens
    │   │   ├─ home
    │   │   ├─ notebook
    │   │   ├─ pdfviewer
    │   │   ├─ settings
    │   │   └─ ...
    │   └─ navigation
    ├─ platform
    │   ├─ android
    │   │   ├─ di
    │   │   ├─ res
    │   │   ├─ utils
    │   │   └─ activity
    ├─ canvas-engine
    │   ├─ InkingEngine.kt (stroke data, hit‑testing, transformations)
    │   └─ DrawingSurface.kt (interop with `View` and Compose `AndroidView`)
    └─ pdf-engine
        ├─ PdfRenderer.kt
        ├─ PdfAnnotationManager.kt
        └─ Adapter (MuPDF wrapper)
```

> **Modular boundaries** are *exactly* the public interfaces that will be exercised by UI.  Every *feature* lives in UI -> ViewModel -> UseCase -> Repository, keeping data and algorithms isolated.

---

## 📚 Data Layer (Room + DataStore)

### 1. Database Tables
| Table | Purpose | Key Columns | Indexes |
|-------|---------|-------------|---------|
| notebooks | Notebook meta‑data | id, title, coverImage, folderId, createdAt | `idx_notebooks_folderId` |
| pages | Page in a notebook | id, notebookId, index, backgroundType, backgroundColor | `idx_pages_notebookId` |
| layers | Vector raster layers | id, pageId, zIndex, type, visible, locked | `idx_layers_pageId` |
| strokes | Vector strokes | id, layerId, points, color, width, opacity, pressure | `idx_strokes_layerId` |
| pdf_documents | Imported PDFs | id, title, filePath, pageCount | `idx_pdf_documents_filePath` |
| pdf_annotations | Annotations per PDF page | id, pdfDocumentId, pageIndex, type, data, color | `idx_pdf_annotations_page` |
| folders | Notebook folders | id, name, color, parentId | `idx_folders_parentId` |
| tool_presets | User saved presets | id, name, color, width, opacity, shapeConfig | — |
| app_settings | User preferences | key, value | — |
| metadata | Global counters (e.g., theme) | key, value | — |

> **SQL migrations** are generated with Room’s `AutoMigration` or explicit `MigrationX` objects.  The first migration defines the schema above.  Every subsequent migration (e.g., adding `tag` column or new annotation type) is a *single* table‑alter step.

### 2. Dalists & DAOs
```kotlin
@Dao
interface NotebookDao { 
  @Query("SELECT * FROM notebooks ORDER BY updatedAt DESC")
  fun getAll(): Flow<List<Notebook>>
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(notebook: Notebook)
  @Delete
  suspend fun delete(notebook: Notebook)
}
```

### 3. Repository Implementation
All read operations expose `Flow<T>` so UI reacts in real‑time.  Write operations are `suspend` functions backed by `@Transaction` blocks that combine DAO calls atomically.

```kotlin
class NotebookRepositoryImpl @Inject constructor(
  private val dao: NotebookDao,
  private val db: AppDatabase
) : NotebookRepository {
  override fun getAll(): Flow<List<Notebook>> = dao.getAll()
  override suspend fun save(notebook: Notebook) {
    dao.insert(notebook)
  }
  // ... other CRUD
}
```

---

## 🖼️ Canvas & Drawing Engine

**InkingEngine.kt** handles raw stroke data, pressure curves, hit‑testing, and transforms.  It exposes a **State‑Flow** of the current layer list and computed paint objects for the surface.

```kotlin
class InkingEngine @Inject constructor() {
  private val _layers = MutableStateFlow<List<Layer>>(emptyList())
  val layers: StateFlow<List<Layer>> get() = _layers

  fun addStroke(points: List<Point>, tool: Tool) { … }
  fun eraseAt(point: Point) { … }
  fun undo() { … }
  fun redo() { … }
}
```

**DrawingSurface.kt** is a `@Composable` wrapping a `Canvas` view using `Modifier.pointerInput` to capture stylus events.  The engine’s state is collected through `collectAsState` and rendered onto the `Canvas` using Skia’s `drawPath` + optional OpenGL for very large canvases.

---

## 📄 PDF Rendering & Annotation

Leverages the pre‑compiled MuPDF / pdfium wrapper (`com.github.barteksc:android-pdf-viewer` or the official `org.openintents:pdftools`).  The **PdfRenderer.kt** exposes a `Flow<PdfRenderResult>` and manages the lifecycle of the PDF file descriptor.

**PdfAnnotationManager.kt** translates user strokes/text into PDF annotation objects via the native PDF API, storing them in `pdf_annotations` table and re‑applying them when the page is rendered.

---

## 📦 Dependency Injection (Hilt)

Each module declares a `@Module`‑annotated object.  Example for *data* layer:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
  @Provides
  fun provideAppDatabase(@ApplicationContext ctx: Context): AppDatabase =
      Room.databaseBuilder(ctx, AppDatabase::class.java, "notes.db").build()

  @Provides
  fun provideNotebookRepository(db: AppDatabase): NotebookRepository =
      NotebookRepositoryImpl(db.notebookDao(), db)
}
```

All `ViewModel`s receive their use‑cases via constructor injection, keeping the ViewModel itself free of any plumbing code.

---

## 🗺️ Navigation (Accompanist)

```kotlin
@Composable
fun AppNavHost() {
  val navController = rememberNavController()
  NavHost(navController, startDestination = "home") {
    composable("home") { HomeScreen(navController) }
    composable("notebook/{id}") { backStackEntry ->
        val id = backStackEntry.arguments?.getString("id")!!
        NotebookScreen(navController, notebookId = id)
    }
    composable("pdf/{path}") { backStackEntry -> ... }
    // ... more routes
  }
}
```

Deep‑links to notebooks/pdfs are built with `Uri.Builder` to interoperate with external sharing intents.

---

## ⚙️ State Management (MVVM + State‑Flow)

Each screen has a dedicated `ViewModel` that contains:
- A *state* object representing the UI (e.g., `HomeState`, `NotebookState`).
- Intents (user actions) emitted as `Event`s.
- A public `StateFlow` that Compose consumes via `collectAsState()`.

> This unidirectional flow guarantees **predictable rendering** and simplifies UI tests via the **Turbine** library.

---

## 🧪 Testing Strategy

| Level | Focus | Tools |
|-------|-------|-------|
| **Unit** | Repository logic, InkingEngine, PDF annotation logic | JUnit, MockK, Coroutines Test, Turbine |
| **Local** | DAO correctness, SQL migrations | Room In-Memory DB, Robolectric |
| **Instrumentation** | UI flows, deep‑link handling, gesture haptics | Espresso, Compose UI Test, ARouter ? |
| **Performance** | Inking FPS, PDF page render time | Systrace, dbench |
| **Smoke** | End‑to‑end flow: create notebook → add ink → save → reopen | UI tests in emulator + physical device | 

> **CI Pipeline** – Gradle `connectedAndroidTest`, `lint`, and `detekt` run on every PR.  Test coverage is targeted at 80 % for Kotlin code.

---

## 📋 Summary
- **Stack** – Kotlin + Jetpack Compose + Hilt + Room + DataStore + MuPDF + `Canvas`.
- **Tables** – 10 (notebooks, pages, layers, strokes, folders, PDFDoc, annotations, tool presets, settings, metadata).
- **Routes** – 12 (home, notebook, pdfviewer, settings, search, etc.).
- **Riskiest Parts**
  1. **High‑fps inking** – motion blur, performance regressions when pages grow.
  2. **PDF annotation overlay** – aligning strokes to page coordinates, handling large PDFs.
  3. **Sync across devices** – conflict resolution (currently out of scope for v1).
- **Next Step** – `/replica-design` to sketch component specs, state shape, and animation plans.

> All code will reside under `app/src/main/java/com/yourcompany/notes` following the module hierarchy above.

---

*This document is generated automatically by the `replica-architect` skill.*