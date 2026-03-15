package com.vincentengelsoftware.androidimagecompare.ui.compare;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Survives configuration changes (e.g. screen rotation) for {@link OverlayTapActivity}.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Retains the decoded bitmaps across rotation so they are never re-decoded.
 *   <li>Owns the {@link #sync} zoom-link flag so it persists across rotation.
 * </ul>
 */
public class OverlayTapViewModel extends ViewModel {

  // ── Bitmaps (survive rotation) ─────────────────────────────────────────────

  /** The first (initially front) image – decoded once on first launch, never mutated. */
  @Nullable private Bitmap bitmapOne;

  /** The second (initially back) image – decoded once on first launch, never mutated. */
  @Nullable private Bitmap bitmapTwo;

  /**
   * Returns {@code true} when bitmaps have already been loaded (i.e. after a configuration change)
   * so the Activity does not decode them again.
   */
  public boolean areBitmapsLoaded() {
    return bitmapOne != null && bitmapTwo != null;
  }

  /**
   * Stores the decoded bitmaps. Must be called exactly once, on the Activity's first launch.
   *
   * @param bitmapOne the first image
   * @param bitmapTwo the second image
   */
  public void initBitmaps(@NonNull Bitmap bitmapOne, @NonNull Bitmap bitmapTwo) {
    this.bitmapOne = bitmapOne;
    this.bitmapTwo = bitmapTwo;
  }

  /** Returns the first image, or {@code null} before {@link #initBitmaps} is called. */
  @Nullable
  public Bitmap getBitmapOne() {
    return bitmapOne;
  }

  /** Returns the second image, or {@code null} before {@link #initBitmaps} is called. */
  @Nullable
  public Bitmap getBitmapTwo() {
    return bitmapTwo;
  }

  // ── Shared mutable state ───────────────────────────────────────────────────

  /**
   * Whether zoom/pan state is copied to the other image view when the user taps to switch images.
   * Initialised to {@code true}; updated from the Intent on first launch and from {@code
   * savedInstanceState} on rotation.
   */
  private final AtomicBoolean sync = new AtomicBoolean(true);

  /** Returns the shared sync flag used by {@link TapHelper} and {@link SyncZoom}. */
  public AtomicBoolean getSync() {
    return sync;
  }
}
