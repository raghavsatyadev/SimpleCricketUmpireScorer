# Plan to Convert to Compose Multiplatform (CMP) - Timetable

This document outlines the step-by-step timetable to convert the existing Android Jetpack Compose project (`SimpleCricketUmpireScorer`) to a Compose Multiplatform (CMP) project.

## Current Status (as of Jan 6)
- **Hilt to Koin**: ✅ Done
- **CMP Migration**: ⏳ Pending (Scheduled for Jan 17-18)

---

## Part 1: Minor Migrations & Preparation
**Schedule**: This Weekend (Jan 10 - Jan 11)
**Goal**: Clean up the Android codebase and prepare it for a smooth transition to KMP.

### Saturday, Jan 10: Cleanup & Standardization
- [ ] **Resources Check**:
    - Ensure all Strings/Colors are in `res/values`.
    - Identify direct Android Asset usage that might need moving to `composeResources`.
- [ ] **Dependency Update**:
    - Update `androidx.navigation`, `room`, `lifecycle`, `coil` to latest stable versions in `libs.versions.toml` (if not already).
- [ ] **Database Inspection**:
    - Review Room Entities (`@Entity`) and DAOs.
    - Ensure no Android-specific imports (like `android.graphics.Bitmap` or `Parcelable` inside DB entities if possible).
- [ ] **Code Sanitation**:
    - Verify no `android.context.Context` is passed into ViewModels (use Koin injection if context is needed, but prefer platform-agnostic interfaces).

### Sunday, Jan 11: Architecture Decoupling
- [ ] **Service Abstraction (Critical)**:
    - Create repository interfaces for **Firebase**, **Google Auth**, and **Ads**.
    - *Why*: These are Android-only SDKs. KMP code cannot see them directly.
    - *Action*: Ensure usage in ViewModels is effectively `repo.doSomething()`, not `Firebase.auth.signIn()`.
- [ ] **ViewModel Check**:
    - Ensure all ViewModels inherit `androidx.lifecycle.ViewModel`.
    - Verify `viewModelScope` usage.

---

## Part 2: The CMP Migration
**Schedule**: Next Weekend (Jan 17 - Jan 18)
**Goal**: Convert project structure to KMP and run on Android.

### Saturday, Jan 17: Infrastructure & Core
- [ ] **Step 1: Module Renaming & Plugin Setup**
    - Rename `app` to `composeApp`.
    - Apply `org.jetbrains.kotlin.multiplatform` & `org.jetbrains.compose` plugins.
    - Configure `androidMain`, `commonMain`, `iosMain` source sets.
- [ ] **Step 2: Dependency Swap**
    - Swap Android dependencies for Multiplatform ones in `commonMain` (`koin-core`, `ktor`, `coil3`, `room-runtime`).
- [ ] **Step 3: Move Domain & Data**
    - Move all "pure Kotlin" code (Models, Repositories, UseCases) to `commonMain`.
    - Implement `expect/actual` for the Service Abstractions created in Part 1.

### Sunday, Jan 18: UI & Integration
- [ ] **Step 4: UI Migration**
    - Move Composable screens to `commonMain`.
    - Set up `composeResources` (Components, Theme, etc.).
- [ ] **Step 5: Database Migration**
    - Move Room Database definition to `commonMain`.
    - Implement `expect/actual` DatabaseBuilder.
- [ ] **Step 6: Android Launch**
    - Fix `MainActivity` (entry point).
    - Verify app runs on Android Emulator.
- [ ] **Step 7: iOS Setup (Bonus)**
    - Create XCode project in `iosApp`.
    - Connect to `composeApp` framework.

---

## Appendix: Dependency Migration Table

| Category  | Current         | Migration Action                                                        |
|:----------|:----------------|:------------------------------------------------------------------------|
| **DI**    | Hilt            | ✅ **Done** (Migrated to Koin)                                           |
| **DB**    | Room (Android)  | **Move to Room KMP**. Use `androidx.room:room-runtime` in `commonMain`. |
| **UI**    | Jetpack Compose | **Compose Multiplatform**. Move to `commonMain`.                        |
| **Image** | Coil 3.x        | **Coil 3 KMP**. Move to `commonMain`.                                   |
| **Auth**  | Play Services   | **Wrap w/ Interface**. Implement `actual` for Android/iOS.              |
| **Ads**   | AdMob           | **Wrap w/ Interface**. Implement `actual` for Android/iOS.              |
