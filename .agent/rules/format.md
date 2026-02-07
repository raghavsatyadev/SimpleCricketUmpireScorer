---
trigger: always_on
---

# Formatting & Style Rules

## 1. The Golden Rule
**NEVER manually format code.**
- Do not adjust whitespace, indentation, or line breaks to "fix" style.
- Do not suggest style changes in code reviews.

## 2. The Tool
You have a direct CLI tool installed in your environment called `ktfmt`.

**Trigger:**
Immediately after generating or editing any Kotlin file (`.kt`).

**Command:**
`ktfmt <filename>`

**Protocol:**
1. Write the code logic.
2. Run `ktfmt src\path\to\File.kt` in the terminal.
3. Trust the result. Do not touch the code again.