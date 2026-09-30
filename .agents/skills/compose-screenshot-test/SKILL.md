---
name: compose-screenshot-test
description: Compose Preview Screenshot Testing for Compose UI — AGP screenshot testing plugin, @PreviewTest, reference goldens, updateScreenshotTest, and validateScreenshotTest.
version: 1.0.0
---

# Compose Preview Screenshot Testing

This project uses Google's official Android Gradle Plugin Compose Preview Screenshot Testing
(`com.android.compose.screenshot`) to catch visual regressions across screens and components.

## Commands

| Task | Command |
| --- | --- |
| Validate screenshots against goldens | `./gradlew :androidCMP:validateScreenshotTest` |
| Record / update reference goldens | `./gradlew :androidCMP:updateScreenshotTest` |
| Run for specific variant | `./gradlew :androidCMP:validateDebugScreenshotTest` |

On Windows PowerShell, use `.\gradlew.bat`.

## Architecture & Layout

```text
androidCMP/
├── build.gradle.kts                            # alias(libs.plugins.compose.screenshot), experimental.enableScreenshotTest
├── src/
│   ├── screenshotTest/kotlin/.../              # Preview tests annotated with @PreviewTest + @Preview
│   │   └── AppScreenshotsTest.kt
│   └── screenshotTestDebug/reference/          # Golden reference images checked into git
gradle.properties                               # android.experimental.enableScreenshotTest=true
gradle/libs.versions.toml                       # screenshot plugin & screenshot-validation-api
```

## Writing Preview Tests

Tests reside in `androidCMP/src/screenshotTest/kotlin/`:

1. **Annotations**: Every test composable must have `@PreviewTest` and `@Preview(showBackground = true)`.
2. **App Theme**: Wrap the composable in `AppTheme { ... }` so colors, typography, and shapes render with the Material 3 Expressive theme.
3. **Koin DI**: Screens using `koinViewModel()` or injected dependencies must wrap with `KoinApplication`:
   ```kotlin
   @Suppress("DEPRECATION")
   @PreviewTest
   @Preview(showBackground = true)
   @Composable
   fun PlaygroundScreen_ScreenshotTest() {
     KoinApplication(
       application = {
         modules(appModule)
       }
     ) {
       AppTheme {
         PlaygroundScreen(
           viewModel = koinViewModel(),
           contentPadding = PaddingValues(16.dp),
         )
       }
     }
   }
   ```
4. **Deterministic UI State**: Pass explicit state models (such as `AssistantSetup`) with fixed permissions and status to avoid device-dependent or asynchronous visual variance:
   ```kotlin
   @PreviewTest
   @Preview(showBackground = true)
   @Composable
   fun SetupScreen_MissingPermissions_ScreenshotTest() {
     AppTheme {
       SetupScreen(
         setup = AssistantSetup(
           hasOverlayPermission = false,
           isAccessibilityEnabled = false,
           isAssistantRunning = false,
           onOpenOverlaySettings = {},
           onOpenAccessibilitySettings = {},
           onStartAssistant = {},
           onStopAssistant = {},
         ),
         contentPadding = PaddingValues(16.dp),
       )
     }
   }
   ```

## Workflow for UI Changes

1. **Modify UI**: Make design or layout changes (e.g. typography, colors, padding).
2. **Validate**: Run `./gradlew :androidCMP:validateScreenshotTest`. If intentional changes cause diffs, Gradle will report mismatches with HTML diff reports under `androidCMP/build/reports/screenshotTest/`.
3. **Update Goldens**: When the visual change is intended, record new golden images:
   ```bash
   ./gradlew :androidCMP:updateScreenshotTest
   ```
4. **Commit**: Check in the updated images in `androidCMP/src/screenshotTestDebug/reference/` alongside your code modifications.
