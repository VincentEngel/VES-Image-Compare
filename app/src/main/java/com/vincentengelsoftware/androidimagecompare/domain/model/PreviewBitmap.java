package com.vincentengelsoftware.androidimagecompare.domain.model;

import android.graphics.Bitmap;
import com.vincentengelsoftware.androidimagecompare.util.BitmapTransformer;

/**
 * Caches the scaled-down preview bitmap for a single image slot and applies rotation and mirroring
 * on demand.
 *
 * <p>The unmodified scaled base is kept separate from the transformed result so that rotation and
 * mirror can always be recomputed from a clean base, regardless of the order in which the user
 * applied them. Call {@link #invalidate()} whenever the source bitmap changes so the base is
 * re-derived on the next {@link #getSmall} call.
 */
public class PreviewBitmap {

  /**
   * Unmodified scaled-down version of the source bitmap.
   *
   * <p>Kept separate from the transformed result so that rotation and mirror can always be
   * recomputed from a clean base, regardless of the order in which the user applied them.
   */
  private Bitmap scaledBase;

  /**
   * Cached result of the last {@link #getSmall} call. Recycled and replaced whenever the transform
   * settings change.
   */
  private Bitmap cachedTransformed;

  /** Snapshot of the rotation degrees used to produce {@link #cachedTransformed}. */
  private int cachedRotationDegrees = -1;

  /** Snapshot of the mirror flag used to produce {@link #cachedTransformed}. */
  private boolean cachedMirrored;

  /** Clears the cached base and transformed preview so they will be re-derived on the next access. */
  public void invalidate() {
    recycleCachedTransformed();
    scaledBase = null;
  }

  /** Recycles the cached scaled base bitmap and transformed preview, and nulls the references. */
  public void release() {
    recycleCachedTransformed();
    if (scaledBase != null && !scaledBase.isRecycled()) {
      scaledBase.recycle();
    }
    scaledBase = null;
  }

  public void updateFrom(PreviewBitmap other) {
    recycleCachedTransformed();
    this.scaledBase = other.scaledBase;
    this.cachedTransformed = other.cachedTransformed;
    this.cachedRotationDegrees = other.cachedRotationDegrees;
    this.cachedMirrored = other.cachedMirrored;
  }

  /**
   * Returns the preview bitmap with transforms applied in the canonical order <b>scale → rotate →
   * mirror</b>, which is identical to the order used by {@link
   * com.vincentengelsoftware.androidimagecompare.domain.model.ImageInfoHolder#getAdjustedBitmap()}.
   *
   * <p>The scaled base is cached lazily and reused across calls. The transformed result is also
   * cached and only recomputed when the rotation or mirror settings actually change, avoiding
   * repeated bitmap allocations.
   */
  public Bitmap getSmall(ImageSource source, BitmapTransformSettings settings) {
    if (scaledBase == null) {
      scaledBase =
          BitmapTransformer.createScaledBitmapToMaxLength(
              source.bitmap(),
              source.maxSideSizeForSmallBitmap(),
              source.maxSideSizeForSmallBitmap());
    }

    int rotationDegrees = settings.getRotationDegrees();
    boolean mirrored = settings.isMirrored();

    // Return the cached result if the settings have not changed.
    if (cachedTransformed != null
        && !cachedTransformed.isRecycled()
        && cachedRotationDegrees == rotationDegrees
        && cachedMirrored == mirrored) {
      return cachedTransformed;
    }

    // Recycle the previous transformed bitmap before creating a new one.
    recycleCachedTransformed();

    cachedTransformed = BitmapTransformer.applyRotateAndMirror(scaledBase, rotationDegrees, mirrored);

    cachedRotationDegrees = rotationDegrees;
    cachedMirrored = mirrored;

    return cachedTransformed;
  }

  /**
   * Recycles {@link #cachedTransformed} if it is non-null, not yet recycled, and distinct from
   * {@link #scaledBase} (which must be kept alive for future transforms).
   */
  private void recycleCachedTransformed() {
    if (cachedTransformed != null
        && cachedTransformed != scaledBase
        && !cachedTransformed.isRecycled()) {
      cachedTransformed.recycle();
    }
    cachedTransformed = null;
    cachedRotationDegrees = -1;
    cachedMirrored = false;
  }
}
