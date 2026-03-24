package com.vincentengelsoftware.androidimagecompare.ui.compare.overlayTouch;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Survives configuration changes (e.g. screen rotation) for the Overlay Touch compare mode.
 *
 * <p>Owns the front-image bitmap so that the erased state is preserved across rotations. The bitmap
 * lifecycle is managed here: it is recycled in {@link #onCleared()} when the Activity is
 * permanently finished, never when it is merely re-created.
 *
 * <p>Also persists the two UI states that Android's default view-state saving does not cover:
 *
 * <ul>
 *   <li>Erase toggle (enabled / paused)
 *   <li>Brush-size seekbar progress
 * </ul>
 */
public class ViewModel extends androidx.lifecycle.ViewModel {

  // ── Front image state ──────────────────────────────────────────────────────

  /**
   * Mutable copy of the front image; transparent holes are punched here by {@link
   * com.vincentengelsoftware.androidimagecompare.ui.widget.TouchRevealView}. {@code null} until the
   * background decode finishes.
   */
  @Nullable private Bitmap mutableBitmap;

  /**
   * Compact JPEG snapshot of the original front image used by {@link
   * com.vincentengelsoftware.androidimagecompare.ui.widget.TouchRevealView#reset()}.
   */
  @Nullable private byte[] originalBytes;

  // ── UI state ───────────────────────────────────────────────────────────────

  /** Whether the erase brush is active ({@code true}) or paused ({@code false}). */
  private boolean erasingEnabled = true;

  /**
   * Last seekbar progress value (range 1–200). Default matches {@code android:progress="60"} in the
   * layout XML.
   */
  private int brushProgress = 60;

  // ── Bitmap accessors ───────────────────────────────────────────────────────

  /**
   * Returns {@code true} if a valid (non-recycled) bitmap is already held. Used by the Activity to
   * decide whether to re-decode or restore directly.
   */
  public boolean hasBitmap() {
    return mutableBitmap != null && !mutableBitmap.isRecycled();
  }

  @Nullable
  public Bitmap getMutableBitmap() {
    return mutableBitmap;
  }

  @Nullable
  public byte[] getOriginalBytes() {
    return originalBytes;
  }

  /**
   * Stores the decoded bitmap and its JPEG snapshot. Must be called from the background decode
   * thread before posting to the UI thread, so that the ViewModel already holds the data if the
   * Activity is destroyed mid-flight.
   */
  public void storeBitmap(@NonNull byte[] jpegBytes, @NonNull Bitmap bitmap) {
    originalBytes = jpegBytes;
    mutableBitmap = bitmap;
  }

  // ── UI state accessors ─────────────────────────────────────────────────────

  public boolean isErasingEnabled() {
    return erasingEnabled;
  }

  public void setErasingEnabled(boolean enabled) {
    erasingEnabled = enabled;
  }

  public int getBrushProgress() {
    return brushProgress;
  }

  public void setBrushProgress(int progress) {
    brushProgress = progress;
  }

  // ── Lifecycle ──────────────────────────────────────────────────────────────

  @Override
  protected void onCleared() {
    super.onCleared();
    // The Activity is permanently gone – safe to release the bitmap now.
    if (mutableBitmap != null && !mutableBitmap.isRecycled()) {
      mutableBitmap.recycle();
    }
    mutableBitmap = null;
    originalBytes = null;
  }
}
