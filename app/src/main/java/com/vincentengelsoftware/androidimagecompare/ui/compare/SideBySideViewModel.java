package com.vincentengelsoftware.androidimagecompare.ui.compare;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.vincentengelsoftware.androidimagecompare.util.BitmapExtractor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Survives configuration changes (e.g. screen rotation) for {@link SideBySideActivity}.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Decodes both bitmaps on a background thread exactly once and retains them across rotation
 *       via {@link #getImages()} LiveData.
 *   <li>Exposes a load-failure signal via {@link #isLoadFailed()} so the Activity can finish itself
 *       without any threading code of its own.
 *   <li>Owns the {@link #getSync()} zoom-link flag so it is never reset on a configuration change.
 * </ul>
 */
public class SideBySideViewModel extends ViewModel {

  // ── Image pair (survives rotation) ─────────────────────────────────────────

  /** Holds the two decoded bitmaps after a successful load. */
  public record ImagePair(@NonNull Bitmap imageOne, @NonNull Bitmap imageTwo) {}

  private final MutableLiveData<ImagePair> images = new MutableLiveData<>();
  private final MutableLiveData<Boolean> loadFailed = new MutableLiveData<>();

  /**
   * Emits the decoded {@link ImagePair} once both bitmaps are ready. After a configuration change
   * the last value is re-delivered immediately to the new observer, so bitmaps are never
   * re-decoded.
   */
  public LiveData<ImagePair> getImages() {
    return images;
  }

  /**
   * Emits {@code true} if either bitmap could not be decoded. The Activity should call {@link
   * android.app.Activity#finish()} on this signal.
   */
  public LiveData<Boolean> isLoadFailed() {
    return loadFailed;
  }

  // ── Shared sync state ──────────────────────────────────────────────────────

  /**
   * Whether both image views pan/zoom together. Initialised from the launching Intent on first
   * launch; retained by the ViewModel on subsequent configuration changes.
   */
  private final AtomicBoolean sync = new AtomicBoolean(true);

  public AtomicBoolean getSync() {
    return sync;
  }

  // ── Background loading ─────────────────────────────────────────────────────

  /** Single-thread executor used to decode bitmaps off the main thread. */
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  /**
   * Decodes both images on a background thread and posts the result to {@link #getImages()} or
   * {@link #isLoadFailed()}.
   *
   * <p>This method is idempotent: it is a no-op when the images are already loaded (i.e. after a
   * configuration change).
   *
   * @param cr the {@link ContentResolver} to open content URIs
   * @param uriOne URI string for the first (top-left) image
   * @param uriTwo URI string for the second (bottom-right) image
   */
  public void loadImages(
      @NonNull ContentResolver cr, @NonNull String uriOne, @NonNull String uriTwo) {
    if (images.getValue() != null) {
      return; // already loaded – bitmaps survived a configuration change
    }

    executor.execute(
        () -> {
          Bitmap bitmapOne = BitmapExtractor.fromUriString(cr, uriOne);
          Bitmap bitmapTwo = BitmapExtractor.fromUriString(cr, uriTwo);

          if (bitmapOne == null || bitmapTwo == null) {
            loadFailed.postValue(true);
          } else {
            images.postValue(new ImagePair(bitmapOne, bitmapTwo));
          }
        });
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    executor.shutdownNow();
  }
}
