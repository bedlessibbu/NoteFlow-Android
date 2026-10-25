# NoteFlow Android App - Build Instructions

## Project Structure
```
NoteFlow/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/notes/
│   │   │   │   ├── NotesApplication.kt          # Hilt Application class
│   │   │   │   ├── NoteFlowActivity.kt          # Main Activity (Compose)
│   │   │   │   ├── data/                        # Room database & entities
│   │   │   │   │   ├── NotesDatabase.kt
│   │   │   │   │   ├── NotebookEntity.kt
│   │   │   │   │   ├── PageEntity.kt
│   │   │   │   │   ├── LayerEntity.kt
│   │   │   │   │   ├── StrokeEntity.kt
│   │   │   │   │   ├── Converters.kt
│   │   │   │   │   ├── NotebookDao.kt
│   │   │   │   │   ├── PageDao.kt
│   │   │   │   │   ├── LayerDao.kt
│   │   │   │   │   └── StrokeDao.kt
│   │   │   │   ├── domain/                      # Domain models
│   │   │   │   │   └── DomainModels.kt
│   │   │   │   ├── repository/                  # Repository layer
│   │   │   │   │   └── NotebookRepository.kt
│   │   │   │   ├── di/                          # Hilt modules
│   │   │   │   │   └── DataModule.kt
│   │   │   │   ├── canvas/                      # Drawing engine
│   │   │   │   │   ├── InkingEngine.kt
│   │   │   │   │   └── CanvasView.kt
│   │   │   │   └── ui/
│   │   │   │       ├── theme/                   # Material 3 theming
│   │   │   │       │   ├── NoteFlowTheme.kt
│   │   │   │       │   ├── Shapes.kt
│   │   │   │       │   └── Typo.kt
│   │   │   │       ├── AppNavHost.kt            # Navigation graph
│   │   │   │       └── screens/                 # Screen composables
│   │   │   │           ├── HomeViewModel.kt
│   │   │   │           ├── HomeScreen.kt
│   │   │   │           ├── NotebookViewModel.kt
│   │   │   │           ├── NotebookScreen.kt
│   │   │   │           ├── PDFViewerScreen.kt
│   │   │   │           └── SettingsScreen.kt
│   │   │   ├── AndroidManifest.xml
│   │   │   └── res/
│   │   │       └── values/
│   │   │           ├── colors.xml
│   │   │           └── colors_dark.xml
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── .github/
│       └── workflows/
│           ├── android-ci.yml
│           └── build-apk.yml
├── build.gradle.kts (root)
├── settings.gradle.kts
├── gradle.properties
└── BUILD_INSTRUCTIONS.md
```

## Requirements
- Android Studio Ladybug (2024.2) or later
- JDK 17
- Android SDK 34 (compileSdk)
- Minimum SDK 24 (Android 7.0)

## Build Commands

### Using Android Studio
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Run `Build > Make Project` or press Ctrl+F9
4. Run on emulator or device with `Run > Run 'app'`

### Using Command Line
```bash
# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run lint checks
./gradlew lint

# Build release APK (signed)
./gradlew assembleRelease
```

## Key Features Implemented (Skeleton)

### Core Architecture
- **MVVM** with Hilt dependency injection
- **Room** database for offline-first persistence
- **Jetpack Compose** for UI (Material 3)
- **Navigation** with type-safe routes
- **Coroutines + Flow** for reactive data

### Screens
1. **Home Screen** - Notebook list with FAB for creating new notebooks
2. **Notebook Screen** - Canvas with toolbar (pen, highlighter, eraser placeholders)
3. **PDF Viewer Screen** - Placeholder for PDF rendering
4. **Settings Screen** - Dark mode toggle, canvas settings, backup options

### Canvas / Drawing
- **InkingEngine** - Manages stroke data with undo support
- **CanvasView** - Composable canvas with pointer input for drawing
- Basic stroke rendering with paths

### Data Layer
- **Entities**: Notebook, Page, Layer, Stroke
- **DAOs**: CRUD operations with Flow for reactive UI
- **Repository**: Abstraction over DAOs

## Missing / TODO Features
The following features from the design docs are **not yet implemented** and marked as TODOs:

### High Priority (Must Have)
- [ ] PDF rendering with MuPDF/pdfium integration
- [ ] PDF annotation overlay (ink, highlight, text, shapes)
- [ ] Page management (add, reorder, duplicate, delete pages)
- [ ] Layer management (add, reorder, hide, lock layers)
- [ ] Pressure-sensitive inking with stylus support
- [ ] Multiple pen types (ballpoint, fountain, brush) with distinct physics
- [ ] Highlighter with multiply blend mode
- [ ] Eraser modes (stroke, area, object)
- [ ] Shape recognition (line, rect, circle, arrow)
- [ ] Text boxes with rich formatting
- [ ] Lasso select (move, resize, copy, delete)
- [ ] Page templates (blank, lined, grid, dot, music, Cornell)
- [ ] Infinite canvas with zoom/pan
- [ ] Undo/Redo (persistent across sessions)
- [ ] Palm rejection
- [ ] Vector stroke storage with efficient serialization
- [ ] Export (PDF, PNG, SVG, TXT)
- [ ] Local backup/restore
- [ ] Notebook folders/categories with nested support
- [ ] Notebook covers & custom thumbnails
- [ ] Left/right handed UI
- [ ] Gesture shortcuts (two-finger undo, etc.)

### Medium Priority (Should Have)
- [ ] PDF form filling
- [ ] PDF text search & selection
- [ ] Tags & filtering
- [ ] Full-text + handwriting search (ML Kit OCR)
- [ ] Raster layer support (images, photos)
- [ ] Password protection / biometric lock
- [ ] Auto-save with crash recovery
- [ ] Import images/photos to canvas
- [ ] Grid/guideline snapping
- [ ] Custom paper sizes
- [ ] Multi-window / split screen support
- [ ] Stylus button actions (S Pen, etc.)

### Low Priority (Could Have)
- [ ] Cloud sync (Google Drive, Dropbox)
- [ ] Presentation mode
- [ ] Laser pointer / presentation tools

## GitHub Actions
- **android-ci.yml**: Runs on every push/PR - builds, tests, lint
- **build-apk.yml**: Builds and uploads debug APK as artifact

## Architecture Decisions
1. **Module Structure**: Single app module with package-by-feature
2. **DI**: Hilt for compile-time safety
3. **Database**: Room with Flow for reactive UI
4. **Canvas**: Android Canvas API via Compose `Canvas` + `pointerInput`
5. **PDF**: Using `com.github.barteksc:android-pdf-viewer` wrapper

## Testing
```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

## Known Issues / Limitations
- Canvas drawing is basic (no pressure, no smoothing, no zoom/pan yet)
- PDF viewer is placeholder only
- Notebook creation doesn't persist yet (needs ViewModel callback wiring)
- Settings dark mode toggle doesn't apply theme dynamically
- No actual data persistence for strokes/pages/layers yet

## Next Steps
1. Implement PDF rendering with MuPDF
2. Add zoom/pan to CanvasView
3. Implement pressure-sensitive drawing
4. Wire up notebook creation to repository
5. Add page management UI
6. Implement layer system
7. Add undo/redo with persistent history
8. Implement export functionality
9. Add folder management
10. Settings persistence with DataStore

## License
All code is original implementation. Icons from Material Icons (Apache 2.0).
PDF viewer library: com.github.barteksc:android-pdf-viewer (Apache 2.0).