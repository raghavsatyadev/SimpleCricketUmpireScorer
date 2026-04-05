# AGENTS.md – SimpleCricketUmpireScorer Developer Guide

## Project Overview

**SCUS** is a cricket umpire scoring application built on **Kotlin Multiplatform (KMP)** with
Compose UI, targeting Android, iOS, and Desktop. The codebase is in **mid-migration** from
traditional Android modules (`app`, `support`) to a unified Compose Multiplatform module (
`composeApp`). Firebase provides auth and Firestore backend; Room provides local persistence across
platforms.

### Module Structure

- **`composeApp`** (CommonMain): Shared KMP business logic, UI, database layer
- **`app`** (Android-only): Legacy Android entry point; gradually migrating screens to `composeApp`
- **`support`** (Android-only): Android utilities, Firebase integration, legacy Database impl
- **`androidCMP`** (Android Compose): Platform-specific Android adaptations
- **`desktopCMP` / `iosApp`**: Desktop and iOS entry points using shared `composeApp` logic

---

## Critical Architectural Patterns

### 1. **Dependency Injection: Koin DSL (Runtime-Based)**

**Why:** KMP-compatible, faster builds than Hilt/KAPT, idiomatic Kotlin.

- **Main Module:** `composeApp/src/commonMain/kotlin/.../support/KoinModule.kt`
    - Registers ViewModels using `viewModelOf(::ScreenNameViewModel)`
    - Database and platform modules composed via `initKoin()`

- **Platform Modules:** `expect`/`actual` pattern for platform-specific DI:
    - `platformDatabaseModule()` defined in each
      `{android,desktop,ios}Main/RoomKoinModule.{platform}.kt`
    - Constructs `RoomDatabase.Builder<AppDatabase>` with platform-specific file paths

- **Verification:** Run `app/src/test/kotlin/.../di/CheckModulesTest.kt` to validate Koin graph at
  compile time

**Key Implementation:**

```kotlin
// composeApp/src/commonMain/kotlin/.../support/KoinModule.kt
val appModule = module {
    singleOf(::UiStateManager)
    viewModelOf(::CreateMatchScreenViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        modules(
            appModule,
            commonDatabaseModule,
            platformDatabaseModule()
        )
    }
}
```

### 2. **State Management: UiState Sealed Class + StateFlow**

**Why:** Type-safe result handling, clear error propagation, composable UI reactivity.

- **Core Sealed Class:** `composeApp/src/commonMain/kotlin/.../models/essential/UiState.kt`
  ```kotlin
  sealed class UiState<out T> {
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val error: CustomError, val code: Int = 400) : UiState<Nothing>()
    data object Initial : UiState<Nothing>()
  }
  ```

- **ViewModel Pattern:** Emit `UiState` changes via `StateFlow`
  ```kotlin
  private val _createMatchRecordEvent = MutableStateFlow<UiState<MatchRecord>>(UiState.Initial)
  val createMatchRecordEvent = _createMatchRecordEvent.asStateFlow()
  
  fun saveMatchRecord() {
    viewModelScope.launch {
      _createMatchRecordEvent.emit(UiState.Success(record))
    }
  }
  ```

- **UI Collection:** Use `collectAsStateWithLifecycle()` in Compose
  ```kotlin
  val state by viewModel.createMatchRecordEvent.collectAsStateWithLifecycle()
  when (state) {
    is UiState.Success -> ShowSuccess(state.data)
    is UiState.Error -> ShowError(state.error)
    is UiState.Initial -> {}
  }
  ```

### 3. **Database Layer: Room + KSP Across Platforms**

**Critical:** Room schema auto-migration disabled (`fallbackToDestructiveMigration(true)`). Manual
migrations managed in `MigrationUtil.kt`.

- **Common Schema:** `composeApp/src/commonMain/kotlin/.../support/database/AppDatabase.kt`
    - Single entity: `MatchRecord` (cricket match data with nested `TeamDetail` embeds)
    - Uses `@TypeConverter` for JSON serialization of complex types

- **Repository Pattern:** `MatchRecordRepository` interface in `commonMain`, impl in
  `MatchRecordRepositoryImpl`
    - All DB queries flow through repository; ViewModels never directly access DAO

- **Platform Database Setup:** Each platform declares `RoomDatabase.Builder<AppDatabase>` in
  `platformDatabaseModule()`
    - Android: Uses `androidContext().getDatabasePath()`
    - Desktop: `System.getProperty("java.io.tmpdir")`
    - iOS: `NSFileManager` document directory

**Schema Versioning:**

- Current version: 1 (in `Constants.DB.VERSION`)
- All schema snapshots stored in `composeApp/schemas/` for compile verification

### 4. **Firebase Integration: expect/actual Pattern**

- **Common interfaces:** `FireStoreRepository`, `AuthRepository` in `commonMain`
- **Android-only impl:** `FirebaseAuthUtil`, `FireStoreRepositoryImpl` in `support` module
- **Placeholder for CMP:** `DummyAuthRepository` used in multiplatform builds (real auth requires
  platform-specific SDKs)

---

## Build System & Conventions

### Gradle Structure

- **Versions managed centrally:** `gradle/libs.versions.toml` (457 lines)
    - SDK targets: minSdk=26, targetSdk=36 (Baklava preview)
    - Kotlin 2.3.20, AGP 9.2.0-alpha07

- **Plugin Chain:**
    1. **KSP** (Kotlin Symbol Processing): Room, @Serializable code gen
    2. **Compose Compiler Plugin:** Stability analysis
    3. **Kotzilla**: Version/build metadata generation
    4. **SonarQube**: Code quality metrics
    5. **Room**: Schema export and migrations

- **Flavor Dimensions:** `isPlayStoreVersion` splits into `Dev` and `Prod` builds
    - Variant filtering: Only `Prod-release` and `Dev-debug` variants enabled

### Build Variants to Use

```powershell
# Clean build (all platforms)
./gradlew clean build

# Android-specific (Dev debug)
./gradlew :app:assembleDevDebug

# Multiplatform tests
./gradlew :composeApp:testCommonUnitTest

# Code quality checks
./gradlew sonar
```

---

## Navigation Architecture

- **Common NavHost:** `AppNavHost.kt` in `composeApp/commonMain` with typed routes
- **Route Definitions:** `AppRoutes.kt` enum-based sealed class destinations
- **State Persistence:** Navigation state bound to ViewModel lifecycle via `Navigation3` library
- **Android Legacy:** `MainActivity` still exists but delegates to `MainScreen` Composable from CMP

---

## Testing Strategy

### Unit Tests

- **Koin Graph Validation:** `app/src/test/kotlin/.../di/CheckModulesTest.kt`
    - Runs `appModule.verify()` to catch missing dependency definitions early

### Instrumented Tests (Android)

- **Database Migrations:** `support/src/androidTest/kotlin/.../MigrationTest.kt`
    - Uses `MigrationTestHelper` to verify Room migration chain

### Test Fixtures

- Bundles: `libs.bundles.test` (JUnit4, Koin test DSL), `libs.bundles.androidTest` (AndroidX
  instrumentation)

---

## File Organization & Key Patterns

### Common Code Location Rules

- **UI Screens & ViewModels:** `composeApp/src/commonMain/kotlin/.../ui/{screen_name}/`
- **Repositories & Use Cases:** `composeApp/src/commonMain/kotlin/.../support/repository/`
- **Database Models & Converters:** `composeApp/src/commonMain/kotlin/.../support/database/`
- **Serializable Data Classes:** `composeApp/src/commonMain/kotlin/.../models/` with `@Serializable`
  annotation

### Android-Only Code

- **Firebase/Auth:** `support/src/main/kotlin/.../google/`
- **WorkManager Scheduling:** `support/src/main/kotlin/.../background/`
- **Android-specific Utilities:** `support/src/main/kotlin/.../extensions/`

### Platform Adaptations

- **Expect/Actual:** Resource loading, database initialization (use `expect fun` in commonMain,
  `actual` in each platform)
- **Source Sets:** Use `androidMain`, `desktopMain`, `iosMain` folders for platform-specific code

---

## Critical Migration Status (CMP_Migration_Status.md)

**36 of 66 support module files** have been transferred to `composeApp/commonMain`. Understand what
remains Android-only:

### ❌ Cannot Migrate (Android-Specific)

- Context/Intent-dependent utilities (file paths, implicit intents, storage)
- WorkManager scheduling, notification APIs
- Firebase auth/Firestore implementations (use `expect`/`actual` pattern instead)

### ⚠️ Partially Migrated

- `DateExtensions`: Removed Java `java.time.*` calls, use `kotlinx.datetime` instead
- `Theme.kt`: Omitted dynamic color support (Android 12+ feature)

### ✅ Transferred

- Data models (User, MatchRecord, UiState, CustomError)
- Serialization extensions, validators, date helpers
- Compose UI components (AppToolBar, Dialogs, Theme, Colors)

---

## Developer Workflows

### Adding a New Screen

1. **Create ViewModel** in `composeApp/src/commonMain/kotlin/.../ui/{screen_name}/`
    - Inherit from `CoreScreenViewModel(uiStateManager)`
    - Register in `KoinModule.kt` via `viewModelOf(::YourScreenViewModel)`

2. **Create Composable** in same location
    - Use `collectAsStateWithLifecycle()` for StateFlow observation
    - Inject ViewModel: `@Composable fun YourScreen(vm: YourScreenViewModel = koinViewModel())`

3. **Add Route** to `AppRoutes.kt` and handle in `AppNavHost`

4. **Verify Koin Graph:** Run `CheckModulesTest`

### Adding a Database Entity

1. **Update `AppDatabase.kt`**: Add `@Entity` class and new DAO method
2. **Increment `Constants.DB.VERSION`**
3. **Create migration** in `MigrationUtil.kt` if auto-migration fails
4. **Run code generation:** `./gradlew kspCompile`
5. **Verify schema snapshot:** New JSON created in `composeApp/schemas/`

### Running Full Build

```powershell
# Clean and verify multiplatform
./gradlew clean build -DskipSigning=true

# Run unit tests on all platforms
./gradlew :composeApp:testCommonUnitTest

# Build and sign APK (requires keystore)
./gradlew :app:assembleDevDebug
```

---

## Common Pitfalls & Solutions

| Issue                                           | Root Cause                     | Solution                                                             |
|-------------------------------------------------|--------------------------------|----------------------------------------------------------------------|
| "unresolved reference" to Firestore APIs in CMP | Firebase libs Android-only     | Use `expect`/`actual`, or add gitlive-firebase multiplatform wrapper |
| Room generated code not found                   | KSP not run before compilation | Run `:composeApp:kspCompile` first, or clean build                   |
| Koin injection fails at runtime                 | Missing module registration    | Add to `initKoin()`, verify with `CheckModulesTest`                  |
| Database migration crashes                      | Schema version mismatch        | Check `Constants.DB.VERSION`, add migration to `MigrationUtil`       |
| Compose recomposition loops                     | StateFlow not `.asStateFlow()` | Always convert `MutableStateFlow` via `.asStateFlow()` for exposure  |
| Android variants don't build                    | Flavor filtering rules         | Check `androidComponents.beforeVariants` in `app/build.gradle.kts`   |

---

## Key Dependencies & Their Roles

| Library                   | Version       | Role                  | Notes                                                                       |
|---------------------------|---------------|-----------------------|-----------------------------------------------------------------------------|
| **Compose Multiplatform** | 1.11.0-beta01 | Shared UI framework   | Use `compose-multiplatform` for KMP, `compose-material3` for Android-only   |
| **Room**                  | 2.8.4         | Local persistence     | SQLite driver bundled in CMP; Android uses native                           |
| **Koin**                  | 4.2.0+        | Dependency injection  | DSL-based, KMP-compatible; use `koin-compose-viewmodel-mp` for VM injection |
| **Firebase (GitLive)**    | 2.4.0         | Multiplatform backend | Wrapper for auth/Firestore; Android also has native SDKs                    |
| **Navigation3**           | 1.1.0+        | Typed navigation      | Use `navigation3-ui-mp` for KMP, `navigation3` for Android                  |
| **Lifecycle**             | 2.11.0-alpha+ | State management      | Use multiplatform versions in `commonMain`                                  |
| **Kotzilla**              | 2.1.3         | Build metadata        | Generates version constants and monitoring                                  |
| **Serialization**         | 1.10.0        | JSON serialization    | `@Serializable` for data models, Room TypeConverters                        |

---

## Debug & Diagnostic Commands

```powershell
# View Koin dependency graph (if debug logging enabled)
./gradlew :composeApp:run -Pkoin.debug=true

# Check Room schema validation
./gradlew :composeApp:kspCompile

# Inspect generated Room code
ls composeApp/build/generated/ksp/*/kotlin/io/github/.../database/AppDatabase_Impl.kt

# Run SonarQube scan locally (requires sonar-scanner in PATH)
./gradlew sonar -Dsonar.host.url=http://localhost:9000

# List all Gradle tasks for composeApp
./gradlew :composeApp:tasks --all
```

---

## Agent Rules

### Android & KMP Build Verification

---
trigger: always_on
---

# RULE: ANDROID & KMP BUILD VERIFICATION

**Context:**
You must verify that the codebase compiles successfully before marking a task as complete.

**Trigger:**

- AFTER formatting the code.
- BEFORE presenting the final solution to the user.

**Command (Configuration Change):**
If you edited `build.gradle.kts`, `libs.versions.toml`, `project.gradle.kts`, `gradle.properties` or
`settings.gradle.kts`:
`.\gradlew.bat help`

**Command (Logic Change):**
If you edited source code (`.kt`, `.xml`):

*For Standard Android:*
`.\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain`

*For KMP / Compose Multiplatform:*
`.\gradlew.bat :composeApp:compileDebugKotlin --no-daemon --console=plain`

**Recovery:**

- If build fails: Read error -> Fix specific issue -> Retry.
- Do NOT guess imports.

**Notes:**

- Always add `--no-daemon --console=plain` in all gradlew commands.

### Kotlin Formatting

---
trigger: always_on
---

# RULE: KOTLIN FORMATTING (STRICT)

**Context:**
You must strictly enforce Google Internal Formatting using the local `ktfmt` tool. Manual formatting
is prohibited.

**Trigger:**

- IMMEDIATELY after editing any `.kt` file.
- BEFORE running any build verification.

**Command:**
`ktfmt --google-style <relative_path_to_file>`

**Verification:**

- If the command runs without error, the file is formatted.
- Do NOT output the file content again.
- Do NOT manually adjust whitespace.

### Compose Multiplatform Migration

---
trigger: always_on
---

# RULE: COMPOSE MULTIPLATFORM MIGRATION

**Context:**
You must follow strict rules when transferring or migrating code from the Jetpack Compose Native
Android implementation to the Compose Multiplatform (CMP) implementation.

**Module Architecture:**

- **Original Jetpack Compose Code:** `app` module and `support` module.
- **Compose Multiplatform (CMP) Code:** `androidCMP` module and `composeApp` module.

**Rules:**

- Whenever asked to transfer or migrate code, ALWAYS copy it from the Jetpack Compose code and
  transform it to the CMP implementation.
- NEVER modify any original Jetpack Compose code (`app` or `support` modules).
- NEVER delete any original Jetpack Compose code.

---

## References & Documentation
