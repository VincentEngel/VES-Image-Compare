package com.vincentengelsoftware.androidimagecompare.util.imageInformation;

import android.graphics.Bitmap;

import com.vincentengelsoftware.androidimagecompare.globals.ImageResizeOptions;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapHelper;

/**
 * Lazily computes and caches the derived bitmaps (small preview, adjusted-for-compare) from a
 * source image and its transform settings. All cached values are invalidated whenever
 * {@link #invalidate()} is called (e.g. after the source or settings change).
 *
 * <p>Lifecycle:</p>
 * <ol>
 *   <li>Call {@link #buildAdjustedBitmap} once before entering a compare flow.</li>
 *   <li>Call {@link #getSmall} / {@link #getAdjusted} to retrieve cached results.</li>
 *   <li>Call {@link #invalidate()} whenever the source image or its settings change.</li>
 * </ol>
 */
public class ImageBitmapCache {
    private static final int DEGREES_PER_ROTATION_STEP = 90;

    /** Small bitmap used for the preview {@link android.widget.ImageView}. */
    private Bitmap previewBitmap;

    /**
     * The final bitmap ready for the compare activity: scaled to the target size (when a
     * resize mode is active) and rotated. Built by {@link #buildAdjustedBitmap}.
     */
    private Bitmap adjustedBitmap;

    /** Clears all cached bitmaps so they will be recomputed on next access. */
    public void invalidate() {
        previewBitmap = null;
        adjustedBitmap = null;
    }

    /** Clears only the adjusted (resized+rotated) bitmap so it will be rebuilt on next access. */
    public void invalidateResized() {
        adjustedBitmap = null;
    }

    /**
     * Returns a small bitmap suitable for the preview {@link android.widget.ImageView}.
     * Computed lazily and cached until {@link #invalidate()} is called.
     */
    public Bitmap getSmall(ImageSource source) {
        if (previewBitmap == null) {
            previewBitmap = BitmapHelper.createScaledBitmapToMaxLength(
                    source.bitmap(),
                    source.maxSideSizeForSmallBitmap(),
                    source.maxSideSizeForSmallBitmap()
            );
        }
        return previewBitmap;
    }

    /**
     * Rotates the cached preview bitmap by 90° and replaces the cache entry.
     * Should be called after {@link ImageTransformSettings#rotate()} has already
     * incremented the rotation counter.
     */
    public void rotateSmall(ImageSource source) {
        previewBitmap = BitmapHelper.rotateBitmap(getSmall(source), DEGREES_PER_ROTATION_STEP);
    }

    /**
     * Builds and caches the bitmap used for comparison.
     *
     * <p>The pipeline is: <em>scale → rotate</em>. Scaling first is significantly cheaper
     * because the rotation matrix then operates on a much smaller pixel surface.</p>
     *
     * <p>Subsequent calls with an unchanged rotation value and an already-valid cache are
     * no-ops.</p>
     */
    public void buildAdjustedBitmap(ImageSource source, ImageTransformSettings settings) {
        // Step 1: scale the source bitmap to the configured target size.
        Bitmap scaled = scaleSource(source, settings);

        // Step 2: apply rotation to the already-small bitmap.
        adjustedBitmap = BitmapHelper.rotateBitmap(
                scaled,
                DEGREES_PER_ROTATION_STEP * settings.getCurrentRotation()
        );

    }

    /**
     * Returns the final bitmap to pass to the compare activity.
     *
     * <p>{@link #buildAdjustedBitmap} must have been called before entering a compare flow.
     * If it has not been called yet (e.g. in a lazy-access path), this method falls back to
     * building the bitmap on demand.</p>
     */
    public Bitmap getAdjusted(ImageSource source, ImageTransformSettings settings) {
        if (adjustedBitmap == null) {
            buildAdjustedBitmap(source, settings);
        }
        return adjustedBitmap;
    }

    /**
     * Scales {@code source} according to the resize option in {@code settings}.
     * Returns the original bitmap reference when no scaling is requested.
     */
    private static Bitmap scaleSource(ImageSource source, ImageTransformSettings settings) {
        return switch (settings.getResizeOption()) {
            case ImageResizeOptions.RESIZE_OPTION_CUSTOM -> BitmapHelper.resizeBitmap(
                    source.bitmap(),
                    settings.getCustomWidth(),
                    settings.getCustomHeight()
            );
            case ImageResizeOptions.RESIZE_OPTION_AUTOMATIC -> BitmapHelper.createScaledBitmapToMaxLength(
                    source.bitmap(),
                    source.maxSideSize(),
                    source.maxSideSize()
            );
            default -> source.bitmap(); // RESIZE_OPTION_ORIGINAL — keep at full resolution.
        };
    }
}
