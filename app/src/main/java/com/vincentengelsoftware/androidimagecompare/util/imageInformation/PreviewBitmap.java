package com.vincentengelsoftware.androidimagecompare.util.imageInformation;

import android.graphics.Bitmap;

import com.vincentengelsoftware.androidimagecompare.helper.BitmapTransformer;

public class PreviewBitmap {
    private static final int DEGREES_PER_ROTATION_STEP = 90;

    /** Small bitmap used for the preview {@link android.widget.ImageView}. */
    private Bitmap previewBitmap;

    /** Clears all cached bitmaps so they will be recomputed on next access. */
    public void invalidate() {
        previewBitmap = null;
    }

    public void updateFrom(PreviewBitmap other) {
        this.previewBitmap = other.previewBitmap;
    }

    /**
     * Returns a small bitmap suitable for the preview {@link android.widget.ImageView}.
     * Computed lazily and cached until {@link #invalidate()} is called.
     */
    public Bitmap getSmall(ImageSource source) {
        if (previewBitmap == null) {
            previewBitmap = BitmapTransformer.createScaledBitmapToMaxLength(
                    source.bitmap(),
                    source.maxSideSizeForSmallBitmap(),
                    source.maxSideSizeForSmallBitmap()
            );
        }

        return previewBitmap;
    }

    /**
     * Rotates the cached preview bitmap by 90° and replaces the cache entry.
     * Should be called after {@link BitmapTransformSettings#rotate()} has already
     * incremented the rotation counter.
     */
    public void rotateSmall(ImageSource source) {
        previewBitmap = BitmapTransformer.rotateBitmap(getSmall(source), DEGREES_PER_ROTATION_STEP);
    }
}
