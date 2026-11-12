---
trigger: always_on
description: SCUS-specific overrides and facts for the shared agent-workflow skills and rules. Wins over them.
---

# Project overrides — SCUS

Shared skills/rules are links into agent-workflow (`scripts/link-project.sh`); this file and
`.agents/flows/` are this repo's own.

- **Reference implementation** of the M3E helpers other apps copy: theme `AppTheme` in
  `composeApp/.../support/theme/Theme.kt` (Montserrat, `appShapes`), `AppMotion.kt`,
  `ThemeReveal.kt`, `support/components/Motion.kt`, `SharedElements.kt` (`sharedMatchContainer`),
  `LiquidRipple.kt`, `Celebration.kt`. All `commonMain` (Android, desktop, iOS).
- **App specifics for M3E**: scoring buttons are the 100+-taps-a-day case (T-009: restrained);
  undoing a ball is a correction (T-027: `MotionScheme.standard()`, no pop, `Reject` haptic);
  `InningsProgressBar` is the shaped progress; the one hero motion is `DriftingShape` behind the
  winner's score; match cards open via `sharedMatchContainer` with per-piece keys
  (`match-<id>-score-1`, `teamNameKey(...)`).
- **Migration**: see the project-local `migration` rule.
- **Preview example**: `composeApp/src/commonMain/kotlin/io/github/raghavsatyadev/library/support/components/AppToolBar.kt AppToolBarPreview`.
