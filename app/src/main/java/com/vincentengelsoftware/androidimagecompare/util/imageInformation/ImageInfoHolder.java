package com.vincentengelsoftware.androidimagecompare.util.imageInformation;

import android.graphics.Bitmap;
import android.widget.ImageView;


import java.io.File;

/**
 * Coordinates the four focused sub-components that together represent a loaded image and
 * its processing state:
 *
 * <ul>
 *   <li>{@link ImageSource}            – raw bitmap + display-size constraints</li>
 *   <li>{@link ImageTransformSettings} – user-configured resize mode and rotation</li>
 *   <li>{@link ImageBitmapCache}       – lazily-computed derived bitmaps (preview, resized+rotated)</li>
 *   <li>{@link AdjustImageState}      – dirty-tracking to avoid unnecessary re-encoding</li>
 * </ul>
 */
public class ImageInfoHolder {
    private ImageSource source = new ImageSource(null, null, 0, 0);
    private final ImageTransformSettings transform = new ImageTransformSettings();
    private final ImageBitmapCache cache = new ImageBitmapCache();
    private AdjustImageState stateSaver = AdjustImageState.empty();

    /**
     * Initialises this holder from a freshly decoded bitmap. All cached bitmaps and the
     * save-state snapshot are cleared so the image is treated as new.
     */
    public void updateFromBitmap(Bitmap bitmap, int maxSideSize, int maxSideSizeForSmallBitmap, String imageName) {
        source = new ImageSource(bitmap, imageName, maxSideSize, maxSideSizeForSmallBitmap);
        transform.reset();
        cache.invalidate();
        stateSaver = AdjustImageState.empty();
    }

    /**
     * Copies all state from another holder into this one (used by the swap-images feature).
     */
    public void updateFromImageHolder(ImageInfoHolder other) {
        source = new ImageSource(other.source.bitmap(), other.source.imageName(), other.source.maxSideSize(), other.source.maxSideSizeForSmallBitmap());
        transform.copyFrom(other.transform);
        cache.invalidate(); // Let the cache rebuild lazily from the copied source & transform.
        stateSaver = AdjustImageState.empty();
    }

    public void setResizeOption(int resizeOption) {
        transform.setResizeOption(resizeOption);
        stateSaver = AdjustImageState.empty();
    }

    public void setCustomSize(int height, int width) {
        transform.setCustomSize(height, width);
        stateSaver = AdjustImageState.empty();
    }

    public Bitmap getBitmap() {
        return source.bitmap();
    }

    public String getImageName() {
        return source.imageName();
    }

    /**
     * Computes the final bitmap for comparison by scaling first, then rotating.
     * The result is cached; subsequent calls with unchanged state are no-ops.
     */
    public void buildAdjustedBitmap() {
        cache.buildAdjustedBitmap(source, transform);
    }

    /** Returns the small preview bitmap, computing it lazily if necessary. */
    public Bitmap getBitmapSmall() {
        return cache.getSmall(source);
    }

    /** Clears the cached resized bitmap so it will be recomputed on next access. */
    public void resetBitmapResized() {
        cache.invalidateResized();
    }

    /**
     * Returns the bitmap to pass to the compare activity: the resized variant when a
     * resize mode is active, otherwise the plain rotated bitmap.
     */
    public Bitmap getAdjustedBitmap() {
        return cache.getAdjusted(source, transform);
    }

    /**
     * Increments the rotation by 90° and updates the cached small preview bitmap.
     */
    public void rotatePreviewImage() {
        transform.rotate();
        cache.rotateSmall(source);
    }

    public void updateImageViewPreviewImage(ImageView imageView) {
        imageView.setImageBitmap(getBitmapSmall());
    }

    /**
     * Returns {@code true} when the compare-output file must be regenerated because the
     * image, its rotation, or its resize settings have changed since the last save, or
     * because the file does not exist yet.
     */
    public boolean requiresRecalculation(File compareFile) {
        return stateSaver.requiresRecalculation(compareFile, source, transform);
    }

    /**
     * Records the current image state so that subsequent calls to {@link #requiresRecalculation}
     * can detect whether anything has changed.
     */
    public void markSaved() {
        stateSaver = AdjustImageState.of(source, transform);
    }
}
