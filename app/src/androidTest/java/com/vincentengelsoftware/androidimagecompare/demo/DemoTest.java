package com.vincentengelsoftware.androidimagecompare.demo;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.ui.main.MainActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * End-to-end demo test.
 *
 * <p>Walks through every compare mode and exercises each mode's interactive controls (seekbars,
 * direction swap, hide/show, swipe-to-erase, reset buttons) so a screen-recording
 * script can capture a full feature showcase automatically. Run via {@code demo_record.sh} in the
 * project root.
 *
 * <p>To add a new feature to the demo:
 *
 * <ol>
 *   <li>Find the {@code @Test} method {@code demoAllFeatures}.
 *   <li>Add an Espresso {@code onView(…).perform(…)} block at the relevant place.
 *   <li>Add a short {@code pause()} call so the viewer can see the result.
 * </ol>
 */
@RunWith(AndroidJUnit4.class)
public class DemoTest {

  private static final long SUPER_SHORT_PAUSE_MS = 50;
  private static final long SHORT_PAUSE_MS = 1_000;
  private static final long MEDIUM_PAUSE_MS = 2_000;
  private static final long LONG_PAUSE_MS = 3_000;

  private Uri leftImageUri;
  private Uri rightImageUri;

  // ── setup ──────────────────────────────────────────────────────────────

  /**
   * Copies the two demo images from the test APK's assets into the app's cache directory so they
   * have a file:// URI the app can open.
   */
  @Before
  public void setUp() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    leftImageUri = copyAssetToCache(context, "demo/demo_left.jpg", "demo_left.jpg");
    rightImageUri = copyAssetToCache(context, "demo/demo_right.jpg", "demo_right.jpg");
  }


  // ── demo test ──────────────────────────────────────────────────────────

  @Test
  public void demoAllFeatures() throws InterruptedException {

    Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
    intent.setAction(Intent.ACTION_SEND_MULTIPLE);
    intent.setType("image/*");
    intent.putParcelableArrayListExtra(
        Intent.EXTRA_STREAM,
        new java.util.ArrayList<>(java.util.Arrays.asList(leftImageUri, rightImageUri)));
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(intent)) {

      // ── Main screen ───────────────────────────────────────────────
      pause(LONG_PAUSE_MS); // let viewer see the main screen with both images loaded

      demoMainScreenControls();

      demoSideBySide();
      demoOverlaySlide();
      demoOverlayTap();
      demoOverlayCut();
      demoSlideTransparent();
      demoTouchTransparent();
      demoDifferences();

      // ── End of demo ───────────────────────────────────────────────
      // The recording script stops the screenrecord process here.
    }
  }

  // ── 0. Main-screen controls ─────────────────────────────────────────────

  /**
   * Demonstrates the main-screen image controls before entering any compare mode:
   *
   * <ol>
   *   <li>Opens the resize dialog for the right image, confirms "Automatic" is selected, and
   *       dismisses it — so the viewer sees the option without changing anything.
   *   <li>Rotates the right image four times (4 × 90°) with a short pause between each tap so the
   *       viewer can follow each rotation step.
   * </ol>
   */
  private void demoMainScreenControls() throws InterruptedException {
    // ── Resize dialog ────────────────────────────────────────────
    onView(withId(R.id.main_btn_resize_image_right)).perform(click());
    pause(MEDIUM_PAUSE_MS); // let viewer read the options

    onView(withId(R.id.dialog_resize_image_radio_button_automatic)).perform(click());
    pause(SHORT_PAUSE_MS);  // highlight the selection briefly

    onView(withId(R.id.dialog_resize_image_btn_done)).perform(click());
    pause(SHORT_PAUSE_MS);

    // ── Rotate right image 4 × 90° ───────────────────────────────
    for (int i = 0; i < 4; i++) {
      onView(withId(R.id.home_button_rotate_image_right)).perform(click());
      pause(SHORT_PAUSE_MS);
    }
    pause(SHORT_PAUSE_MS); // dwell on the restored orientation before entering compare modes
  }

  // ── 1. Side by Side ─────────────────────────────────────────────────────

  private void demoSideBySide() throws InterruptedException {
    onView(withId(R.id.main_button_compare)).perform(click());
    pause(MEDIUM_PAUSE_MS);
    onView(withId(R.id.select_compare_mode_dialog_btn_side_by_side)).perform(click());
    pause(LONG_PAUSE_MS); // show images side by side

    naturalPinchZoom(R.id.side_by_side_image_top_left);

    pressBack();
    pause(SHORT_PAUSE_MS);
  }

  // ── 2. Overlay Slide ────────────────────────────────────────────────────

  private void demoOverlaySlide() throws InterruptedException {
    onView(withId(R.id.main_button_compare)).perform(click());
    pause(SHORT_PAUSE_MS);
    onView(withId(R.id.select_compare_mode_dialog_btn_overlay_slide)).perform(click());
    pause(MEDIUM_PAUSE_MS);

    // Tap the image to show the controls bar (it auto-hides after 2 s of inactivity)
    onView(withId(R.id.overlay_slide_image_view_base)).perform(click());
    pause(SHORT_PAUSE_MS);

    // Slide all the way to show the base image
    naturalSlideFromTo(10, 90);
    pause(SHORT_PAUSE_MS);
    naturalSlideFromTo(90, 10);
    pause(SHORT_PAUSE_MS);

    // Re-show controls bar so the viewer sees it during the zoom demo
    onView(withId(R.id.overlay_slide_image_view_base)).perform(click());
    pause(SHORT_PAUSE_MS);
    naturalPinchZoom(R.id.overlay_slide_image_view_base);

    pressBack();
    pause(SHORT_PAUSE_MS);
  }

  // ── 3. Overlay Tap ──────────────────────────────────────────────────────

  private void demoOverlayTap() throws InterruptedException {
    onView(withId(R.id.main_button_compare)).perform(click());
    pause(SHORT_PAUSE_MS);
    onView(withId(R.id.select_compare_mode_dialog_btn_overlay_tap)).perform(click());
    pause(LONG_PAUSE_MS);

    // TapHelper makes the clicked view INVISIBLE and the other VISIBLE on each tap.
    // Alternate between the two IDs so Espresso always finds a VISIBLE target.
    onView(withId(R.id.overlay_tap_image_view_one)).perform(click()); // one→INVISIBLE, two→VISIBLE
    pause(MEDIUM_PAUSE_MS);
    onView(withId(R.id.overlay_tap_image_view_two)).perform(click()); // two→INVISIBLE, one→VISIBLE
    pause(MEDIUM_PAUSE_MS);
    onView(withId(R.id.overlay_tap_image_view_one)).perform(click()); // one→INVISIBLE, two→VISIBLE
    pause(SHORT_PAUSE_MS);

    // two is now VISIBLE — pinch zoom on it
    naturalPinchZoom(R.id.overlay_tap_image_view_two);

    pressBack();
    pause(SHORT_PAUSE_MS);
  }

  // ── 4. Slide Transparent ────────────────────────────────────────────────

  private void demoSlideTransparent() throws InterruptedException {
    onView(withId(R.id.main_button_compare)).perform(click());
    pause(SHORT_PAUSE_MS);
    onView(withId(R.id.select_compare_mode_dialog_btn_transparent)).perform(click());
    pause(LONG_PAUSE_MS);

    // Tap the image to show the controls bar (auto-hides after 2 s)
    onView(withId(R.id.overlay_transparent_image_view_base)).perform(click());
    pause(SHORT_PAUSE_MS);

    // Fade the front image to fully transparent (base only)
    naturalSlideFromTo(R.id.overlay_slide_seek_bar, 50, 0);
    pause(MEDIUM_PAUSE_MS);

    // Fade the front image to fully opaque (front only)
    naturalSlideFromTo(R.id.overlay_slide_seek_bar, 0, 100);
    pause(MEDIUM_PAUSE_MS);

    // Return to 50 % blend
    naturalSlideFromTo(R.id.overlay_slide_seek_bar, 100, 50);
    pause(SHORT_PAUSE_MS);

    // Tap before hide / show buttons
    onView(withId(R.id.overlay_transparent_image_view_base)).perform(click());
    onView(withId(R.id.overlay_transparent_button_hide_front_image)).perform(click()); // hide
    pause(MEDIUM_PAUSE_MS);
    onView(withId(R.id.overlay_transparent_image_view_base)).perform(click());
    onView(withId(R.id.overlay_transparent_button_hide_front_image)).perform(click()); // show
    pause(SHORT_PAUSE_MS);

    naturalPinchZoom(R.id.overlay_transparent_image_view_base);

    pressBack();
    pause(SHORT_PAUSE_MS);
  }

  // ── 5. Overlay Cut ──────────────────────────────────────────────────────

  private void demoOverlayCut() throws InterruptedException {
    onView(withId(R.id.main_button_compare)).perform(click());
    pause(SHORT_PAUSE_MS);
    onView(withId(R.id.select_compare_mode_dialog_btn_overlay_cut)).perform(click());
    pause(LONG_PAUSE_MS);

    // Seekbars are always visible (fixed at the edges, no ControlsBarManager)
    naturalSlideFromTo(R.id.full_slider_seekbar_top, 0, 25);
    pause(SHORT_PAUSE_MS);
    naturalSlideFromTo(R.id.full_slider_seekbar_bottom, 0, 25);
    pause(SHORT_PAUSE_MS);
    naturalSlideFromTo(R.id.full_slider_seekbar_left, 0, 25);
    pause(SHORT_PAUSE_MS);
    naturalSlideFromTo(R.id.full_slider_seekbar_right, 0, 25);
    pause(MEDIUM_PAUSE_MS);

    // Tighten the window further so the viewer sees a pronounced cut-out effect
    naturalSlideFromTo(R.id.full_slider_seekbar_top, 25, 40);
    naturalSlideFromTo(R.id.full_slider_seekbar_bottom, 25, 40);
    naturalSlideFromTo(R.id.full_slider_seekbar_left, 25, 40);
    naturalSlideFromTo(R.id.full_slider_seekbar_right, 25, 40);
    pause(MEDIUM_PAUSE_MS);

    // Reset button is VISIBLE with extensions enabled (INVISIBLE without)
    onView(withId(R.id.overlay_cut_btn_reset)).perform(click());
    pause(MEDIUM_PAUSE_MS);

    naturalPinchZoom(R.id.full_slide_image_view_base);

    pressBack();
    pause(SHORT_PAUSE_MS);
  }

  // ── 6. Touch Transparent ────────────────────────────────────────────────

  private void demoTouchTransparent() throws InterruptedException {
    onView(withId(R.id.main_button_compare)).perform(click());
    pause(SHORT_PAUSE_MS);
    onView(withId(R.id.select_compare_mode_dialog_btn_overlay_touch)).perform(click());
    pause(LONG_PAUSE_MS);

    // Controls are VISIBLE with extensions enabled and do NOT auto-hide (no ControlsBarManager)
    // Increase the brush size so erased strokes are clearly visible
    naturalSlideFromTo(R.id.overlay_touch_brush_size, 0, 150);
    pause(SHORT_PAUSE_MS);

    // Swipe across the reveal canvas to erase / expose the base image
    onView(withId(R.id.overlay_touch_reveal_view)).perform(swipeRight());
    pause(SHORT_PAUSE_MS);
    onView(withId(R.id.overlay_touch_reveal_view)).perform(swipeLeft());
    pause(SHORT_PAUSE_MS);
    onView(withId(R.id.overlay_touch_reveal_view)).perform(swipeRight());
    pause(MEDIUM_PAUSE_MS);

    // Switch to a smaller brush and add a fine stroke
    naturalSlideFromTo(R.id.overlay_touch_brush_size, 150, 40);
    pause(SHORT_PAUSE_MS);
    onView(withId(R.id.overlay_touch_reveal_view)).perform(swipeLeft());
    pause(MEDIUM_PAUSE_MS);

    // Reset the reveal mask (restore original front image)
    onView(withId(R.id.overlay_touch_button_reset)).perform(click());
    pause(MEDIUM_PAUSE_MS);

    // Pause erasing so the next swipe pans/zooms instead of erasing
    onView(withId(R.id.overlay_touch_button_erase_toggle)).perform(click());
    pause(SHORT_PAUSE_MS);
    onView(withId(R.id.overlay_touch_reveal_view)).perform(swipeRight()); // pans the image
    pause(MEDIUM_PAUSE_MS);

    // Pinch zoom directly on the ZoomImageView (reveal view passes events through when erase
    // is paused, but targeting the bottom view is more reliable for multi-touch dispatch)
    naturalPinchZoom(R.id.overlay_touch_image_view_bottom);

    onView(withId(R.id.overlay_touch_button_erase_toggle)).perform(click()); // resume erasing
    pause(SHORT_PAUSE_MS);

    pressBack();
    pause(SHORT_PAUSE_MS);
  }

  // ── 7. Differences ──────────────────────────────────────────────────────

  private void demoDifferences() throws InterruptedException {
    onView(withId(R.id.main_button_compare)).perform(click());
    pause(SHORT_PAUSE_MS);
    onView(withId(R.id.select_compare_mode_dialog_btn_differences)).perform(click());
    // The difference bitmap is computed asynchronously — wait for it to finish
    pause(LONG_PAUSE_MS + LONG_PAUSE_MS);

    naturalPinchZoom(R.id.differences_image_top);

    pressBack();
    pause(MEDIUM_PAUSE_MS);
  }

  // ── helpers ────────────────────────────────────────────────────────────

  private static void pause(long ms) throws InterruptedException {
    Thread.sleep(ms);
  }

  private void naturalSlideFromTo(int from, int to) throws InterruptedException {
    naturalSlideFromTo(R.id.overlay_slide_seek_bar, from, to);
  }

  private void naturalSlideFromTo(int viewId, int from, int to) throws InterruptedException {
    int step = from <= to ? 1 : -1;
    for (int i = from; from <= to ? i < to : i > to; i += step) {
      onView(withId(viewId)).perform(setProgress(i));
      pause(SUPER_SHORT_PAUSE_MS);
    }
  }

  /**
   * Sets a {@link SeekBar} to a specific progress value directly on the UI thread.
   *
   * <p>This is more reliable than dragging the thumb with a swipe because it does not depend on
   * the seekbar's exact position on screen. The seekbar's {@code OnSeekBarChangeListener} is still
   * fired, so all downstream effects (image cropping, transparency, etc.) are triggered exactly as
   * if the user had dragged the thumb.
   */
  private static ViewAction setProgress(final int progress) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(SeekBar.class);
      }

      @Override
      public String getDescription() {
        return "Set SeekBar progress to " + progress;
      }

      @Override
      public void perform(UiController uiController, View view) {
        ((SeekBar) view).setProgress(progress);
        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /**
   * Copies an asset from the instrumentation APK into the app's cache dir and returns a {@code
   * file://} URI pointing to the copy.
   */
  private static Uri copyAssetToCache(Context context, String assetPath, String fileName)
      throws Exception {
    Context testContext =
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getContext();
    File outFile = new File(context.getCacheDir(), fileName);
    try (InputStream in = testContext.getAssets().open(assetPath);
        OutputStream out = new FileOutputStream(outFile)) {
      byte[] buf = new byte[4096];
      int len;
      while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
    }
    return Uri.fromFile(outFile);
  }

  /**
   * Zooms in, pans to explore different parts of the image, then zooms back out.
   *
   * <p>After the zoom-in the finger sweeps upper-left → lower-right → centre so the recording
   * shows that the image can be freely panned while zoomed. Each pan leg uses the same sine
   * ease-in-out curve as the pinch so the whole sequence feels like one fluid gesture chain.
   */
  private void naturalPinchZoom(int viewId) throws InterruptedException {
    onView(withId(viewId)).perform(pinchZoom(0.08f, 0.32f, 30)); // zoom in
    pause(SHORT_PAUSE_MS);                                         // let zoom settle

    // Sweep upper-left to show that part of the image
    onView(withId(viewId)).perform(drag(0.50f, 0.50f, 0.30f, 0.37f, 25));
    pause(SHORT_PAUSE_MS);

    // Swing across to lower-right
    onView(withId(viewId)).perform(drag(0.30f, 0.37f, 0.68f, 0.62f, 30));
    pause(SHORT_PAUSE_MS);

    // Glide back to centre
    onView(withId(viewId)).perform(drag(0.68f, 0.62f, 0.50f, 0.50f, 20));
    pause(MEDIUM_PAUSE_MS);

    onView(withId(viewId)).perform(pinchZoom(0.32f, 0.08f, 30)); // zoom out
    pause(SHORT_PAUSE_MS);
  }

  /**
   * Returns a {@link ViewAction} that simulates a smooth single-finger drag on any {@link View}.
   *
   * <p>All four coordinates are expressed as fractions of the view's width/height (e.g.
   * {@code 0.5f, 0.5f} = centre). Motion uses the same sine ease-in-out curve as {@link
   * #pinchZoom} and is paced at 16 ms per frame so it chains seamlessly with a preceding pinch.
   *
   * @param fromXFraction starting X as a fraction of view width
   * @param fromYFraction starting Y as a fraction of view height
   * @param toXFraction   ending X as a fraction of view width
   * @param toYFraction   ending Y as a fraction of view height
   * @param steps         number of {@code ACTION_MOVE} frames (≥ 1)
   */
  private static ViewAction drag(
      final float fromXFraction,
      final float fromYFraction,
      final float toXFraction,
      final float toYFraction,
      final int steps) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Drag from ("
            + fromXFraction + "," + fromYFraction
            + ") to ("
            + toXFraction + "," + toYFraction + ")";
      }

      @Override
      public void perform(UiController uiController, View view) {
        float w = view.getWidth();
        float h = view.getHeight();

        long downTime = SystemClock.uptimeMillis();

        MotionEvent.PointerProperties[] props = new MotionEvent.PointerProperties[1];
        props[0] = new MotionEvent.PointerProperties();
        props[0].id = 0;
        props[0].toolType = MotionEvent.TOOL_TYPE_FINGER;

        MotionEvent.PointerCoords[] coords = new MotionEvent.PointerCoords[1];
        coords[0] = new MotionEvent.PointerCoords();
        coords[0].pressure = 1f;
        coords[0].size = 1f;
        coords[0].x = w * fromXFraction;
        coords[0].y = h * fromYFraction;

        // Finger down
        MotionEvent e =
            MotionEvent.obtain(
                downTime, downTime, MotionEvent.ACTION_DOWN,
                1, props, coords,
                0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
        view.dispatchTouchEvent(e);
        e.recycle();
        uiController.loopMainThreadForAtLeast(16);

        // Move frames — sine ease-in-out
        for (int i = 1; i <= steps; i++) {
          float t = (float) i / steps;
          float eased = (float) (-(Math.cos(Math.PI * t) - 1.0) / 2.0);
          coords[0].x = w * fromXFraction + (w * toXFraction - w * fromXFraction) * eased;
          coords[0].y = h * fromYFraction + (h * toYFraction - h * fromYFraction) * eased;
          e =
              MotionEvent.obtain(
                  downTime, downTime + (long) i * 16, MotionEvent.ACTION_MOVE,
                  1, props, coords,
                  0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
          view.dispatchTouchEvent(e);
          e.recycle();
          uiController.loopMainThreadForAtLeast(16);
        }

        // Finger up
        e =
            MotionEvent.obtain(
                downTime, downTime + (long) steps * 16 + 16, MotionEvent.ACTION_UP,
                1, props, coords,
                0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
        view.dispatchTouchEvent(e);
        e.recycle();
        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /**
   * Returns a {@link ViewAction} that simulates a two-finger pinch gesture on any {@link View}.
   *
   * <p>Both fingers move primarily along the horizontal axis through the view's centre, with a
   * small fixed vertical offset (6 % of view height) so the gesture looks like a real thumb+index
   * pinch rather than a perfectly horizontal ruler-slide.
   *
   * <p>Motion is interpolated with a <em>sine ease-in-out</em> curve so the gesture starts slow,
   * accelerates through the middle, and decelerates to a soft stop — matching the feel of a real
   * finger movement. Each frame is paced at exactly 16 ms (≈ 60 fps) via
   * {@link UiController#loopMainThreadForAtLeast}.
   *
   * <p>The gesture is synthesised as raw {@link MotionEvent}s dispatched directly to the target
   * view, so it works even when the view is partially obscured by a sibling (e.g. an erase overlay
   * on top of a ZoomImageView).
   *
   * @param fromHalfFraction starting half-spread as a fraction of view width (e.g. {@code 0.08f})
   * @param toHalfFraction   ending   half-spread as a fraction of view width (e.g. {@code 0.32f})
   * @param steps            number of {@code ACTION_MOVE} frames (≥ 1); 30 gives ~500 ms travel
   */
  private static ViewAction pinchZoom(
      final float fromHalfFraction, final float toHalfFraction, final int steps) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Pinch zoom from halfFraction="
            + fromHalfFraction
            + " to halfFraction="
            + toHalfFraction;
      }

      @Override
      public void perform(UiController uiController, View view) {
        float cx = view.getWidth() / 2f;
        float cy = view.getHeight() / 2f;
        float w = view.getWidth();
        // Small vertical offset so fingers aren't on an unnaturally perfect horizontal line.
        float vy = view.getHeight() * 0.06f;

        long downTime = SystemClock.uptimeMillis();

        MotionEvent.PointerProperties[] props = new MotionEvent.PointerProperties[2];
        props[0] = new MotionEvent.PointerProperties();
        props[0].id = 0;
        props[0].toolType = MotionEvent.TOOL_TYPE_FINGER;
        props[1] = new MotionEvent.PointerProperties();
        props[1].id = 1;
        props[1].toolType = MotionEvent.TOOL_TYPE_FINGER;

        MotionEvent.PointerCoords[] coords = new MotionEvent.PointerCoords[2];
        coords[0] = new MotionEvent.PointerCoords();
        coords[0].pressure = 1f;
        coords[0].size = 1f;
        coords[1] = new MotionEvent.PointerCoords();
        coords[1].pressure = 1f;
        coords[1].size = 1f;

        // Initial finger positions (finger 0 upper-left, finger 1 lower-right)
        coords[0].x = cx - w * fromHalfFraction;
        coords[0].y = cy - vy;
        coords[1].x = cx + w * fromHalfFraction;
        coords[1].y = cy + vy;

        // Finger 1 down (ACTION_DOWN, single pointer)
        MotionEvent e =
            MotionEvent.obtain(
                downTime, downTime, MotionEvent.ACTION_DOWN,
                1, new MotionEvent.PointerProperties[]{props[0]},
                new MotionEvent.PointerCoords[]{coords[0]},
                0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
        view.dispatchTouchEvent(e);
        e.recycle();

        // Finger 2 down (ACTION_POINTER_DOWN, two pointers)
        int ptr1Down =
            (1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT) | MotionEvent.ACTION_POINTER_DOWN;
        e =
            MotionEvent.obtain(
                downTime, downTime + 16, ptr1Down,
                2, props, coords,
                0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
        view.dispatchTouchEvent(e);
        e.recycle();
        uiController.loopMainThreadForAtLeast(16);

        // Intermediate ACTION_MOVE frames — sine ease-in-out for organic feel
        for (int i = 1; i <= steps; i++) {
          float t = (float) i / steps;
          // Sine ease-in-out: slow start → fast middle → slow end
          float eased = (float) (-(Math.cos(Math.PI * t) - 1.0) / 2.0);
          float half = fromHalfFraction + (toHalfFraction - fromHalfFraction) * eased;
          coords[0].x = cx - w * half;
          coords[0].y = cy - vy;
          coords[1].x = cx + w * half;
          coords[1].y = cy + vy;
          e =
              MotionEvent.obtain(
                  downTime, downTime + 16 + (long) i * 16, MotionEvent.ACTION_MOVE,
                  2, props, coords,
                  0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
          view.dispatchTouchEvent(e);
          e.recycle();
          uiController.loopMainThreadForAtLeast(16);
        }

        long upTime = downTime + 16 + (long) steps * 16;

        // Finger 2 up (ACTION_POINTER_UP)
        int ptr1Up =
            (1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT) | MotionEvent.ACTION_POINTER_UP;
        e =
            MotionEvent.obtain(
                downTime, upTime + 16, ptr1Up,
                2, props, coords,
                0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
        view.dispatchTouchEvent(e);
        e.recycle();

        // Finger 1 up (ACTION_UP, single pointer)
        e =
            MotionEvent.obtain(
                downTime, upTime + 32, MotionEvent.ACTION_UP,
                1, new MotionEvent.PointerProperties[]{props[0]},
                new MotionEvent.PointerCoords[]{coords[0]},
                0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
        view.dispatchTouchEvent(e);
        e.recycle();

        uiController.loopMainThreadUntilIdle();
      }
    };
  }
}
