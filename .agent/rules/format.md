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