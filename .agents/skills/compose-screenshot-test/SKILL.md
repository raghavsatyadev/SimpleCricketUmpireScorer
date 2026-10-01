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

Follow the existing tests in `androidCMP/src/screenshotTest/kotlin/`:

1. Annotate each test `@PreviewTest` + `@Preview(showBackground = true)`.
2. Wrap it in your app theme (`AppTheme { }`); screens that get a ViewModel from DI (Koin
   `koinViewModel()`, Hilt) also need the DI setup around them, e.g.
   `KoinApplication(application = { modules(appModule) }) { }` (with `@Suppress("DEPRECATION")`).
3. Pass explicit state (fixed permissions, no-op lambdas), never device- or time-dependent state.

## Workflow for UI Changes

1. **Modify UI**: Make design or layout changes (e.g. typography, colors, padding).
2. **Validate**: Run `./gradlew :androidCMP:validateScreenshotTest`. If intentional changes cause diffs, Gradle will report mismatches with HTML diff reports under `androidCMP/build/reports/screenshotTest/`.
3. **Update Goldens**: When the visual change is intended, record new golden images:
   ```bash
   ./gradlew :androidCMP:updateScreenshotTest
   ```
4. **Commit**: Check in the updated images in `androidCMP/src/screenshotTestDebug/reference/` alongside your code modifications.
