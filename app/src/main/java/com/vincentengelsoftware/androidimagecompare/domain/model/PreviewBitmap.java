package com.vincentengelsoftware.androidimagecompare.domain.model;

import android.graphics.Bitmap;
import com.vincentengelsoftware.androidimagecompare.util.BitmapTransformer;

public class PreviewBitmap {
  private static final int DEGREES_PER_ROTATION_STEP = 90;

  /**
   * Unmodified scaled-down version of the source bitmap.
   *
   * <p>Kept separate from the transformed result so that rotation and mirror can always be
   * recomputed from a clean base, regardless of the order in which the user applied them.
   */
  private Bitmap scaledBase;

  /** Clears the cached base so it will be re-derived from source on the next access. */
  public void invalidate() {
    scaledBase = null;
  }

  public void updateFrom(PreviewBitmap other) {
    this.scaledBase = other.scaledBase;
  }

  /**
   * Returns the preview bitmap with transforms applied in the canonical order <b>scale → rotate →
   * mirror</b>, which is identical to the order used by {@link
   * com.vincentengelsoftware.androidimagecompare.domain.model.ImageInfoHolder#getAdjustedBitmap()}.
   *
   * <p>The scaled base is cached lazily and reused across calls; rotation and mirror are recomputed
   * from it on every call so the result is always consistent with the current {@link
   * BitmapTransformSettings}, regardless of the order in which individual operations were applied
   * interactively.
   */
  public Bitmap getSmall(ImageSource source, BitmapTransformSettings settings) {
    if (scaledBase == null) {
      scaledBase =
          BitmapTransformer.createScaledBitmapToMaxLength(
              source.bitmap(),
              source.maxSideSizeForSmallBitmap(),
              source.maxSideSizeForSmallBitmap());
    }
    Bitmap rotated =
        BitmapTransformer.rotateBitmap(
            scaledBase, DEGREES_PER_ROTATION_STEP * settings.getCurrentRotation());
    return settings.isMirrored() ? BitmapTransformer.mirrorBitmap(rotated) : rotated;
  }
}
