# Plan to Convert to Compose Multiplatform (CMP)

This document outlines the step-by-step plan to convert the existing Android Jetpack Compose
project (`SimpleCricketUmpireScorer`) to a Compose Multiplatform (CMP) project targeting Android and
iOS.

## 1. Dependency Analysis & Migration Strategy

The following table lists existing Android dependencies and their KMP/CMP equivalents or migration
strategies.

| Dependency Category      | Current Android Lib                      | Status           | Migration Strategy / KMP Equivalent                                                                                                      |
|:-------------------------|:-----------------------------------------|:-----------------|:-----------------------------------------------------------------------------------------------------------------------------------------|
| **UI**                   | Jetpack Compose (`androidx.compose.*`)   | ✅ Supported      | **Compose Multiplatform** (jetbrains.compose). Most code moves to `commonMain`.                                                          |
| **Navigation**           | `androidx.navigation:navigation-compose` | ✅ Supported      | **JetBrains Compose Navigation** (based on AndroidX) or `voyager` if preferred. Use `2.8.0+`.                                            |
| **Dependency Injection** | **Hilt** (`dagger-hilt`)                 | ❌ Android Only   | **Migrate to Koin**. Hilt relies on generated Java code which doesn't work in Common/iOS. Koin is the standard for KMP.                  |
| **Database**             | **Room** (`androidx.room`)               | ✅ Supported      | Room 2.7.0+ supports KMP (SQLite Driver). Switch to `androidx.room:room-runtime` (KMP) + `sqlite-bundled`.                               |
| **Image Loading**        | **Coil** (`io.coil-kt`)                  | ✅ Supported      | Upgrade to **Coil 3.x** (KMP). Move to `commonMain`.                                                                                     |
| **Async**                | Coroutines (`kotlinx-coroutines`)        | ✅ Supported      | Fully supported.                                                                                                                         |
| **Serialization**        | `kotlinx-serialization`                  | ✅ Supported      | Fully supported.                                                                                                                         |
| **Firebase**             | Official Firebase SDK                    | ⚠️ C++ / Android | Use **GitLive Firebase SDK** (`dev.gitlive:firebase-*`) or official C++ SDK wrappers. GitLive is easier for Kotlin.                      |
| **Google/Auth**          | Play Services Auth                       | ❌ Android Only   | **Common Interface**: Create `expect class/fun` for Auth. Implement `actual` using Play Services (Android) and Google Sign-In SDK (iOS). |
| **Ads**                  | Play Services Ads                        | ❌ Android Only   | **Common Interface**: Create ad wrapper. Implement `actual` using AdMob Android SDK and AdMob iOS SDK.                                   |

## 2. Project Restructuring Steps

### Step 1: Set up the Multiplatform Module

1. **Modify `plugins`**:
    * Apply `org.jetbrains.kotlin.multiplatform` instead of `kotlin-android` in the shared module (
      currently `app`, but usually better to rename `app` to `composeApp` or similar and make it
      KMP).
    * Apply `org.jetbrains.compose`.
2. **Configure Source Sets**:
    * `commonMain`: The core of the app (UI, ViewModels, Domain, Data).
    * `androidMain`: Android-specific implementations (Activity, Application class, Manifest).
    * `iosMain`: iOS-specific implementations (MainViewController).

### Step 2: Hilt to Koin Migration (Critical)

Hilt is the biggest blocker.

1. Remove Hilt dependencies and plugins.
2. Add Koin KMP dependencies (`koin-core`, `koin-compose`, `koin-android`).
3. Replace `@HiltViewModel` with Koin's `viewModel` module.
4. Replace `@Inject` with Koin's `by inject()` or strictly constructor injection resolved by Koin
   modules.
5. Define `appModule` in `commonMain` containing all shared dependencies.

### Step 3: Room Migration

1. Update Room to `2.7.0+` (or `2.8.4` as currently used).
2. Move Entity and DAO definitions to `commonMain`.
3. In `commonMain`, declare an `expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>`.
4. In `androidMain`: `actual fun` returns standard Android builder.
5. In `iosMain`: `actual fun` returns `RoomDatabase.Builder` using `InstantiateImpl` and a file path
   in standard iOS directories.

### Step 4: UI & Resource Migration

1. Move all `@Composable` screens to `commonMain`.
2. **Resources**: Move `res/drawable`, `res/values` (strings) to `commonMain/composeResources` (
   Compose Multiplatform Resources).
    * Replace `R.string.x` with `Res.string.x`.
    * Replace `R.drawable.x` with `Res.drawable.x`.
3. **ViewModel**: Move ViewModels to `commonMain`. Inherit from `androidx.lifecycle.ViewModel` (now
   KMP).

### Step 5: Platform Integration

1. **Android**: `MainActivity.kt` stays in `androidMain`. It sets the content to the main Composable
   function (e.g., `App()`).
2. **iOS**: Create `MainViewController.kt` in `iosMain` returning
   `ComposeUIViewController { App() }`.

## 3. iOS Project Setup

1. Create a new Xcode project (SwiftUI App) inside an `iosApp` folder.
2. Configure Build Phase in Xcode to run the generic Gradle task (e.g.,
   `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`).
3. In Swift, import the shared framework (e.g., `Shared`).
4. Call the `MainViewController` from Swift.

## 4. Dependencies Checklist (from `libs.versions.toml`)

- [ ] **Remove/Replace**:
    - `dagger-hilt`, `hilt-navigation`, `hilt-work`, `hilt-compiler` -> **Replace with Koin**.
    - `play-services-*` -> **Wrap in expect/actual**.
- [ ] **Update/Move**:
    - `room`, `room-runtime` -> **Enable KMP artifacts**.
    - `coil` -> **Ensure Coil 3.0**.
    - `ktor` -> **Already KMP**.
    - `navigation-compose` -> **Use JetBrains/AndroidX KMP version**.

## 5. Execution Order

1. **Create Branch**: `feature/cmp-migration`.
2. **Init KMP**: Change `app/build.gradle.kts` to KMP format.
3. **DI Swap**: Replace Hilt with Koin.
4. **Common Logic**: Move Domain/Data classes to `commonMain`.
5. **DB Migration**: Move Room to `commonMain`.
6. **UI Migration**: Move Composables and Resources.
7. **iOS Setup**: Initialize Xcode project and connect.
