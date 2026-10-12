---
name: material3-expressive
description: Material 3 Expressive UI conventions for this project — theme, motion roles, shapes, typography, semantic colors, reduced motion, haptics, and the shared components (AppMotion, StaggeredEntrance, DriftingShape, InningsProgressBar, StatusPill). Use when building, restyling or reviewing a screen, component or animation.
---

# Material 3 Expressive

The theme is `AppTheme` in `composeApp/.../support/theme/Theme.kt`: `MaterialExpressiveTheme`
with `MotionScheme.expressive()`, the Montserrat typography and `appShapes`. Files that use
expressive APIs need `@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)`. Everything below
is in `commonMain` and works on Android, desktop and iOS.

## Motion roles — `support/theme/AppMotion.kt`

Pick the spec by **what moves**, not by taste:

| What moves | Spec | Why |
| --- | --- | --- |
| A value read as data: score, overs, progress end | `AppMotion.dataSpec()` (slow effects) or `tapDataSpec()` (fast effects, for a value that changes on every tap) | Critically damped: a number never shows a value it does not have |
| A container: card, pane, sheet, button size, list placement | `AppMotion.containerSpec()` / `motionScheme.*SpatialSpec()` | Spatial springs may bounce a little |
| Alpha, colour, tint, scrim, elevation | `AppMotion.effectsSpec()` / `motionScheme.*EffectsSpec()` | Opacity must never pass 1 or 0 |
| A transition a gesture drives (predictive back) | `AppMotion.scrubSpec()` (linear tween) | A spring runs ahead of the finger |
| A decorative loop that never stops | `AppMotion.driftSpec()` (linear, 60 s period) | Loops without a jump |
| A first-open list cascade | `StaggeredEntrance` (tween + `EmphasizedDecelerate`) | A timed curve reads as one cascade |

- Springs from `MaterialTheme.motionScheme` for every state change; a fixed `tween` only for the
  last three rows. Speed tier: fast for toggles and icon changes, default for cards, dialogs and
  sheets, slow for full-screen transforms.
- Animate scale, translation, rotation and alpha in `Modifier.graphicsLayer { }`, and read
  animated state inside the lambda, so a frame redraws without recomposing.
- Expand a container from its own edge (a top bar grows down, a bottom bar up), not from the
  centre.
- Desktop: hover changes only effects (tint, outline) on `fastEffectsSpec`; never move or scale
  on hover. iOS: never block the edge-swipe back gesture.

## Reduced motion

`animationsEnabled()` is false when the device has animations off or `LocalReduceMotion` is set.
Compose animations end at once by themselves; check the flag for motion Compose does not time
(infinite loops, the moving wave of a wavy indicator) and to swap spatial motion for a short fade.
Every meaning carried by motion needs a static form too (colour, text, icon).

## Shapes

- Cards 24 dp (`MaterialTheme.shapes.large`), sheets and hero surfaces 28–32 dp, pills
  `shapes.small`. Mix pills, standard radii and asymmetric shapes on purpose.
- Connected buttons: `ButtonGroupDefaults.connectedLeadingButtonShapes()` /
  `connectedTrailingButtonShapes()` with `ButtonGroupDefaults.ConnectedSpaceBetween`; for icon
  buttons, `IconButtonDefaults.shapes(shape, pressedShape)` with an asymmetric shape (see
  `MatchRecordItem`). Buttons take `shapes = ButtonDefaults.shapes()` so they morph when pressed.
- `MaterialShapes` (`Cookie9Sided`, `Cookie12Sided`, `Clover4Leaf`, `Sunny`, `Gem`, `SoftBurst`,
  ...) via `.toShape()` for identity and decoration. Give each identity (a team, a status) its
  own shape as well as its own colour, so it never relies on colour alone.
- Morph between two shapes with `Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided)`
  (`androidx.graphics.shapes`), built once (`by lazy`), drawn through a `Shape` whose
  `createOutline` calls `morph.toPath(progress, Path())` scaled to the size. Drive the progress
  with `containerSpec()`.

## Typography

`MaterialTheme.typography` only. Emphasise with weight (`SemiBold`–`ExtraBold`), not ad-hoc
sizes. Display and headline styles use tabular figures (`fontFeatureSettings = "tnum"`), so a
changing score does not shift sideways.

## Colours

`MaterialTheme.colorScheme` roles only — never `Color(0x…)` in UI code; add new colours to the
theme. Primary for the main action, tertiary/`tertiaryContainer` for a complementary accent, error
for "behind" or destructive. Light in a shimmer: white at 0.65 alpha on light, 0.45 on dark.

## Patterns

- **Wavy means attention.** A progress indicator is `LinearWavyProgressIndicator` only when the
  state needs attention (a chase behind the required rate); otherwise flat
  `LinearProgressIndicator` with `StrokeCap.Round`. Flat when animations are off.
- **One hero moment per screen.** A single slow, decorative motion (`DriftingShape` behind the
  winner's score) — not motion everywhere.
- **Selection pop.** A newly selected swatch or chip scales to 1.25 on `StiffnessMedium`, then back
  to 1 on `DampingRatioMediumBouncy` + `StiffnessLow`. Only on becoming selected, not on recompose.
- **Press compression.** A custom clickable surface scales to ~0.96 while pressed on
  `fastSpatialSpec()`. Material buttons already do this through `shapes`.
- **Swapping content.** `AnimatedContent` with slide + fade; data values use the data specs, and
  `SizeTransform(clip = false)`. A pane swap: `fadeIn + scaleIn(initialScale = 0.98f)` on effects.
- **Loading.** `LoadingIndicator()` (or `ContainedLoadingIndicator`) for a wait, not
  `CircularProgressIndicator`. Pull to refresh: `PullToRefreshDefaults.LoadingIndicator`. Show it
  only after ~400 ms: a local load finishes sooner, and a one-frame indicator reads as a flash.
- **Content that arrives late** fades in (`Animatable` alpha on the effects spec) instead of
  popping in. Never `animateContentSize` on a block whose data loads while a transition runs.
- **FAB size.** A list screen's main action is `SmallExtendedFloatingActionButton` (56 dp);
  Medium and Large are for a screen whose one job is that action (the scoring buttons).
- **Stale data.** Draw it at 0.6 alpha and 0.15 saturation (a `ColorMatrix` layer), and say why in
  words for screen readers.
- **Haptics** (`LocalHapticFeedback`): `SegmentTick` for an add, `Reject` for an undo,
  `GestureThresholdActivate` / `GestureEnd` for drags, `Confirm` for a finished action.

## Motion judgement

Rules adapted from [rock3r/android-ux-skills](https://github.com/rock3r/android-ux-skills)
`STANDARDS.md` (rule ids in brackets):

- **Frequency sets the ceiling** (T-009). 100+ taps a day (the scoring buttons): the component's
  own press feedback only. Tens a day: fast tier. Occasional: default tier. Rare or first-run:
  delight allowed.
- **A correction is not a celebration** (T-027). Adding a ball may be expressive; undoing it
  resolves plainer and quicker (`MotionScheme.standard()` springs, no pop, `Reject` haptic).
- **Never gate input on an animation** (T-012). No `enabled = !isAnimating`, no `snapTo()` before
  `animateTo()`, no `delay()` chains a new tap cannot cancel. `Animatable.animateTo` retargets.
- **Motion starts on the input frame** (T-023). Start the transition at once; the destination
  arrives loading rather than the origin holding still.
- **Exits use fewer properties** (T-010). Something that appears in place (menu, badge, section)
  enters with spatial + effects and leaves with a fade on `fastEffectsSpec` only; a shrinking slot
  beside the fade counts as fade.
- **Back plays the inverse of forward** (T-024). What slid in from the right leaves to the right;
  predictive back shows the reversal live.
- **`animateContentSize` clips** (F-006). It cuts shadows, elevation and ripples past the edge;
  never on a `Card`/`Surface` with elevation.

## Container transform (card → screen)

A card that opens into a screen grows out of the card (`sharedMatchContainer` in
`support/components/SharedElements.kt`). Each rule below fixed a glitch seen in a frame-by-frame
recording; keep all of them:

1. **Setup.** `SharedTransitionLayout` around `NavDisplay(sharedTransitionScope = this)`; each
   entry gives its content `LocalNavAnimatedContentScope.current` (here `SharedEntry`). The card
   and the screen's `Scaffold` share one key, `sharedBounds` on the outermost modifier.
2. **No reflow.** `resizeMode = scaleToBounds(ContentScale.FillWidth, Alignment.TopCenter)`.
   `RemeasureToBounds` relayouts the whole screen at every in-between size: text jumps from one
   line to two mid-flight.
3. **No bounce.** Bounds on `AppMotion.screenTransformSpec()` (critically damped, ~450 ms). An
   expressive spring makes a screen edge overshoot and come back.
4. **Keep the list behind the card.** The screen transition for these entries is
   `fadeIn(tween(500), initialAlpha = 0.99f) togetherWith fadeOut(tween(120, delayMillis = 380))`.
   Fading the list out at once leaves the card growing over a blank surface of the same colour —
   it reads as a cut. `EnterTransition.None` stops the shared bounds animating (they hold, then
   snap); `ExitTransition.KeepUntilTransitionsFinished` is internal in CMP 1.13.
5. **Complete on the first frame.** Pass the list's record in the route (`preview`) and show it
   synchronously (`remember { viewModel.showPreview(preview) }`) until the database copy arrives.
   Data that loads mid-transform makes the bounds wait and then jump.
6. **Corners.** The screen's `OverlayClip` corner animates from the card's radius to 0 on the
   entry's `visibility.transition`, so nothing snaps square at the end.
7. **Crossfade.** `enter = fadeIn(defaultEffectsSpec)`, `exit = fadeOut(fastEffectsSpec)`: the side
   that leaves goes fast, so the card and the screen are never both clearly visible.
8. **Reduced motion:** no shared bounds, the screens fade.

## Verify motion on a device

Motion bugs do not show in screenshots or previews. Record the transition and look at it frame by
frame (on an emulator or a test device — never on the phone the user is working on).

**Judge smoothness on a release build** (R8 on), not debug (F-009). Debug Compose is unoptimised
and has no baseline profile: smooth in debug is safe to trust, but jank seen only in debug is not
evidence — confirm it in release before fixing it. Glitches in *what* moves (a jump, a snap, two
layers at once) do show in debug.

```bash
adb -s <serial> shell screenrecord --time-limit 6 --size 720x1560 /sdcard/rec.mp4 &   # then tap
adb -s <serial> pull /sdcard/rec.mp4 tmp/rec.mp4
# timestamps of changed frames (status bar cropped), to find where the transition starts
ffmpeg -i tmp/rec.mp4 -vf "crop=iw:ih-120:0:120,select='gt(scene,0.004)',showinfo" -f null - 2>&1 | grep -o 'pts_time:[0-9.]*'
# a 30 fps contact sheet of one second from just before that point
ffmpeg -y -ss <t-0.1> -i tmp/rec.mp4 -t 1.06 -vf "fps=30,scale=180:-1,tile=8x4" tmp/sheet.png
```

Read the sheet: a jump of more than a few frames, content popping in, a region that does not move
while others do, or two layers clearly visible at once is a glitch. Fix, reinstall, record again.
Seed realistic data first (a debug build allows `run-as <pkg>` to replace the Room database).
`dumpsys gfxinfo <pkg> framestats` tells real jank (slow frames) from a transition bug.

## Building blocks

| Component | File | Use |
| --- | --- | --- |
| `AppMotion`, `animationsEnabled()`, `LocalReduceMotion` | `support/theme/AppMotion.kt` | Motion roles above |
| `StaggeredEntrance`, `staggerDelayMillis` | `support/components/Motion.kt` | List items on first open: 180 ms, 40 ms steps, capped at 120 ms; `play = false` after the first cascade |
| `DriftingShape` | `support/components/Motion.kt` | The one decorative hero shape |
| `InningsProgressBar` | `support/components/InningsProgressBar.kt` | Innings bowled; wavy and error colour when behind |
| `StatusPill`, `StatusTone` | `support/components/StatusPill.kt` | Won / lost / draw / in progress |
| `AppToolBar` | `support/components/AppToolBar.kt` | Top bar with scroll behaviour |
| `sharedMatchContainer`, `LocalSharedTransitionScope`, `LocalEntryVisibilityScope` | `support/components/SharedElements.kt` | Card → screen transform (above) |
| `liquidRipple`, `rememberLiquidRippleState` | `support/components/LiquidRipple.kt` | A water ripple on unhandled taps (AGSL on Android 13+, SkSL elsewhere; none before 13) |
| `AppRoot`, `ThemeMode`, `LocalAppearance`, `ThemeReveal` | `support/theme/ThemeReveal.kt` | Entry points use `AppRoot`; a theme change is uncovered by a circle from the tap |
| `ThemeModeButton` | `support/components/ThemeModeButton.kt` | Cycles device / light / dark through the reveal |

Expressive components in use: `LoadingIndicator`, `LinearWavyProgressIndicator`, `ToggleButton`
in connected groups, `SmallExtendedFloatingActionButton`, `LargeFloatingActionButton`. Icons are
`Icons.Rounded.*` (selected) / `Icons.Outlined.*` (unselected) or drawable resources — no emoji.

## Review checklist

- [ ] No overshoot on a value read as data; no oscillating alpha.
- [ ] `tween` only for gestures, reveals, loops and the entrance cascade.
- [ ] Transforms in `graphicsLayer`, state read inside it.
- [ ] Every motion has a static form; infinite motion stops when `animationsEnabled()` is false.
- [ ] Colours from the scheme, type from the typography, shapes from the theme or `MaterialShapes`.
- [ ] A new or changed transition was recorded and read frame by frame on a device; any jank
      claim comes from a release build.
- [ ] Motion fits how often it is seen; an undo is plainer than the action it reverses.
- [ ] Input stays live while anything animates; motion starts on the input frame.
- [ ] Exits only fade; back retraces forward.

People planning a new screen can sketch it in [M3E Canvas](https://lnkiai.github.io/m3e-canvas/)
and paste its prompt here. Patterns adapted from [rock3r/headroom](https://github.com/rock3r/headroom)
and [rock3r/android-ux-skills](https://github.com/rock3r/android-ux-skills) (both Apache-2.0).
