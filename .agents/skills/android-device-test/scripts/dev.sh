#!/usr/bin/env bash
# Device-driving CLI for physical UI testing over adb.
#
#   dev.sh setup                     discover device/app/screen, seed the cache
#   dev.sh info                      print the resolved environment
#   dev.sh install <apk>             install (or reinstall) an APK
#   dev.sh launch [activity]         launch the app's main activity
#   dev.sh stop                      force-stop the app
#   dev.sh shot [label]              screenshot -> cache/artifacts
#   dev.sh rec start|stop [label]    screen recording
#   dev.sh dump                      UI hierarchy XML -> cache, path on stdout
#   dev.sh find <selector> [opts]    resolve a selector to "x y ..." (TSV)
#   dev.sh tap <selector>            resolve then tap (re-dumps; always correct)
#   dev.sh tap --fast <sel>          reuse a cached coord for this screen
#   dev.sh tapxy <x> <y>             raw coordinate tap
#   dev.sh text <string>             type into the focused field
#   dev.sh key <keycode>             send a keyevent (e.g. BACK, ENTER)
#   dev.sh swipe <x1> <y1> <x2> <y2> [ms]
#   dev.sh scroll up|down [frac]     scroll the screen by a fraction of height
#   dev.sh scroll-to <selector>      scroll down until the selector appears
#   dev.sh wait <selector> [secs]    wait for a selector to appear
#   dev.sh gone <selector> [secs]    wait for a selector to disappear
#   dev.sh logcat start|stop|grab    capture app logs
#   dev.sh files [subpath]           list the app's private files dir
#   dev.sh clear                     clear app data
#   dev.sh clear-field [n]           delete the focused field's text (no dump)
#   dev.sh grant overlay|a11y        grant draw-over-apps / enable our a11y service
#   dev.sh revoke a11y               disable only our a11y service
#   dev.sh log <TAG> [lines]         app log lines for a tag (no dump)
#
# Selectors: text:Exact  textc:contains  id:btn_go  desc:Label  class:...
#            bare strings mean case-insensitive "contains".

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib.sh
source "$HERE/lib.sh"

PY="${DT_PYTHON:-python}"
command -v "$PY" >/dev/null 2>&1 || PY=python3
# Windows Python cannot read MSYS-style paths, so hand it native ones.
UIQUERY="$(dt_winpath "$HERE/uiquery.py")"

dt_init

DUMP_FILE="$DT_TMP/ui-dump.xml"

# -------------------------------------------------------------- screen identity

# A cheap signature for "which screen am I on", used to key coordinate caching.
dt_screen_sig() {
  local act
  act=$(dt_adb_sh dumpsys activity activities \
        | sed -n 's/.*mResumedActivity.*{[^ ]* [^ ]* \([^ ]*\).*/\1/p' | head -1)
  [ -z "$act" ] && act=$(dt_adb_sh dumpsys window \
        | sed -n 's/.*mCurrentFocus=Window{[^ ]* [^ ]* \([^}]*\)}.*/\1/p' | head -1)
  printf '%s|%sx%s' "${act:-unknown}" "$DT_SCREEN_W" "$DT_SCREEN_H"
}

dt_coord_cache_get() {
  [ -f "$DT_COORDS_FILE" ] || return 1
  awk -F'\t' -v k="$1" '$1==k {print $2"\t"$3; found=1} END{exit !found}' "$DT_COORDS_FILE"
}

dt_coord_cache_put() {
  local key="$1" x="$2" y="$3" tmp="$DT_COORDS_FILE.tmp"
  touch "$DT_COORDS_FILE"
  awk -F'\t' -v k="$key" '$1!=k' "$DT_COORDS_FILE" > "$tmp" 2>/dev/null || true
  printf '%s\t%s\t%s\n' "$key" "$x" "$y" >> "$tmp"
  mv "$tmp" "$DT_COORDS_FILE"
}

# ------------------------------------------------------------------- ui dump

dt_dump() {
  local dest="${1:-$DUMP_FILE}" tries=0
  while [ "$tries" -lt 3 ]; do
    # Dump to the device, then pull via exec-out to avoid shell mangling.
    if dt_adb shell uiautomator dump /sdcard/dt-dump.xml >/dev/null 2>&1; then
      if dt_adb exec-out cat /sdcard/dt-dump.xml > "$dest" 2>/dev/null \
         && grep -q "<hierarchy" "$dest" 2>/dev/null; then
        printf '%s' "$dest"; return 0
      fi
    fi
    tries=$((tries+1)); sleep 1
  done
  dt_die "uiautomator dump failed after 3 attempts (is the screen on and unlocked?)"
}

dt_find() {
  local sel="$1"; shift
  dt_dump >/dev/null
  "$PY" "$UIQUERY" "$sel" --dump "$(dt_winpath "$DUMP_FILE")" "$@"
}

# ----------------------------------------------------------------- subcommands

cmd="${1:-info}"; shift 2>/dev/null || true

case "$cmd" in

  setup|info)
    printf 'serial   : %s\n' "$DT_SERIAL"
    printf 'app id   : %s\n' "$DT_APP_ID"
    printf 'screen   : %sx%s (density %s)\n' "$DT_SCREEN_W" "$DT_SCREEN_H" "$(dt_cache_get DENSITY || echo '?')"
    printf 'android  : %s (sdk %s)\n' "$(dt_adb_sh getprop ro.build.version.release)" "$(dt_adb_sh getprop ro.build.version.sdk)"
    printf 'model    : %s\n' "$(dt_adb_sh getprop ro.product.model)"
    printf 'installed: %s\n' "$(dt_adb_sh pm list packages "$DT_APP_ID" | head -1 || echo 'no')"
    printf 'cache    : %s\n' "$DT_CACHE"
    ;;

  wake)
    # Bring the device to a state where UI automation is meaningful:
    # screen on, keyguard dismissed, no shade or dialog covering the app.
    dt_adb shell input keyevent KEYCODE_WAKEUP >/dev/null 2>&1
    dt_adb shell wm dismiss-keyguard   >/dev/null 2>&1
    dt_adb shell cmd statusbar collapse >/dev/null 2>&1
    sleep 1
    dt_log "device awake: $(dt_adb_sh dumpsys power | sed -n 's/.*mWakefulness=\([A-Za-z]*\).*/\1/p' | head -1)"
    ;;

  install)
    apk="${1:?usage: dev.sh install <apk>}"
    [ -f "$apk" ] || dt_die "APK not found: $apk"
    dt_log "Installing $(basename "$apk") ..."
    dt_adb install -r -g "$apk" 2>&1 | tail -5
    ;;

  launch)
    act="${1:-$(dt_cache_get MAIN_ACTIVITY || true)}"

    if [ -z "$act" ]; then
      # Debug builds often register extra launcher activities (LeakCanary and
      # friends), so resolve-activity alone is not trustworthy. List every
      # launcher entry for the package and drop the known diagnostic ones.
      act=$(dt_adb_sh cmd package query-activities \
              -a android.intent.action.MAIN -c android.intent.category.LAUNCHER \
            | sed -n "s@.*name=\($DT_APP_ID[A-Za-z0-9_./]*\).*@\1@p" \
            | grep -vi 'leakcanary\|chucker\|flipper' | head -1)
      # Fall back to the platform's own preference if filtering found nothing.
      [ -z "$act" ] && act=$(dt_adb_sh cmd package resolve-activity --brief "$DT_APP_ID" | tail -1)

      # resolve-activity prints prose like "No activity found" when the package is
      # absent. Caching that poisons every later run, so demand a component name.
      case "$act" in
        *[![:alnum:]_./]*|"") act="" ;;
        *.*) ;;
        *) act="" ;;
      esac
      [ -z "$act" ] && dt_die "Could not resolve a launcher activity for $DT_APP_ID (is it installed?)"
      dt_cache_set MAIN_ACTIVITY "$act"
    fi

    # Accept both "pkg/.Activity" and a bare class name.
    case "$act" in
      */*) target="$act" ;;
      *)   target="$DT_APP_ID/$act" ;;
    esac

    dt_adb shell am start -W -n "$target" 2>&1 | grep -iE "Status|Error|Warning" | head -3
    sleep 2
    dt_log "launched $target"
    ;;

  stop)   dt_adb shell am force-stop "$DT_APP_ID"; dt_log "stopped $DT_APP_ID" ;;
  clear)  dt_adb shell pm clear "$DT_APP_ID" ;;

  shot)
    label="${1:-shot}"
    out="$DT_ARTIFACTS/$(dt_ts)-$label.png"
    dt_adb exec-out screencap -p > "$out" 2>/dev/null
    [ -s "$out" ] || dt_die "screenshot failed"
    printf '%s\n' "$out"
    ;;

  rec)
    sub="${1:?usage: dev.sh rec start|stop [label]}"; label="${2:-rec}"
    case "$sub" in
      start)
        dt_adb shell "rm -f /sdcard/dt-rec.mp4" >/dev/null 2>&1
        # screenrecord caps at 180s; run detached and stop it explicitly.
        dt_adb shell screenrecord --bit-rate 6000000 /sdcard/dt-rec.mp4 >/dev/null 2>&1 &
        echo $! > "$DT_CACHE/rec.pid"
        dt_log "recording started"
        ;;
      stop)
        dt_adb shell pkill -INT -f screenrecord >/dev/null 2>&1
        sleep 3   # let the encoder flush the MP4 moov atom
        out="$DT_ARTIFACTS/$(dt_ts)-$label.mp4"
        dt_adb pull /sdcard/dt-rec.mp4 "$out" >/dev/null 2>&1
        dt_adb shell rm -f /sdcard/dt-rec.mp4 >/dev/null 2>&1
        rm -f "$DT_CACHE/rec.pid"
        [ -s "$out" ] && printf '%s\n' "$out" || dt_log "no recording captured"
        ;;
      *) dt_die "rec: use start or stop" ;;
    esac
    ;;

  dump) dt_dump; echo ;;

  find)
    sel="${1:?usage: dev.sh find <selector>}"; shift
    dt_find "$sel" "$@"
    ;;

  tap)
    fast=0
    if [ "${1:-}" = "--fast" ]; then fast=1; shift; fi
    sel="${1:?usage: dev.sh tap [--fast] <selector>}"; shift
    key="$(dt_screen_sig)|$sel"

    x=""; y=""
    if [ "$fast" = "1" ]; then
      if c=$(dt_coord_cache_get "$key"); then
        x=$(printf '%s' "$c" | cut -f1); y=$(printf '%s' "$c" | cut -f2)
        dt_log "tap(cached) $sel -> $x,$y"
      fi
    fi

    if [ -z "$x" ]; then
      row=$(dt_find "$sel" --clickable-ancestor "$@") || dt_die "selector not found: $sel"
      x=$(printf '%s' "$row" | cut -f1); y=$(printf '%s' "$row" | cut -f2)
      dt_coord_cache_put "$key" "$x" "$y"
      dt_log "tap $sel -> $x,$y"
    fi

    dt_adb shell input tap "$x" "$y"
    sleep 1
    ;;

  tapxy)
    x="${1:?x}"; y="${2:?y}"
    dt_adb shell input tap "$x" "$y"; sleep 1
    ;;

  text)
    s="${1:?usage: dev.sh text <string>}"
    # input text needs spaces escaped; %s is safest for punctuation-free strings.
    esc=$(printf '%s' "$s" | sed 's/ /%s/g')
    dt_adb shell input text "$esc"
    ;;

  key)  dt_adb shell input keyevent "${1:?keycode}" ;;

  swipe)
    dt_adb shell input swipe "${1:?x1}" "${2:?y1}" "${3:?x2}" "${4:?y2}" "${5:-300}"
    sleep 1
    ;;

  scroll)
    dir="${1:-down}"; frac="${2:-0.6}"
    cx=$((DT_SCREEN_W / 2))
    span=$("$PY" -c "print(int($DT_SCREEN_H*$frac))")
    top=$((DT_SCREEN_H / 2 - span / 2)); bot=$((DT_SCREEN_H / 2 + span / 2))
    if [ "$dir" = "down" ]; then
      dt_adb shell input swipe "$cx" "$bot" "$cx" "$top" 300
    else
      dt_adb shell input swipe "$cx" "$top" "$cx" "$bot" 300
    fi
    sleep 1
    ;;

  scroll-to)
    sel="${1:?usage: dev.sh scroll-to <selector>}"; max="${2:-8}"; i=0
    while [ "$i" -lt "$max" ]; do
      if dt_find "$sel" >/dev/null 2>&1; then dt_log "found after $i scroll(s)"; exit 0; fi
      "$0" scroll down 0.6 >/dev/null
      i=$((i+1))
    done
    dt_die "selector never appeared after $max scrolls: $sel"
    ;;

  wait)
    sel="${1:?selector}"; secs="${2:-20}"; i=0
    while [ "$i" -lt "$secs" ]; do
      if out=$(dt_find "$sel" 2>/dev/null); then printf '%s\n' "$out"; exit 0; fi
      sleep 1; i=$((i+1))
    done
    dt_die "timed out after ${secs}s waiting for: $sel"
    ;;

  gone)
    sel="${1:?selector}"; secs="${2:-20}"; i=0
    while [ "$i" -lt "$secs" ]; do
      dt_find "$sel" >/dev/null 2>&1 || { dt_log "gone: $sel"; exit 0; }
      sleep 1; i=$((i+1))
    done
    dt_die "still present after ${secs}s: $sel"
    ;;

  logcat)
    sub="${1:-grab}"; label="${2:-log}"
    case "$sub" in
      start) dt_adb logcat -c; dt_log "logcat cleared" ;;
      grab|stop)
        out="$DT_ARTIFACTS/$(dt_ts)-$label.log"
        pid=$(dt_adb_sh pidof "$DT_APP_ID" | awk '{print $1}')
        if [ -n "$pid" ]; then
          dt_adb logcat -d --pid="$pid" > "$out" 2>/dev/null
        else
          dt_adb logcat -d > "$out" 2>/dev/null
        fi
        printf '%s\n' "$out"
        ;;
    esac
    ;;

  files)
    sub="${1:-}"
    # run-as works on debuggable builds without root.
    dt_adb shell "run-as $DT_APP_ID ls -la files/$sub" 2>&1 | tr -d '\r'
    ;;

  clear-field)
    # Key events only: safe while an accessibility service is under test.
    n="${1:-200}"
    dt_adb shell input keyevent KEYCODE_MOVE_END
    dt_adb shell input keyevent $(printf '67 %.0s' $(seq 1 "$n"))
    ;;

  grant|revoke)
    what="${1:?usage: dev.sh grant|revoke overlay|a11y}"
    case "$what" in
      overlay)
        mode=allow; [ "$cmd" = "revoke" ] && mode=default
        dt_adb shell appops set "$DT_APP_ID" SYSTEM_ALERT_WINDOW "$mode"
        dt_log "overlay: $(dt_adb_sh appops get "$DT_APP_ID" SYSTEM_ALERT_WINDOW)"
        ;;
      a11y)
        # Ask the package manager for our AccessibilityService rather than hardcoding it.
        comp=$(dt_adb_sh cmd package query-services --brief \
                 -a android.accessibilityservice.AccessibilityService \
               | tr -d ' ' | grep "^$DT_APP_ID/" | head -1)
        [ -z "$comp" ] && dt_die "$DT_APP_ID declares no AccessibilityService (is it installed?)"
        case "$comp" in "$DT_APP_ID/."*) comp="$DT_APP_ID/$DT_APP_ID${comp#"$DT_APP_ID/"}" ;; esac

        cur=$(dt_adb_sh settings get secure enabled_accessibility_services)
        [ "$cur" = "null" ] && cur=""
        # Drop our entry in either form, keep everyone else's (e.g. a keyboard's service).
        others=$(printf '%s' "$cur" | tr ':' '\n' | grep -v "^$DT_APP_ID/" | grep -v '^$' | paste -sd: -)
        if [ "$cmd" = "grant" ]; then
          new="${others:+$others:}$comp"
          dt_adb shell settings put secure accessibility_enabled 1
          # Toggle off first. After the app process dies (reinstall, instrumented tests) Android
          # lists the service under "Crashed services" and will not rebind it until it is
          # re-enabled; writing an unchanged value does nothing.
          if [ -n "$others" ]; then
            dt_adb shell settings put secure enabled_accessibility_services "$others"
          else
            dt_adb shell settings delete secure enabled_accessibility_services >/dev/null
          fi
          sleep 1
        else
          new="$others"
        fi
        if [ -z "$new" ]; then
          dt_adb shell settings delete secure enabled_accessibility_services >/dev/null
        else
          dt_adb shell settings put secure enabled_accessibility_services "$new"
        fi
        dt_log "a11y services: $(dt_adb_sh settings get secure enabled_accessibility_services)"
        ;;
      *) dt_die "grant|revoke: use overlay or a11y" ;;
    esac
    ;;

  log)
    tag="${1:?usage: dev.sh log <TAG> [lines]}"
    dt_adb logcat -d -s "$tag" 2>/dev/null | grep -v '^---------' | tail -n "${2:-20}"
    ;;

  *) sed -n '2,40p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'; exit 1 ;;
esac
