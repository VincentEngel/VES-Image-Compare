#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# demo_record.sh
#
# Usage:
#   ./demo_record.sh              # records to ./demo.mp4
#   ./demo_record.sh my_demo.mp4  # records to ./my_demo.mp4
#
# What it does:
#   1. Verifies a device/emulator is connected.
#   2. Starts a background screen recording on the device.
#   3. Runs only DemoTest via Gradle (connectedDebugAndroidTest).
#   4. Stops the recording and pulls the .mp4 to your machine.
#
# Recommended AVD for best quality:
#   Device : Pixel 9 Pro XL
#   Screen : 6.8 inch  |  1344 × 2992 px  |  ~486 ppi
#   API    : 36 (Android 16)
#   (Create via Android Studio → Device Manager → "Pixel 9 Pro XL" template)
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

OUTPUT_FILE="${1:-demo.mp4}"
DEVICE_MP4="/sdcard/demo.mp4"
RECORD_PID=""

# ── helpers ───────────────────────────────────────────────────────────────────
log()  { echo "[demo_record] $*"; }
die()  { echo "[demo_record] ERROR: $*" >&2; exit 1; }

# ── locate adb ────────────────────────────────────────────────────────────────
# Use adb from PATH if available; otherwise probe the standard SDK locations.
if ! command -v adb &>/dev/null; then
    for candidate in \
        "$HOME/Android/Sdk/platform-tools/adb" \
        "$HOME/Library/Android/sdk/platform-tools/adb" \
        "/opt/android-sdk/platform-tools/adb" \
        "/usr/local/lib/android/sdk/platform-tools/adb"
    do
        if [[ -x "$candidate" ]]; then
            export PATH="$(dirname "$candidate"):$PATH"
            log "Found adb at $candidate"
            break
        fi
    done
fi
command -v adb &>/dev/null || die "adb not found. Add \$ANDROID_HOME/platform-tools to your PATH."

# ── locate JAVA_HOME ──────────────────────────────────────────────────────────
if [[ -z "${JAVA_HOME:-}" ]] || ! command -v java &>/dev/null; then
    # Build a list of candidates; glob entries are expanded by the shell.
    java_candidates=(
        # JetBrains Toolbox – flat path (newer Toolbox versions)
        "$HOME/.local/share/JetBrains/Toolbox/apps/android-studio/jbr"
        # JetBrains Toolbox – versioned directory (older Toolbox versions)
        "$HOME"/.local/share/JetBrains/Toolbox/apps/android-studio/ch-0/*/jbr
        "$HOME"/.local/share/JetBrains/Toolbox/apps/android-studio/ch-*/*/jbr
        "$HOME"/.local/share/JetBrains/Toolbox/apps/AndroidStudio/ch-0/*/jbr
        "$HOME"/.local/share/JetBrains/Toolbox/apps/AndroidStudio/ch-*/*/jbr
        # Android Studio installed directly
        /usr/local/android-studio/jbr
        /opt/android-studio/jbr
        "$HOME/android-studio/jbr"
        # Common system JDKs
        /usr/lib/jvm/java-21-openjdk-amd64
        /usr/lib/jvm/java-17-openjdk-amd64
        /usr/lib/jvm/temurin-21-amd64
        /usr/lib/jvm/temurin-17-amd64
    )
    for candidate in "${java_candidates[@]}"; do
        if [[ -x "$candidate/bin/java" ]]; then
            export JAVA_HOME="$candidate"
            export PATH="$JAVA_HOME/bin:$PATH"
            log "Found Java at $candidate"
            break
        fi
    done
fi
command -v java &>/dev/null || die "Java not found. Set JAVA_HOME or install a JDK (17+)."

restore_ui_mode() {
    if [[ -n "${ORIG_UI_MODE:-}" ]]; then
        adb shell cmd uimode night "$ORIG_UI_MODE" 2>/dev/null || true
        log "UI mode restored to: $ORIG_UI_MODE"
    fi
}

restore_show_touches() {
    if [[ -n "${ORIG_SHOW_TOUCHES:-}" ]]; then
        adb shell settings put system show_touches "$ORIG_SHOW_TOUCHES" 2>/dev/null || true
        log "Show touches restored to: $ORIG_SHOW_TOUCHES"
    fi
}

restore_animations() {
    # Restore original animation scales (saves and restores the values that were set before the demo)
    if [[ -n "${ORIG_WINDOW_ANIM:-}" ]]; then
        adb shell settings put global window_animation_scale    "$ORIG_WINDOW_ANIM"    2>/dev/null || true
        adb shell settings put global transition_animation_scale "$ORIG_TRANSITION_ANIM" 2>/dev/null || true
        adb shell settings put global animator_duration_scale    "$ORIG_ANIMATOR_ANIM"   2>/dev/null || true
        log "Animation scales restored."
    fi
}

stop_recording() {
    # Signal the device-side screenrecord process (SIGINT causes it to finalise
    # and flush the MP4 — SIGKILL would leave the file corrupt).
    adb shell pkill -2 screenrecord 2>/dev/null || true
    sleep 2  # give screenrecord time to flush
    # Clean up the local adb shell wrapper that was backgrounded.
    if [[ -n "$RECORD_PID" ]] && kill -0 "$RECORD_PID" 2>/dev/null; then
        kill "$RECORD_PID" 2>/dev/null || true
        wait "$RECORD_PID" 2>/dev/null || true
    fi
    RECORD_PID=""
}

cleanup() {
    if [[ -n "$RECORD_PID" ]]; then
        log "Stopping screen recording (pid $RECORD_PID)…"
        stop_recording
    fi
    restore_animations
    restore_ui_mode
    restore_show_touches
}
trap cleanup EXIT

# ── 1. Check device ───────────────────────────────────────────────────────────
if ! adb devices | grep -v "^List" | grep -q "device$"; then
    die "No device/emulator connected. Start an AVD or plug in a device."
fi
log "Device found."

# ── 1b. Disable animations so auto-hiding controls bars snap in instantly ─────
# Save current values so cleanup() can restore them.
ORIG_WINDOW_ANIM=$(adb shell settings get global window_animation_scale 2>/dev/null | tr -d '[:space:]')
ORIG_TRANSITION_ANIM=$(adb shell settings get global transition_animation_scale 2>/dev/null | tr -d '[:space:]')
ORIG_ANIMATOR_ANIM=$(adb shell settings get global animator_duration_scale 2>/dev/null | tr -d '[:space:]')
adb shell settings put global window_animation_scale    0.0
adb shell settings put global transition_animation_scale 0.0
adb shell settings put global animator_duration_scale   0.0
log "Animations disabled."

# ── 1c. Enable dark mode ───────────────────────────────────────────────────────
ORIG_UI_MODE=$(adb shell cmd uimode night 2>/dev/null | awk '{print $NF}' | tr -d '[:space:]')
adb shell cmd uimode night yes
log "Dark mode enabled (was: $ORIG_UI_MODE)."

# ── 1d. Show touch indicators ─────────────────────────────────────────────────
ORIG_SHOW_TOUCHES=$(adb shell settings get system show_touches 2>/dev/null | tr -d '[:space:]')
adb shell settings put system show_touches 1
log "Show touches enabled (was: $ORIG_SHOW_TOUCHES)."

# ── 2. Start screen recording in the background ───────────────────────────────log "Starting screen recording on device → $DEVICE_MP4"
adb shell "screenrecord --bit-rate 16000000 --size 1080x2400 --time-limit 300 $DEVICE_MP4" &
RECORD_PID=$!
sleep 1   # give screenrecord time to initialise before the test starts

# ── 3. Run only DemoTest ──────────────────────────────────────────────────────
DEMO_CLASS="com.vincentengelsoftware.androidimagecompare.demo.DemoTest"
log "Running $DEMO_CLASS …"
./gradlew connectedDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class="$DEMO_CLASS" \
    --info

# ── 4. Stop recording ─────────────────────────────────────────────────────────
log "Stopping screen recording…"
stop_recording

# ── 5. Pull the video ─────────────────────────────────────────────────────────
log "Pulling recording → $OUTPUT_FILE"
adb pull "$DEVICE_MP4" "$OUTPUT_FILE"
adb shell rm -f "$DEVICE_MP4"

log "Done! Video saved to: $OUTPUT_FILE"

