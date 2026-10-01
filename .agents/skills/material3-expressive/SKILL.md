---
name: material3-expressive
description: Material 3 Expressive UI conventions for this project — theme, motion, typography, semantic colors, and the card/pill/selector patterns the app uses. Use when building or restyling a screen or component.
---

# Material 3 Expressive

Find the app theme (`AppTheme` or similar, usually `.../support/theme/Theme.kt`). Wrap content in
`MaterialExpressiveTheme` with `MotionScheme.expressive()` and the project typography. Files using
expressive APIs need `@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)`.

## Rules

- **Colors**: `MaterialTheme.colorScheme` roles only — never `Color(0x…)` in UI code. Add new
  colors to the theme, not to screens.
- **Motion**: springs from `MaterialTheme.motionScheme`, not fixed `tween` durations.
- **Typography**: `MaterialTheme.typography` styles; emphasise with weight (`SemiBold`–`ExtraBold`),
  not ad-hoc sizes.
- **Shapes**: generous rounding — 16–28 dp for cards, 12 dp for pills.

## Building blocks

Reuse the project's shared components (cards, status pills, tab bars) instead of hand-rolled ones.
List them here per app, for example `SectionCard`, `StatusPill`, `FloatingTabBar`.

Expressive components in use: `LoadingIndicator`, `LinearWavyProgressIndicator`, motion from
`MaterialTheme.motionScheme` (`defaultEffectsSpec`, `fastSpatialSpec`), segmented buttons for
single choice. Icons are `Icons.Rounded.*` (selected) / `Icons.Outlined.*` (unselected) — no emoji.

People planning a new screen can sketch it in [M3E Canvas](https://lnkiai.github.io/m3e-canvas/) and paste its prompt here.
