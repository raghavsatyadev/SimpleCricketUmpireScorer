---
trigger: glob
globs: "**/*.kt"
description: Formatting Kotlin with ktfmt (Google style) through scripts/ci-local.sh after every .kt edit.
---

# RULE: KOTLIN FORMATTING (STRICT)

Every `.kt` file must be ktfmt Google style. Manual formatting is prohibited.

**When:** immediately after editing any `.kt` file, before build verification.

**Command:**
`bash scripts/ci-local.sh --fix --format-only`

This fetches the exact ktfmt version CI uses and reformats changed files. **Never** use a `ktfmt`
from your PATH — versions disagree (0.61 output fails a 0.64 check).

- Do NOT output the file content again.
- Do NOT manually adjust whitespace.
