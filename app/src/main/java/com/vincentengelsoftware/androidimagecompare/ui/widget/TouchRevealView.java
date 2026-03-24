package com.vincentengelsoftware.androidimagecompare.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A {@link View} that renders the front image and lets the user "erase" areas by touch, revealing
 * whatever is behind this view (i.e. the background image).
 *
 * <h3>Performance notes</h3>
 *
 * <ul>
 *   <li>The scratch object {@link #touchPt} is pre-allocated as an instance field – zero per-touch
 *       heap allocations.
 *   <li>The matrix scale is cached in {@link #cachedScale} and refreshed only when the view size or
 *       zoom state changes.
 *   <li>Historical touch points are consumed to produce smooth, gap-free strokes even during fast
 *       gestures.
 * </ul>
 *
 * <h3>Memory notes</h3>
 *
 * <ul>
 *   <li>The original image is stored as a compact JPEG byte array instead of a full decoded {@link
 *       Bitmap}, cutting the "original" footprint from ~4 MB to ~200 KB for a typical 1 MP image.
 *       {@link #reset()} decodes it on demand.
 *   <li>The bitmap is owned by {@link
 *       com.vincentengelsoftware.androidimagecompare.ui.compare.overlayTouch.ViewModel}; this view
 *       never recycles it. The ViewModel recycles it in {@code onCleared()} when the Activity is
 *       permanently finished.
 *   <li>References are released in {@link #onDetachedFromWindow()}.
 * </ul>
 *
 * <h3>Threading</h3>
 *
 * <ul>
 *   <li>{@link #applyBitmap(byte[], Bitmap)} must be called from the main thread after all heavy
 *       decode/copy work has been done on a worker thread.
 *   <li>All other public methods must be called from the main thread.
 * </ul>
 */
public class TouchRevealView extends View {

  // ── State ──────────────────────────────────────────────────────────────────

  /** Erase-brush radius in <em>view</em> pixels. */
  private float brushRadius = 60f;

  /**
   * When {@code false} touch events are not consumed and fall through to the {@link
   * com.vincentengelsoftware.androidimagecompare.ui.widget.ZoomImageView} underneath, enabling
   * normal zoom/pan without accidentally erasing.
   */
  private boolean erasingEnabled = true;

  /** Working copy of the front image; transparent holes are punched here. */
  @Nullable private Bitmap mutableBitmap;

  /**
   * Compact snapshot of the original image stored as JPEG bytes. Decoded on demand inside {@link
   * #reset()} instead of keeping a full bitmap alive, which would double the memory used by the
   * front image.
   */
  @Nullable private byte[] originalBytes;

  /** Software {@link Canvas} backed by {@link #mutableBitmap}; reused across calls. */
  @Nullable private Canvas bitmapCanvas;

  // ── Paints ─────────────────────────────────────────────────────────────────

  private final Paint erasePaint = buildErasePaint();
  private final Paint drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

  // ── Pre-allocated scratch object (zero per-touch allocations) ──────────────

  /** Reused to map a single touch point into bitmap coordinates. */
  private final float[] touchPt = new float[2];

  // ── Matrix ─────────────────────────────────────────────────────────────────

  /** Maps bitmap pixels → view pixels (center-fit scale + translate). */
  private final Matrix fitMatrix = new Matrix();

  /** Inverse of {@link #fitMatrix}: maps view pixels → bitmap pixels. */
  private final Matrix inverseMatrix = new Matrix();

  /**
   * Cached uniform scale from {@link #fitMatrix}; updated by {@link #rebuildMatrix()} and {@link
   * #syncZoom} so {@link #onTouchEvent} never needs to read the matrix.
   */
  private float cachedScale = 1f;

  // ── Constructors ───────────────────────────────────────────────────────────

  public TouchRevealView(@NonNull Context context) {
    super(context);
  }

  public TouchRevealView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public TouchRevealView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  // ── Public API ─────────────────────────────────────────────────────────────

  /**
   * Assigns the pre-computed front-image data and schedules a repaint. <strong>Must be called from
   * the main thread.</strong>
   *
   * <p>All heavy work (JPEG compression, bitmap copy) must be done by the caller on a worker thread
   * before calling this method.
   *
   * <p>Bitmap ownership stays with {@link
   * com.vincentengelsoftware.androidimagecompare.ui.compare.overlayTouch.OverlayTouchActivity};
   * this view never recycles the bitmap.
   *
   * @param jpegBytes compact JPEG snapshot of the original image, used by {@link #reset()}
   * @param mutableCopy mutable {@link Bitmap.Config#ARGB_8888} copy ready for erase operations
   */
  @MainThread
  public void applyBitmap(@NonNull byte[] jpegBytes, @NonNull Bitmap mutableCopy) {
    // Do NOT recycle the previous mutableBitmap – it is owned by the ViewModel.
    originalBytes = jpegBytes;
    mutableBitmap = mutableCopy;
    bitmapCanvas = new Canvas(mutableCopy);
    rebuildMatrix();
    invalidate();
  }

  /**
   * Sets the erase-brush radius in <em>view</em> pixels.
   *
   * @param radius brush radius; clamped to a minimum of 1
   */
  public void setBrushRadius(float radius) {
    this.brushRadius = Math.max(1f, radius);
  }

  /**
   * Enables or disables erase-on-touch.
   *
   * <p>When {@code false}, {@link #onTouchEvent} returns {@code false} immediately, passing events
   * through to the ZoomImageView below for zoom/pan.
   *
   * @param enabled {@code true} to erase on touch (default); {@code false} to zoom/pan
   */
  public void setErasingEnabled(boolean enabled) {
    this.erasingEnabled = enabled;
  }

  /**
   * Synchronises the render matrix with the zoom/pan state of the background {@link
   * com.vincentengelsoftware.androidimagecompare.ui.widget.ZoomImageView}.
   *
   * <p>Call this from a {@code ZoomImageView.addZoomChangeListener} callback so that the front
   * image always moves in lockstep with the background, even when erasing is paused and the user
   * pans or pinch-zooms.
   *
   * @param zoomFactor zoom multiplier relative to each image's own fit-center scale (1.0 =
   *     fit-center, 2.0 = 2× zoom, …)
   * @param scrollX normalised x-coordinate of the bitmap pixel shown at the view centre (0 = left
   *     edge, 0.5 = centre, 1 = right edge)
   * @param scrollY normalised y-coordinate of the bitmap pixel shown at the view centre (0 = top
   *     edge, 0.5 = centre, 1 = bottom edge)
   */
  public void syncZoom(float zoomFactor, float scrollX, float scrollY) {
    if (mutableBitmap == null || getWidth() == 0 || getHeight() == 0) return;

    float bw = mutableBitmap.getWidth();
    float bh = mutableBitmap.getHeight();
    float vw = getWidth();
    float vh = getHeight();

    // Scale relative to this bitmap's own fit-centre, then apply the caller's zoom factor.
    float fitScale = Math.min(vw / bw, vh / bh);
    float totalScale = fitScale * zoomFactor;

    // Position the image so that the given normalised bitmap coordinate is at the view centre.
    float dx = vw / 2f - scrollX * bw * totalScale;
    float dy = vh / 2f - scrollY * bh * totalScale;

    fitMatrix.reset();
    fitMatrix.setScale(totalScale, totalScale);
    fitMatrix.postTranslate(dx, dy);
    fitMatrix.invert(inverseMatrix);

    // totalScale is already the uniform scale value – no need to read it back from the matrix.
    cachedScale = totalScale;

    invalidate();
  }

  /**
   * Restores the front image to its original state.
   *
   * <p>The original pixels are decoded from the stored JPEG byte array and redrawn into the
   * existing {@link #mutableBitmap} via the cached {@link #bitmapCanvas}, avoiding any new bitmap
   * or canvas allocation.
   */
  public void reset() {
    if (originalBytes == null || mutableBitmap == null || bitmapCanvas == null) return;

    Bitmap restored = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.length);
    if (restored == null) return;

    // Reuse the existing bitmapCanvas — no new Canvas allocation.
    bitmapCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
    bitmapCanvas.drawBitmap(restored, 0, 0, null);
    restored.recycle();

    invalidate();
  }

  // ── View overrides ─────────────────────────────────────────────────────────

  @Override
  protected void onSizeChanged(int w, int h, int oldW, int oldH) {
    super.onSizeChanged(w, h, oldW, oldH);
    rebuildMatrix();
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    // Intentionally no super.onDraw() – View's implementation is an empty no-op.
    if (mutableBitmap != null) {
      canvas.drawBitmap(mutableBitmap, fitMatrix, drawPaint);
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (mutableBitmap == null || bitmapCanvas == null || !erasingEnabled) return false;

    int action = event.getActionMasked();
    if (action != MotionEvent.ACTION_DOWN
        && action != MotionEvent.ACTION_MOVE
        && action != MotionEvent.ACTION_POINTER_DOWN) {
      return true; // consume but do nothing for UP / CANCEL
    }

    float radiusInBitmap = brushRadius / cachedScale;

    // Process historical positions first for smooth, gap-free strokes
    // even during fast gestures — no extra allocation needed.
    int historySize = event.getHistorySize();
    for (int h = 0; h < historySize; h++) {
      for (int i = 0; i < event.getPointerCount(); i++) {
        eraseAt(event.getHistoricalX(i, h), event.getHistoricalY(i, h), radiusInBitmap);
      }
    }

    // Current positions.
    for (int i = 0; i < event.getPointerCount(); i++) {
      eraseAt(event.getX(i), event.getY(i), radiusInBitmap);
    }

    invalidate();
    return true;
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    // Do NOT recycle mutableBitmap here.  Ownership belongs to the ViewModel, which
    // keeps it alive across configuration changes and recycles it in onCleared() only
    // when the Activity is permanently finished.
    mutableBitmap = null;
    bitmapCanvas = null;
    originalBytes = null;
  }

  // ── Private helpers ────────────────────────────────────────────────────────

  /**
   * Erases a circle at the given <em>view</em> coordinates, reusing the pre-allocated {@link
   * #touchPt} scratch array.
   */
  private void eraseAt(float viewX, float viewY, float radiusInBitmap) {
    if (mutableBitmap == null || bitmapCanvas == null) return;

    touchPt[0] = viewX;
    touchPt[1] = viewY;
    inverseMatrix.mapPoints(touchPt);

    float bx = touchPt[0];
    float by = touchPt[1];

    // Use >= so a centre exactly at the bitmap edge (one unit past the last pixel) is
    // also rejected; the canvas clips partial circles at the edges automatically.
    if (bx < 0 || by < 0 || bx >= mutableBitmap.getWidth() || by >= mutableBitmap.getHeight()) {
      return;
    }

    bitmapCanvas.drawCircle(bx, by, radiusInBitmap, erasePaint);
  }

  private static Paint buildErasePaint() {
    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    return p;
  }

  /**
   * Recomputes the center-fit matrices and caches the scale factor. Uses {@link Matrix#reset()} on
   * the existing final fields — no new allocations.
   */
  private void rebuildMatrix() {
    if (mutableBitmap == null || getWidth() == 0 || getHeight() == 0) return;

    float bw = mutableBitmap.getWidth();
    float bh = mutableBitmap.getHeight();
    float vw = getWidth();
    float vh = getHeight();

    float scale = Math.min(vw / bw, vh / bh);
    float dx = (vw - bw * scale) / 2f;
    float dy = (vh - bh * scale) / 2f;

    fitMatrix.reset();
    fitMatrix.setScale(scale, scale);
    fitMatrix.postTranslate(dx, dy);
    fitMatrix.invert(inverseMatrix);

    // scale is already the uniform scale value – no need to read it back from the matrix.
    cachedScale = scale;
  }
}
