# Compose previews — render with the Android CLI

Agents can render any `@Preview` to a PNG without opening the IDE UI by hand:

```bash
android studio render-compose-preview composeApp/src/commonMain/kotlin/io/github/raghavsatyadev/library/support/components/AppToolBar.kt AppToolBarPreview --output-image-file=tmp/preview.png
```

- Needs a running Android Studio (Quail 1 or newer) with this project open and synced
  (`android studio check` lists running instances). The CLI asks that Studio to render.
- `<path>` is the Kotlin file, `<composable>` the preview function name. Add
  `--print-semantics` to get the semantics tree as text, `--project=.` when Studio has more
  than one project open.
- Then `Read` the PNG to look at it. Keep outputs under the gitignored `tmp/`.
- New previews: put them next to the component (or in a `*Previews.kt` file beside it), wrap
  them in the app theme, and pass fake data — never a real ViewModel.
