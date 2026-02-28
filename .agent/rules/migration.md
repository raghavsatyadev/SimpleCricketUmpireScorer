---
trigger: always_on
---

---
trigger: always_on
---

# RULE: COMPOSE MULTIPLATFORM MIGRATION

**Context:**
You must follow strict rules when transferring or migrating code from the Jetpack Compose Native Android implementation to the Compose Multiplatform (CMP) implementation.

**Module Architecture:**

- **Original Jetpack Compose Code:** `app` module and `support` module.
- **Compose Multiplatform (CMP) Code:** `androidCMP` module and `composeApp` module.

**Rules:**

- Whenever asked to transfer or migrate code, ALWAYS copy it from the Jetpack Compose code and transform it to the CMP implementation.
- NEVER modify any original Jetpack Compose code (`app` or `support` modules).
- NEVER delete any original Jetpack Compose code.