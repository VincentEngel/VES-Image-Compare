package com.vincentengelsoftware.androidimagecompare.ui.compare.overlaySlide;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.vincentengelsoftware.androidimagecompare.util.BitmapTransformer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Survives configuration changes (e.g. screen rotation) for {@link OverlaySlideActivity}.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Retains the decoded bitmaps across rotation so they are never re-decoded.
 *   <li>Owns the {@link #leftToRight} slide-direction and {@link #sync} zoom-link state.
 *   <li>Executes bitmap-crop operations on a background thread and posts the result back to the
 *       main thread via {@link #getFrontBitmap()} LiveData.
 * </ul>
 */
public class ViewModel extends androidx.lifecycle.ViewModel {

  // ── Bitmaps (survive rotation) ─────────────────────────────────────────────

  /** The base (background) image – decoded once on first launch, never mutated. */
  @Nullable Bitmap bitmapBase;

  /** The full source (front) image – decoded once on first launch, never mutated. */
  @Nullable private Bitmap bitmapSource;

  /**
   * A fully-transparent bitmap with the same dimensions as {@link #bitmapSource}. Reused as the
   * compositing background on every crop, avoiding repeated allocations.
   */
  @Nullable private Bitmap transparentBitmap;

  /**
   * Returns {@code true} when bitmaps have already been loaded (i.e. after a configuration change)
   * so the Activity does not decode them again.
   */
  public boolean areBitmapsLoaded() {
    return bitmapBase != null && bitmapSource != null;
  }

  /**
   * Stores the decoded bitmaps and pre-allocates the transparent compositing bitmap. Must be called
   * exactly once, on the Activity's first launch.
   */
  public void initBitmaps(@NonNull Bitmap base, @NonNull Bitmap source) {
    this.bitmapBase = base;
    this.bitmapSource = source;
    this.transparentBitmap =
        BitmapTransformer.createTransparentBitmap(source.getWidth(), source.getHeight());
  }

  // ── Shared mutable state ───────────────────────────────────────────────────

  /** Whether both image views pan/zoom together. */
  private final AtomicBoolean sync = new AtomicBoolean(true);

  /** Slide direction: {@code true} = left-to-right, {@code false} = right-to-left. */
  private final AtomicBoolean leftToRight = new AtomicBoolean(true);

  public AtomicBoolean getSync() {
    return sync;
  }

  public AtomicBoolean getLeftToRight() {
    return leftToRight;
  }

  // ── Live output ────────────────────────────────────────────────────────────

  private final MutableLiveData<Bitmap> frontBitmap = new MutableLiveData<>();

  /**
   * Emits the latest cropped bitmap whenever a crop job completes. Observed by {@link
   * OverlaySlideActivity} to update the front image view.
   */
  public LiveData<Bitmap> getFrontBitmap() {
    return frontBitmap;
  }

  // ── Background crop ────────────────────────────────────────────────────────

  /**
   * Single-thread executor that serialises crop jobs. Submitting a new job cancels any queued
   * predecessor, preventing stale frames from overwriting a more recent result.
   */
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  @Nullable private Future<?> pendingCrop;

  /**
   * Enqueues a bitmap-crop job on the background thread. Any previously pending job that has not
   * started yet is cancelled first.
   *
   * @param progress the seekbar progress (0–100) representing the visible width fraction
   */
  public void submitCrop(int progress) {
    if (bitmapSource == null || transparentBitmap == null) return;

    final boolean ltr = leftToRight.get();
    final int width = bitmapSource.getWidth() * progress / 100;

    cancelPendingCrop();
    pendingCrop =
        executor.submit(
            () -> {
              if (Thread.currentThread().isInterrupted()) return;

              Bitmap result =
                  BitmapTransformer.getCutBitmapWithTransparentBackgroundWithCanvas(
                      bitmapSource, transparentBitmap, width, ltr);

              if (!Thread.currentThread().isInterrupted()) {
                frontBitmap.postValue(result);
              }
            });
  }

  /** Cancels the last submitted crop job if it has not yet started. Safe to call at any time. */
  public void cancelPendingCrop() {
    if (pendingCrop != null && !pendingCrop.isDone()) {
      pendingCrop.cancel(true);
    }
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    executor.shutdownNow();
  }
}
