package com.vincentengelsoftware.androidimagecompare.ui.compare;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.constants.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.constants.Settings;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityOverlayTouchBinding;
import com.vincentengelsoftware.androidimagecompare.ui.util.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.ui.widget.ImageScaleCenter;
import com.vincentengelsoftware.androidimagecompare.util.BitmapExtractor;
import java.io.ByteArrayOutputStream;

/**
 * Displays two images stacked on top of each other and lets the user "paint away" the front image
 * by touching the screen, revealing the background image underneath.
 *
 * <p>The background image is shown inside a {@link
 * com.vincentengelsoftware.androidimagecompare.ui.widget.ZoomImageView} so the user can still
 * zoom/pan it. The front image is rendered by a {@link TouchRevealView} which intercepts touch
 * events and erases circular areas at each touch point.
 *
 * <p>All UI state (erasing enabled, brush size) and the erased bitmap itself are retained across
 * configuration changes via {@link OverlayTouchViewModel}, so rotating the screen preserves the
 * current erase marks, seekbar position, and pause/resume status.
 *
 * <p>Controls (shown when {@code SHOW_EXTENSIONS} is {@code true}):
 *
 * <ul>
 *   <li>Reset button – restores the front image to its original state.
 *   <li>Brush-size seekbar – adjusts the erase radius in real time.
 * </ul>
 */
public class OverlayTouchActivity extends AppCompatActivity {

  private OverlayTouchViewModel viewModel;

  private ActivityOverlayTouchBinding binding;

  // ── Lifecycle ──────────────────────────────────────────────────────────────

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    viewModel = new ViewModelProvider(this).get(OverlayTouchViewModel.class);

    FullScreenHelper.apply(getWindow(), Settings.SHOW_NAVIGATION_BAR);

    binding = ActivityOverlayTouchBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    if (!initImages()) {
      finish();
      return;
    }

    initControls();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  // ── Initialisation ─────────────────────────────────────────────────────────

  /**
   * Reads the two image URIs from the Intent, sets the background image on the {@link
   * com.vincentengelsoftware.androidimagecompare.ui.widget.ZoomImageView} and either restores the
   * front image from the ViewModel (after a configuration change) or kicks off a background thread
   * to decode it for the first time.
   *
   * <p>Storing the bitmap in the ViewModel <em>before</em> posting to the UI thread ensures it is
   * available even if the Activity is destroyed mid-flight, so the next recreation can restore it
   * immediately without re-decoding.
   *
   * @return {@code false} if either URI string is absent; the caller should {@link #finish()}.
   */
  private boolean initImages() {
    String uriStringOne = getIntent().getStringExtra(IntentExtras.IMAGE_URI_ONE);
    String uriStringTwo = getIntent().getStringExtra(IntentExtras.IMAGE_URI_TWO);

    if (uriStringOne == null
        || uriStringOne.isEmpty()
        || uriStringTwo == null
        || uriStringTwo.isEmpty()) {
      return false;
    }

    // Background image — ZoomImageView handles this via its own internal path.
    binding.overlayTouchImageViewBottom.setImageURI(Uri.parse(uriStringOne));

    // Capture the view reference now; accessing 'binding' from a worker thread is unsafe.
    TouchRevealView revealView = binding.overlayTouchRevealView;

    // Keep the front (TouchRevealView) in sync whenever the background is panned / zoomed.
    // The listener fires only when erasing is paused and the user interacts with the
    // ZoomImageView, so there is no overhead during normal erase operation.
    binding.overlayTouchImageViewBottom.addZoomChangeListener(
        () -> {
          if (binding == null) return;
          ImageScaleCenter isc = binding.overlayTouchImageViewBottom.getImageScaleCenter();
          revealView.syncZoom(isc.scale(), isc.centerX(), isc.centerY());
        });

    if (viewModel.hasBitmap()) {
      // Configuration change (e.g. rotation): restore the erased bitmap directly
      // from the ViewModel on the main thread — no decode needed.
      // hasBitmap() guarantees both fields are non-null and the bitmap is not recycled.
      byte[] bytes = viewModel.getOriginalBytes();
      Bitmap bmp = viewModel.getMutableBitmap();
      if (bytes != null && bmp != null) {
        revealView.applyBitmap(bytes, bmp);
      }
    } else {
      // First launch: decode the front image on a worker thread.
      new Thread(
              () -> {
                // ── All heavy work on this worker thread ─────────────────────────────
                Bitmap source = BitmapExtractor.fromUriString(getContentResolver(), uriStringTwo);
                if (source == null) {
                  runOnUiThread(this::finish);
                  return;
                }

                // Compress the original to JPEG bytes (used by reset()).
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                source.compress(Bitmap.CompressFormat.JPEG, 95, baos);
                byte[] jpegBytes = baos.toByteArray();

                // Create the mutable working copy; recycle source immediately after.
                Bitmap mutableCopy = source.copy(Bitmap.Config.ARGB_8888, true);
                source.recycle();

                // Store in the ViewModel BEFORE posting to the UI thread so that if the
                // Activity is destroyed between now and the post executing, the next
                // recreation will find hasBitmap() == true and restore immediately.
                viewModel.storeBitmap(jpegBytes, mutableCopy);

                // ── Only lightweight field assignment on the main thread ──────────────
                runOnUiThread(
                    () -> {
                      if (binding == null) {
                        // Activity destroyed mid-decode; the ViewModel holds the bitmap
                        // for the next recreation — do NOT recycle it here.
                        return;
                      }
                      revealView.applyBitmap(jpegBytes, mutableCopy);
                    });
              },
              "TouchReveal-decode")
          .start();
    }

    return true;
  }

  private void initControls() {
    // ── Restore persisted state from the ViewModel ─────────────────────────
    boolean erasingEnabled = viewModel.isErasingEnabled();
    binding.overlayTouchRevealView.setErasingEnabled(erasingEnabled);
    binding.overlayTouchButtonEraseToggle.setImageResource(
        erasingEnabled ? R.drawable.pause_circle : R.drawable.play_circle);

    int savedProgress = viewModel.getBrushProgress();
    binding.overlayTouchBrushSize.setProgress(savedProgress);
    binding.overlayTouchRevealView.setBrushRadius(Math.max(1, savedProgress));

    // ── Wire up controls ───────────────────────────────────────────────────

    // Erase-mode toggle: tap to pause (play_circle) / resume (pause_circle).
    binding.overlayTouchButtonEraseToggle.setOnClickListener(
        v -> {
          boolean nowErasing = !viewModel.isErasingEnabled();
          viewModel.setErasingEnabled(nowErasing);
          binding.overlayTouchRevealView.setErasingEnabled(nowErasing);
          binding.overlayTouchButtonEraseToggle.setImageResource(
              nowErasing ? R.drawable.pause_circle : R.drawable.play_circle);
        });

    // Reset button restores the front image to its original unmodified state.
    binding.overlayTouchButtonReset.setOnClickListener(v -> binding.overlayTouchRevealView.reset());

    // Brush-size seekbar maps 1..200 view-pixels to the erase radius.
    binding.overlayTouchBrushSize.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(@NonNull SeekBar seekBar, int progress, boolean fromUser) {
            int radius = Math.max(1, progress);
            viewModel.setBrushProgress(progress);
            binding.overlayTouchRevealView.setBrushRadius(radius);
          }

          @Override
          public void onStartTrackingTouch(@NonNull SeekBar seekBar) {}

          @Override
          public void onStopTrackingTouch(@NonNull SeekBar seekBar) {}
        });

    // Show or hide the controls bar according to the user preference.
    binding.overlayTouchExtensions.setVisibility(
        getIntent().getBooleanExtra(IntentExtras.SHOW_EXTENSIONS, false)
            ? View.VISIBLE
            : View.GONE);
  }
}
