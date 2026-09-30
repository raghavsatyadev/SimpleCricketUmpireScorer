---
trigger: always_on
description: Verify the code compiles before calling any task done.
---

# RULE: ANDROID & KMP BUILD VERIFICATION

Verify the code compiles before marking any task complete — after formatting, before presenting
the result.

| You edited | Run |
| --- | --- |
| `build.gradle.kts` / `libs.versions.toml` | `./gradlew help --no-daemon --console=plain` |
| Kotlin / XML sources | `./gradlew ${APP_MODULE}:compileDebugKotlin --no-daemon --console=plain` (same task CI runs) |

`APP_MODULE` comes from `agent-kit.env` (for example `:app`).

On Windows PowerShell use `.\gradlew.bat`.

If the build fails: read the error → fix that issue → retry. Do NOT guess imports.
