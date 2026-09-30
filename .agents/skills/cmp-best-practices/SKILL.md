---
name: cmp-best-practices
description: Project conventions for Compose Multiplatform code in this project — source-set layout, UDF state, resources, dependencies, Compose stability, expect/actual boundaries.
version: 3.0.0
---

# Compose Multiplatform conventions

These are the target conventions for **new or rewritten** code. Older code predates some of them
(some ViewModels may expose several separate `StateFlow`s rather than one `UiState`).
In a bug fix, apply them to what you touch — don't refactor unrelated code in the same PR.

## Layout

```text
<shared-module>/src/
├── commonMain/
│   ├── composeResources/{values,drawable,font}/    # strings.xml, images, fonts
│   └── kotlin/<package>/                           # logic, ui/, di/, theme/
├── androidMain/  iosMain/  jvmMain/                # platform bindings only
<app-module>/src/{main,test,androidTest}/           # Android host app, unit tests
```

Adjust the paths to this repo (the app module is `APP_MODULE` in `agent-kit.env`).

Put logic in `commonMain`; platform source sets only bridge to OS APIs.

## Rules

- **Strings**: all user-facing text in `commonMain/composeResources/values/strings.xml`, read with
  `stringResource(Res.string.<name>)`. No hardcoded UI strings.
- **Dependencies**: only via `gradle/libs.versions.toml` (Koin, Ktor, kotlinx.serialization are
  already there). Never inline coordinates in `build.gradle.kts`. Read the catalog for versions.
- **UDF**: each screen has an `@Immutable` `UiState` data class, a sealed `UiEvent` interface
  handled by one `onEvent`, and one-shot effects through a `Channel` exposed as `Flow`. State is a
  private `MutableStateFlow` exposed as `StateFlow`, updated with `update { it.copy(...) }`.
- **Stability**: `ImmutableList`/`persistentListOf` (kotlinx.collections.immutable) in composable
  parameters; `key` + `contentType` in lazy lists; defer high-frequency reads to lambda modifiers
  (`Modifier.offset { }`, `graphicsLayer { }`).
- **Colors**: `MaterialTheme.colorScheme` tokens only — see
  [material3-expressive](../material3-expressive/SKILL.md).
- **Screenshot tests**: UI screens and preview states are verified against golden images via
  Compose Preview Screenshot Testing — see [compose-screenshot-test](../compose-screenshot-test/SKILL.md).
- **expect/actual**: keep platform bridges small and primitive (e.g. an engine factory), at the
  system boundary. No platform types in `commonMain` signatures.

Build and format commands: [AGENTS.md](../../../AGENTS.md#commands).
