package com.vincentengelsoftware.androidimagecompare.ui.compare.differences;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.vincentengelsoftware.androidimagecompare.constants.Status;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Survives configuration changes for {@link DifferencesActivity}.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Owns the single-thread executor so it is never recreated on rotation.
 *   <li>Runs the image-difference pipeline exactly once and caches the two annotated bitmaps.
 *   <li>Exposes {@link #getProcessingState()} so the Activity can react to state transitions
 *       without duplicating logic.
 *   <li>Recycles bitmaps and shuts down the executor in {@link #onCleared()} (called only when the
 *       Activity is truly finishing, not on rotation).
 * </ul>
 */
public class ViewModel extends AndroidViewModel {

  // ── Processing state ───────────────────────────────────────────────────────

  /** Lifecycle of the difference-detection job. */
  public enum ProcessingState {
    /** Job has been submitted and is running. */
    PROCESSING,
    /**
     * Annotated bitmaps are ready; call {@link #getAnnotatedOne()} / {@link #getAnnotatedTwo()}.
     */
    DONE,
    /** An unrecoverable error occurred. */
    ERROR
  }

  private final MutableLiveData<ProcessingState> processingState = new MutableLiveData<>();

  // ── Cached results ─────────────────────────────────────────────────────────

  /** Written on the executor thread; read on the UI thread only after {@code DONE} is posted. */
  @Nullable private volatile Bitmap annotatedOne;

  @Nullable private volatile Bitmap annotatedTwo;

  // ── Infrastructure ─────────────────────────────────────────────────────────

  /** Sync-zoom flag; retains user's last choice across rotation. */
  private final AtomicBoolean sync = new AtomicBoolean(true);

  /**
   * Single executor shared across configuration changes. One thread is sufficient — the two images
   * are processed sequentially.
   */
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  /**
   * Set to {@code true} in {@link #onCleared()} so that a still-running task can discard its result
   * instead of storing and posting to destroyed observers.
   */
  private final AtomicBoolean cleared = new AtomicBoolean(false);

  // ── Algorithm constants ────────────────────────────────────────────────────

  /** Minimum total per-pixel colour delta (|ΔR|+|ΔG|+|ΔB|, 0–765) to flag a pixel as different. */
  private static final int DIFF_THRESHOLD = 45;

  /** Minimum connected-blob area (pixels) to count as a meaningful region. */
  private static final int MIN_BLOB_SIZE = 50;

  // ── Constructor ────────────────────────────────────────────────────────────

  public ViewModel(@NonNull Application application) {
    super(application);
  }

  // ── Public API ─────────────────────────────────────────────────────────────

  public LiveData<ProcessingState> getProcessingState() {
    return processingState;
  }

  public AtomicBoolean getSync() {
    return sync;
  }

  @Nullable
  public Bitmap getAnnotatedOne() {
    return annotatedOne;
  }

  @Nullable
  public Bitmap getAnnotatedTwo() {
    return annotatedTwo;
  }

  /** Returns {@code true} if processing has already been started (running or finished). */
  public boolean isProcessingStarted() {
    return processingState.getValue() != null;
  }

  /**
   * Submits the image-difference pipeline to the executor. Safe to call multiple times — subsequent
   * calls are ignored when already started.
   *
   * @param uriOne URI of the first compare image
   * @param uriTwo URI of the second compare image
   * @param maxDifferences maximum number of circles to draw
   * @param circleColor resolved {@link Color} int for the annotation circles
   */
  public void startProcessing(Uri uriOne, Uri uriTwo, int maxDifferences, int circleColor) {
    if (isProcessingStarted()) return;
    processingState.setValue(ProcessingState.PROCESSING);

    executor.submit(
        () -> {
          try {
            Bitmap[] result = processImages(uriOne, uriTwo, maxDifferences, circleColor);
            if (cleared.get()) {
              // ViewModel was cleared while we were running; discard to avoid leaking bitmaps.
              result[0].recycle();
              result[1].recycle();
            } else {
              annotatedOne = result[0];
              annotatedTwo = result[1];
              processingState.postValue(ProcessingState.DONE);
            }
          } catch (Exception e) {
            if (!cleared.get()) {
              processingState.postValue(ProcessingState.ERROR);
            }
          }
        });
  }

  // ── Lifecycle ──────────────────────────────────────────────────────────────

  @Override
  protected void onCleared() {
    cleared.set(true);
    executor.shutdownNow();
    recycleBitmaps();
  }

  // ── Processing pipeline ────────────────────────────────────────────────────

  /**
   * Loads both bitmaps, normalises dimensions, detects difference regions, and returns two mutable
   * annotated copies. Called exclusively on the executor thread.
   */
  private Bitmap[] processImages(Uri uriOne, Uri uriTwo, int maxDifferences, int circleColor)
      throws IOException {

    // Decode directly as mutable — avoids an extra copy() call later.
    Bitmap bmp1 = loadMutableBitmap(uriOne);
    Bitmap bmp2 = loadMutableBitmap(uriTwo);

    if (bmp1 == null || bmp2 == null) {
      if (bmp1 != null) bmp1.recycle();
      if (bmp2 != null) bmp2.recycle();
      throw new IOException("Failed to decode one or both bitmaps");
    }

    // Normalise: scale bmp2 to bmp1's dimensions for pixel-accurate comparison.
    if (bmp1.getWidth() != bmp2.getWidth() || bmp1.getHeight() != bmp2.getHeight()) {
      Bitmap scaled = Bitmap.createScaledBitmap(bmp2, bmp1.getWidth(), bmp1.getHeight(), true);
      bmp2.recycle();
      // createScaledBitmap may return an immutable bitmap — ensure mutability for canvas drawing.
      if (scaled.isMutable()) {
        bmp2 = scaled;
      } else {
        bmp2 = scaled.copy(Bitmap.Config.ARGB_8888, true);
        scaled.recycle();
      }
    }

    int width = bmp1.getWidth();
    int height = bmp1.getHeight();
    int total = width * height;

    int[] pixels1 = new int[total];
    int[] pixels2 = new int[total];
    bmp1.getPixels(pixels1, 0, width, 0, 0, width, height);
    bmp2.getPixels(pixels2, 0, width, 0, 0, width, height);

    boolean[] diffMask = buildDiffMask(pixels1, pixels2);
    List<Rect> topRegions = findTopRegions(diffMask, width, height, maxDifferences);

    // Draw directly onto the already-mutable decoded bitmaps — no extra copy needed.
    drawCircles(bmp1, topRegions, circleColor);
    drawCircles(bmp2, topRegions, circleColor);

    return new Bitmap[] {bmp1, bmp2};
  }

  /**
   * Decodes a bitmap directly as mutable ({@link BitmapFactory.Options#inMutable inMutable=true}),
   * avoiding the extra {@code copy()} call that would otherwise be needed before drawing.
   */
  @Nullable
  private Bitmap loadMutableBitmap(Uri uri) {
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inMutable = true;
    opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
    try (InputStream stream = getApplication().getContentResolver().openInputStream(uri)) {
      if (stream == null) return null;
      return BitmapFactory.decodeStream(stream, null, opts);
    } catch (IOException e) {
      return null;
    }
  }

  /** Flags pixels whose channel-sum delta exceeds {@link #DIFF_THRESHOLD}. */
  private static boolean[] buildDiffMask(int[] pixels1, int[] pixels2) {
    boolean[] mask = new boolean[pixels1.length];
    for (int i = 0; i < pixels1.length; i++) {
      int p1 = pixels1[i];
      int p2 = pixels2[i];
      int delta =
          Math.abs(Color.red(p1) - Color.red(p2))
              + Math.abs(Color.green(p1) - Color.green(p2))
              + Math.abs(Color.blue(p1) - Color.blue(p2));
      mask[i] = delta > DIFF_THRESHOLD;
    }
    return mask;
  }

  /**
   * BFS connected-component labelling (8-connectivity). Returns the bounding rectangles of the
   * {@code maxDifferences} largest blobs with area ≥ {@link #MIN_BLOB_SIZE}, sorted largest-first.
   */
  private static List<Rect> findTopRegions(
      boolean[] mask, int width, int height, int maxDifferences) {

    boolean[] visited = new boolean[mask.length];
    List<int[]> blobs = new ArrayList<>(); // each entry: { size, minX, minY, maxX, maxY }
    Deque<Integer> queue = new ArrayDeque<>();

    for (int i = 0; i < mask.length; i++) {
      if (!mask[i] || visited[i]) continue;

      int minX = i % width, maxX = minX;
      int minY = i / width, maxY = minY;
      int size = 0;

      visited[i] = true;
      queue.offer(i);

      while (!queue.isEmpty()) {
        int idx = queue.removeFirst();
        int x = idx % width;
        int y = idx / width;
        size++;
        if (x < minX) minX = x;
        if (x > maxX) maxX = x;
        if (y < minY) minY = y;
        if (y > maxY) maxY = y;

        for (int dy = -1; dy <= 1; dy++) {
          for (int dx = -1; dx <= 1; dx++) {
            if (dx == 0 && dy == 0) continue;
            int nx = x + dx;
            int ny = y + dy;
            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
              int nIdx = ny * width + nx;
              if (mask[nIdx] && !visited[nIdx]) {
                visited[nIdx] = true;
                queue.offer(nIdx);
              }
            }
          }
        }
      }

      if (size >= MIN_BLOB_SIZE) {
        blobs.add(new int[] {size, minX, minY, maxX, maxY});
      }
    }

    blobs.sort((a, b) -> b[0] - a[0]);

    List<Rect> result = new ArrayList<>(Math.min(maxDifferences, blobs.size()));
    for (int i = 0; i < Math.min(maxDifferences, blobs.size()); i++) {
      int[] blob = blobs.get(i);
      result.add(new Rect(blob[1], blob[2], blob[3], blob[4]));
    }
    return result;
  }

  /** Draws a circle enclosing each bounding rectangle onto the mutable {@code bitmap}. */
  private static void drawCircles(Bitmap bitmap, List<Rect> regions, int circleColor) {
    if (regions.isEmpty()) return;
    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setColor(circleColor);
    paint.setStyle(Paint.Style.STROKE);
    int smaller = Math.min(bitmap.getWidth(), bitmap.getHeight());
    paint.setStrokeWidth(Math.max(4f, smaller / 150f));
    int padding = Math.max(10, smaller / 80);
    for (Rect r : regions) {
      float cx = (r.left + r.right) / 2f;
      float cy = (r.top + r.bottom) / 2f;
      float radius = Math.max((r.right - r.left) / 2f, (r.bottom - r.top) / 2f) + padding;
      canvas.drawCircle(cx, cy, radius, paint);
    }
  }

  /** Maps a {@link Status} colour constant to an Android {@link Color} int. */
  public static int resolveCircleColor(int colorConstant) {
    return switch (colorConstant) {
      case Status.DIFF_CIRCLE_COLOR_BLUE -> Color.BLUE;
      case Status.DIFF_CIRCLE_COLOR_GREEN -> Color.GREEN;
      default -> Color.RED;
    };
  }

  private void recycleBitmaps() {
    Bitmap one = annotatedOne;
    if (one != null && !one.isRecycled()) {
      one.recycle();
      annotatedOne = null;
    }
    Bitmap two = annotatedTwo;
    if (two != null && !two.isRecycled()) {
      two.recycle();
      annotatedTwo = null;
    }
  }
}
