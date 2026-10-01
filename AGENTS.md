# AGENTS.md

Shared instructions for every coding agent in this repo — Claude, Gemini, Codex, Cursor.
`CLAUDE.md` and `GEMINI.md` just point here.

**SimpleCricketUmpireScorer (SCUS)** — a cricket umpire scoring app, mid-migration from Jetpack Compose (`app`, `support`) to Compose Multiplatform (`composeApp`, `androidCMP`).
Modules: `app`, `support`, `composeApp`, `androidCMP`, `desktopCMP` · app module `:androidCMP` · applicationId `io.github.raghavsatyadev.scus` ·
base branch `migration-cmp`. Values also live in `agent-kit.env`.

## How to work

- Do only what the task asks: no unrequested features, tests, files, docs or refactors.
- Ask only when blocked, or before an action that needs approval (push, PR).
- Before "done", run the check for the change (android-build.md); if none can run, say why.
- When done and checked, stop. Report in five lines or fewer, in ASD-STE100 Simplified Technical English.
- Claude Code: medium effort for scoped edits; high for native or architecture work. A second
  agent only for a review the user asked for.
- Decision model: global skill `local-model`. Global skills live in `global_skills/` (no agent reads it
  directly); install them with `bash global_skills/install.sh`.
  Turn on the local decision model first (`bash ~/.local-model/lm-on`), then use `jgl`,
  `lm-ask`, `lm-rank` and `rg` as the skill says. Rule checks for `lm-diffcheck` (run before a
  review): [.agents/diff-checks.txt](.agents/diff-checks.txt).

## Rules — `.agents/rules/`

| Rule | Read it when |
| --- | --- |
| [android-build.md](.agents/rules/android-build.md) | before calling any task done — the code must compile |
| [format.md](.agents/rules/format.md) | touching any Kotlin — ktfmt Google style is mandatory |
| [artemis-mobile-testing.md](.agents/rules/artemis-mobile-testing.md) | *on demand* — using ARTEMIS MCP tools |
| [branch-pr-policy.md](.agents/rules/branch-pr-policy.md) | *always* — see the summary below |
| [parallel-agents.md](.agents/rules/parallel-agents.md) | the user asks for agents in parallel worktrees — pin the base commit, cap Gradle memory, clean up after |
| [migration.md](.agents/rules/migration.md) | *always* — moving code to CMP; never edit `app` or `support` |

## Skills — `.agents/skills/`

One folder per skill; each `SKILL.md` `description` says when to read it. Claude Code sees them
through the `.claude/skills/` links that `scripts/setup-env.*` create.

## Commands

New machine: run the setup doctor first — `scripts/setup-env.ps1` (Windows) or
`scripts/setup-env.sh` (macOS/Linux).

```bash
scripts/ci-local.sh                      # ktfmt + compile<APP_VARIANT>Kotlin — run before pushing
scripts/gradle-agent.sh <tasks>          # agents: Gradle with errors only; full log in tmp/
./gradlew :androidCMP:assembleDebug   # build
./gradlew :androidCMP:testDebugUnitTest
git config core.hooksPath .githooks      # once: pre-commit runs ktfmt, pre-push runs ci-local.sh
```

`ci-local.sh` flags: `--fix` reformats, `--format-only` skips the compile, `--committed-only`
matches CI exactly. Never format with a `ktfmt` from your PATH; `ci-local.sh` fetches the right one.

## Branches & PRs (strict)

Never push to `migration-cmp` or `master`. Branch off `origin/migration-cmp` as `<type>/<slug>`,
push, and open a PR against `migration-cmp` with `gh pr create --body-file <file>` (never inline
`--body`). Never merge or approve a PR — hand off the URL.

## Working on a bug

Follow [fix-issue](.agents/skills/fix-issue/SKILL.md)
(`/fix-issue <url>` in Claude Code): reproduce on the device → diagnose from real device state →
fix with a test that fails on the old behaviour → reinstall and re-run the reproduction → PR, stop.
A green unit test is not device verification. Inspect the device with ARTEMIS, not
`uiautomator dump`, which rebinds accessibility services.

## Housekeeping

`tmp/` (agent scratch), `memory/` (persistent local data such as device serials),
`.cache/` and every `.env` are git-ignored. Never commit credentials. Never put a token in a remote URL or
`.git/config`; `gh` and the credential manager handle auth. Never clean `tmp/` unless the
user asks — see [workspace-cleanup](.agents/skills/workspace-cleanup/SKILL.md).

---

# Project guide

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
    - SDK targets: minSdk=26, targetSdk=37
    - Kotlin 2.3.20, AGP 9.2.0-alpha07

- **Plugin Chain:**
    1. **KSP** (Kotlin Symbol Processing): Room, @Serializable code gen
    2. **Compose Compiler Plugin:** Stability analysis
    3. **SonarQube**: Code quality metrics
    4. **Room**: Schema export and migrations

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
