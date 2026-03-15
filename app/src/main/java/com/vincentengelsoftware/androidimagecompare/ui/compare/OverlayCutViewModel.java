package com.vincentengelsoftware.androidimagecompare.ui.compare;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.vincentengelsoftware.androidimagecompare.domain.model.CropParams;
import com.vincentengelsoftware.androidimagecompare.util.BitmapTransformer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Survives configuration changes (e.g. screen rotation) for {@link OverlayCutActivity}.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Retains the three bitmap references across rotation.
 *   <li>Exposes the latest cropped front-image frame via {@link #getFrontBitmap()} so the Activity
 *       can observe it without any threading code of its own.
 *   <li>Executes bitmap-cut operations on a background thread and posts results back to the main
 *       thread via {@link LiveData}.
 *   <li>Persists seekbar positions across configuration changes.
 * </ul>
 */
public class OverlayCutViewModel extends ViewModel {

  // ── Bitmaps (survive rotation) ─────────────────────────────────────────────

  /** The base (background) image – decoded once on first launch, never mutated. */
  @Nullable Bitmap bitmapBase;

  /** The original source (front) image – decoded once on first launch, never mutated. */
  @Nullable Bitmap bitmapSource;

  /**
   * Working baseline for the next crop. Starts equal to {@link #bitmapSource}; reset to {@link
   * #bitmapSource} by the Reset button; updated to the current visible crop by the Check button.
   */
  @Nullable Bitmap bitmapAdjusted;

  /**
   * Returns {@code true} when bitmaps have already been loaded (i.e. after a configuration change)
   * so the Activity does not decode them again.
   */
  public boolean areBitmapsLoaded() {
    return bitmapBase != null && bitmapSource != null;
  }

  // ── Live output ────────────────────────────────────────────────────────────

  private final MutableLiveData<Bitmap> frontBitmap = new MutableLiveData<>();

  /**
   * Emits the latest cropped bitmap whenever a crop job completes. Observed by {@link
   * OverlayCutActivity} to update the front image view.
   */
  public LiveData<Bitmap> getFrontBitmap() {
    return frontBitmap;
  }

  // ── Seekbar state ──────────────────────────────────────────────────────────

  private boolean hasSeekBarState = false;
  private int seekBarTopProgress = 0;
  private int seekBarLeftProgress = 90;
  private int seekBarRightProgress = 10;
  private int seekBarBottomProgress = 0;

  public boolean hasSeekBarState() {
    return hasSeekBarState;
  }

  public int getSeekBarTopProgress() {
    return seekBarTopProgress;
  }

  public int getSeekBarLeftProgress() {
    return seekBarLeftProgress;
  }

  public int getSeekBarRightProgress() {
    return seekBarRightProgress;
  }

  public int getSeekBarBottomProgress() {
    return seekBarBottomProgress;
  }

  /**
   * Snapshots the current seekbar positions before a configuration change so that they can be
   * restored when the Activity is recreated.
   */
  public void saveSeekBarState(int top, int left, int right, int bottom) {
    hasSeekBarState = true;
    seekBarTopProgress = top;
    seekBarLeftProgress = left;
    seekBarRightProgress = right;
    seekBarBottomProgress = bottom;
  }

  // ── Background crop ────────────────────────────────────────────────────────

  /**
   * Single-thread executor that serialises crop jobs. Submitting a new job cancels any queued
   * (not-yet-started) predecessor, preventing stale frames from overwriting a more recent result.
   */
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  @Nullable private Future<?> pendingCrop;

  /**
   * Enqueues a bitmap-cut job on the background thread. Any previously <em>pending</em> job that
   * has not started yet is cancelled first.
   *
   * @param source a fresh, mutable copy of the bitmap to cut (modified in-place)
   * @param params the four seekbar states that define the crop geometry
   */
  public void submitCrop(@NonNull Bitmap source, @NonNull CropParams params) {
    cancelPendingCrop();
    pendingCrop =
        executor.submit(
            () -> {
              if (Thread.currentThread().isInterrupted()) {
                return;
              }
              Bitmap result = BitmapTransformer.cutBitmapAny(source, params);
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
