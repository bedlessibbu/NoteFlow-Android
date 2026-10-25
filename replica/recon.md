# Freenotes App - Reconstruction Map

## App Overview
**Target App**: Freenotes (Android note-taking app with handwriting & PDF annotation)
**Platform**: Android (Phone & Tablet)
**Scope**: Core note-taking loop - create/edit notebooks, handwriting/drawing, PDF import & annotation, organization
**Audience**: Students, professionals, creatives needing freehand notes + PDF markup

---

## Screen Inventory

| ID | Screen | Route / Entry Point | Purpose | Key Components | States |
|----|--------|---------------------|---------|----------------|--------|
| S01 | Home / Notebook List | App launch, back from editor | Browse/create notebooks, folders | Toolbar (search, sort, view mode), FAB (new), Grid/List of notebooks, Folder chips | Empty (no notebooks), Loading, Filled, Search results, Selection mode |
| S02 | Notebook Editor (Canvas) | Tap notebook from S01 | Freehand writing, drawing, typing | Canvas (infinite/finite), Toolbar (tools, colors, stroke), Layer panel, Page navigator, Zoom/pan | Empty page, Inking, Erasing, Selecting, Text input, Multi-page |
| S03 | PDF Viewer / Annotator | Import PDF from S01 or share intent | View & annotate PDF pages | PDF renderer, Annotation toolbar, Thumbnail strip, Page selector, Search | Loading, Rendered, Annotating, Text selection, Form filling |
| S04 | Tool Palette / Bottom Sheet | Tap tool icon in S02/S03 | Select pen, highlighter, eraser, shapes, text, lasso | Tool icons, Stroke width slider, Color grid, Presets, Favorites | Collapsed, Expanded, Tool-specific options |
| S05 | Color Picker | Tap color in S04 | Choose stroke/fill color | Hue/saturation picker, Opacity slider, Hex input, Preset swatches, Recent colors | Default, Custom color, Eyedropper active |
| S06 | Page Manager / Thumbnails | Tap page indicator in S02/S03 | Reorder, add, delete, duplicate pages | Thumbnail grid, Drag handles, Page numbers, Context menu | Single page, Multi-page, Selection mode |
| S07 | Folder / Category Manager | Long-press notebook or menu in S01 | Organize notebooks into folders | Folder list, Create/edit/delete, Drag-drop notebooks | Empty, With folders, Editing |
| S08 | Settings | Menu in S01 | App preferences | Sections: General, Canvas, Tools, Sync, Backup, About | Default, Modified |
| S09 | Export / Share Dialog | Menu in S02/S03 | Export page/notebook as PDF, image, text | Format picker, Page range, Quality, Destination apps | Single page, All pages, Annotated only |
| S10 | Search Results | Search icon in S01 | Find text/handwriting across notebooks | Query input, Filter chips (notebooks, tags, date), Results list | Empty query, Loading, Results, No matches |

---

## User Flows

| ID | Flow | Screens | Happy Path Clicks | Edge Cases |
|----|------|---------|-------------------|------------|
| F01 | Create handwritten note | S01 → FAB → S02 (new) → ink → back → S01 | 3 | No storage perm, disk full, app kill during ink |
| F02 | Annotate imported PDF | S01 → Import → S03 → annotate → export → S01 | 5 | Large PDF, password-protected, corrupt, OOM |
| F03 | Organize notebooks | S01 → long-press → S07 → create folder → drag → back | 6 | Nested folders, rename conflict, delete with notes |
| F04 | Search handwriting | S01 → search → type → results → open → S02 | 4 | No index, slow search, partial matches |
| F05 | Multi-page notebook | S02 → page manager → add/page → navigate → ink each | 4 | Page reorder, delete current, duplicate |
| F06 | Customize tool preset | S02 → tool palette → adjust → save preset → use | 4 | Max presets, reset defaults, share preset |

---

## Component Inventory

| Component | Variants | States | Used In |
|-----------|----------|--------|---------|
| TopAppBar | Standard, Search, Selection | Normal, Search expanded, Selection count | S01, S02, S03 |
| BottomAppBar | Toolbar, Palette | Collapsed, Expanded, Tool-specific | S02, S03 |
| FloatingActionButton | Primary, Extended | Normal, Pressed, Disabled, Loading | S01 |
| NotebookCard | Grid, List | Normal, Pressed, Selected, Long-press | S01 |
| ToolButton | Pen, Highlighter, Eraser, Shape, Text, Lasso, Laser | Active, Inactive, Disabled, Configured | S02, S03, S04 |
| StrokeWidthSlider | 1-50px | Dragging, Discrete steps | S04, S05 |
| ColorSwatch | Preset, Custom, Recent | Normal, Selected, Pressed | S04, S05 |
| PageThumbnail | Portrait, Landscape | Normal, Selected, Current, Drag | S06 |
| FolderChip | Flat, Elevated | Normal, Selected, Editing | S01, S07 |
| SearchBar | Persistent, Expandable | Empty, Typing, Results, Voice | S01, S10 |
| Dialog | Alert, Confirm, Picker, BottomSheet | Open, Closing, Dismissed | S05, S08, S09 |
| CanvasView | Infinite, Page-bound | Idle, Inking, Panning, Zooming, Selecting | S02, S03 |

---

## Inferred Data Model

| Entity | Fields | Evidence | Confidence |
|--------|--------|----------|------------|
| Notebook | id, title, coverImage, createdAt, updatedAt, folderId, pageCount, isFavorite, tags[], metadata{} | S01 cards, F01, F03 | High |
| Page | id, notebookId, index, backgroundType (plain/grid/dot/template), backgroundColor, layers[], canvasBounds, thumbnailPath | S02, S06, F05 | High |
| Layer | id, pageId, type (vector/raster/text), zIndex, visible, locked, strokes[], bounds | S02 tool palette, layers concept | High |
| Stroke | id, layerId, toolType, points[], color, width, opacity, pressure[], timestamp | S02 inking, S04 tools | High |
| PDFDocument | id, title, filePath, pageCount, passwordHash?, createdAt, lastOpenedPage, annotations[] | S03, F02 | High |
| PDFAnnotation | id, documentId, pageIndex, type (ink/highlight/text/shape/stamp), data{}, color, width, author, createdAt | S03 toolbar, F02 | High |
| Folder | id, name, color, icon, parentId, notebookCount, createdAt | S01, S07, F03 | High |
| ToolPreset | id, name, toolType, color, width, opacity, pressureCurve, shapeConfig | S04, F06 | Medium |
| AppSettings | canvasDefaults{}, toolDefaults{}, gestureMappings{}, backupConfig{}, theme, language | S08 | High |

---

## Feature Matrix

| Feature | Area | Priority | Original | Clone | Notes |
|---------|------|----------|----------|-------|-------|
| Infinite canvas with zoom/pan | Canvas | Must | Yes | Yes | Core differentiator |
| Pressure-sensitive inking | Inking | Must | Yes | Yes | Stylus support critical |
| Multiple pen types (ballpoint, fountain, brush) | Tools | Must | Yes | Yes | Distinct feel per tool |
| Highlighter with blend modes | Tools | Must | Yes | Yes | Multiply blend for realism |
| Eraser (stroke, area, object) | Tools | Must | Yes | Yes | Three modes |
| Shape recognition (line, rect, circle, arrow) | Tools | Should | Yes | Yes | Snap to shape |
| Text boxes with rich formatting | Tools | Should | Yes | Yes | Font, size, alignment |
| Lasso select (move, resize, copy, delete) | Tools | Must | Yes | Yes | Freeform selection |
| Layer management (add, reorder, hide, lock) | Canvas | Should | Yes | Yes | Pro feature |
| Page templates (blank, lined, grid, dot, music, Cornell) | Canvas | Should | Yes | Yes | User-customizable |
| PDF import (file picker, share target) | PDF | Must | Yes | Yes | Large file handling |
| PDF rendering (MuPDF/pdfium) | PDF | Must | Yes | Yes | Hardware accelerated |
| PDF annotation (ink, highlight, text, shapes, stamps) | PDF | Must | Yes | Yes | Non-destructive |
| PDF form filling | PDF | Could | Yes | Maybe | AcroForm support |
| PDF text search & selection | PDF | Should | Yes | Yes | Text layer overlay |
| Notebook folders/categories | Organization | Must | Yes | Yes | Nested folders |
| Notebook covers & custom thumbnails | Organization | Should | Yes | Yes | Visual identity |
| Tags & filtering | Organization | Could | Yes | Maybe | Secondary org |
| Full-text + handwriting search | Search | Should | Yes | Maybe | Requires OCR/index |
| Export (PDF, PNG, SVG, TXT) | Export | Must | Yes | Yes | Batch export |
| Cloud sync (Google Drive, Dropbox) | Sync | Could | Yes | No | Out of scope v1 |
| Local backup/restore | Sync | Should | Yes | Yes | JSON + assets |
| Dark/Light/System theme | UI | Must | Yes | Yes | Material 3 |
| Left/right handed UI | UI | Should | Yes | Yes | Toolbar position |
| Gesture shortcuts (two-finger undo, etc.) | UX | Should | Yes | Yes | Configurable |
| Presentation mode (external display) | UX | Could | Yes | No | v2 |

---

## What Cannot Be Cloned (Skip)

| Feature | Reason |
|---------|--------|
| Proprietary handwriting recognition/OCR engine | Licensed tech, requires cloud API |
| Cloud sync service & account system | Infrastructure, not app feature |
| Handwriting-to-text conversion (premium) | ML model, licensing |
| Collaborative real-time editing | Backend infrastructure |
| Marketplace for templates/stickers | Content licensing, server |
| Audio recording linked to notes | Separate feature domain |
| Handwriting beautification AI | Proprietary ML |
| Cross-device sync via proprietary cloud | Service dependency |

---

## Size Estimation

| Metric | Count | Notes |
|--------|-------|-------|
| Screens | 10 | S01-S10 |
| User Flows | 6 | F01-F06 |
| Entities | 8 | Notebook, Page, Layer, Stroke, PDFDoc, PDFAnnotation, Folder, ToolPreset, Settings |
| Components | 12 | Reusable UI parts |
| Hard Parts | 4 | 1) Smooth inking at 60fps 2) PDF rendering perf 3) Infinite canvas math 4) Stroke serialization |

**Overall Size**: **L (a quarter)** - Significant custom drawing, PDF rendering, and data persistence complexity.

**Hardest Parts**:
1. **High-performance inking** - Sub-millisecond stroke capture, Bezier smoothing, pressure curves, no lag
2. **PDF rendering & annotation** - Large file handling, page caching, annotation overlay alignment
3. **Infinite canvas coordinate system** - Zoom/pan transform matrix, viewport culling, stroke hit-testing
4. **Data serialization** - Efficient stroke storage, undo/redo, migration, backup format

---

## Next Step
→ `/replica-architect` to design the technical architecture, data layer, and module structure.