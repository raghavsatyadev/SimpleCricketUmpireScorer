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
If you edited `build.gradle.kts` or `libs.versions.toml`:
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