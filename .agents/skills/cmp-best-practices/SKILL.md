---
name: cmp-best-practices
description: Project conventions for Compose Multiplatform code in this project — source-set layout, UDF state, resources, dependencies, Compose stability, expect/actual boundaries.
version: 3.1.0
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

## Recomposition

Picked from [skydoves/compose-performance-skills](https://github.com/skydoves/compose-performance-skills);
its Android-only parts (baseline profiles, R8, Hilt) do not apply here.

- **Honest annotations**: mark a type `@Immutable`/`@Stable` only when the contract is true — a false
  one silently skips recompositions. Prefer immutable collections to annotating a mutable one.
- **No `Flow` parameters**: collect at the screen and pass the value down (or a `() -> T` for very
  hot values). New screens collect with `collectAsStateWithLifecycle()` (`lifecycle-runtime-compose-mp`).
- **No fresh literals in arguments**: hoist `listOf(...)`, objects and lambdas that capture nothing
  into `remember { }` or a top-level `persistentListOf(...)`; strong skipping compares unstable arguments by `===`.
- **Hot state in later phases**: animated alpha/scale/translation through `Modifier.graphicsLayer { }`,
  never `Modifier.alpha(state.value)`. Never write a state that was already read in the same pass.
- **`derivedStateOf`**: always inside `remember(keys)`, keyed on captured non-state values, and only
  when the output changes less often than the input. Never wrap a `collectAsState` result in it;
  filter the flow upstream (`distinctUntilChanged`, `conflate` for >10 emissions/s).
- **Effects**: key every effect on what it closes over; `rememberUpdatedState` for callbacks read
  by a long-lived `LaunchedEffect`; `DisposableEffect` for non-coroutine listeners; no allocation
  in `SideEffect`.
- **Lazy lists**: `key` is a stable domain id — never the index or a per-emission random id
  (`animateItem()` needs it). No `BoxWithConstraints` inside items; no `Scaffold` inside `Scaffold`.
- **Proof**: claim a performance gain only from a release build on a device, before and after.

Build and format commands: [AGENTS.md](../../../AGENTS.md#commands).
