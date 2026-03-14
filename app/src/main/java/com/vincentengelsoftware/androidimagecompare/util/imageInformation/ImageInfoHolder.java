package com.vincentengelsoftware.androidimagecompare.util.imageInformation;

import android.graphics.Bitmap;
import android.widget.ImageView;


import com.vincentengelsoftware.androidimagecompare.globals.ImageResizeOptions;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapTransformer;

import java.io.File;

/**
 * Coordinates the four focused sub-components that together represent a loaded image and
 * its processing state:
 *
 * <ul>
 *   <li>{@link ImageSource}            – raw bitmap + display-size constraints</li>
 *   <li>{@link BitmapTransformSettings} – user-configured resize mode and rotation</li>
 *   <li>{@link PreviewBitmap}       – lazily-computed derived bitmaps (preview, resized+rotated)</li>
 *   <li>{@link AdjustImageState}      – dirty-tracking to avoid unnecessary re-encoding</li>
 * </ul>
 */
public class ImageInfoHolder {
    private ImageSource source = new ImageSource(null, null, 0, 0);
    private final BitmapTransformSettings transformSettings = new BitmapTransformSettings();
    private final PreviewBitmap previewBitmap = new PreviewBitmap();
    private AdjustImageState stateSaver = AdjustImageState.empty();

    private static final int DEGREES_PER_ROTATION_STEP = 90;

    /**
     * Initialises this holder from a freshly decoded bitmap. All cached bitmaps and the
     * save-state snapshot are cleared so the image is treated as new.
     */
    public void updateFromBitmap(Bitmap bitmap, int maxSideSize, int maxSideSizeForSmallBitmap, String imageName) {
        source = new ImageSource(bitmap, imageName, maxSideSize, maxSideSizeForSmallBitmap);
        transformSettings.reset();
        previewBitmap.invalidate();
        stateSaver = AdjustImageState.empty();
    }

    /**
     * Copies all state from another holder into this one (used by the swap-images feature).
     */
    public void updateFromImageHolder(ImageInfoHolder other) {
        source = new ImageSource(other.source.bitmap(), other.source.imageName(), other.source.maxSideSize(), other.source.maxSideSizeForSmallBitmap());
        transformSettings.copyFrom(other.transformSettings);
        previewBitmap.updateFrom(other.previewBitmap);
        stateSaver = AdjustImageState.of(other.stateSaver);
    }

    public void setResizeOption(int resizeOption) {
        transformSettings.setResizeOption(resizeOption);
        stateSaver = AdjustImageState.empty();
    }

    public void setCustomSize(int height, int width) {
        transformSettings.setCustomSize(height, width);
        stateSaver = AdjustImageState.empty();
    }

    public Bitmap getBitmap() {
        return source.bitmap();
    }

    public String getImageName() {
        return source.imageName();
    }

    /** Returns the small preview bitmap, computing it lazily if necessary. */
    public Bitmap getBitmapSmall() {
        return previewBitmap.getSmall(source);
    }

    /** Clears the cached resized bitmap so it will be recomputed on next access. */
    public void resetBitmapResized() {
        stateSaver = AdjustImageState.empty();
    }

    /**
     * Returns the bitmap to pass to the compare activity: the resized variant when a
     * resize mode is active, otherwise the plain rotated bitmap.
     */
    public Bitmap getAdjustedBitmap() {
        // Step 1: scale the source bitmap to the configured target size.
        Bitmap scaled = scaleSource(source, transformSettings);

        // Step 2: apply rotation to the already-small bitmap.
        return BitmapTransformer.rotateBitmap(
                scaled,
                DEGREES_PER_ROTATION_STEP * transformSettings.getCurrentRotation()
        );
    }

    /**
     * Increments the rotation by 90° and updates the cached small preview bitmap.
     */
    public void rotatePreviewImage() {
        transformSettings.rotate();
        previewBitmap.rotateSmall(source);
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
        return stateSaver.requiresRecalculation(compareFile, transformSettings);
    }

    /**
     * Records the current image state so that subsequent calls to {@link #requiresRecalculation}
     * can detect whether anything has changed.
     */
    public void markSaved() {
        stateSaver = AdjustImageState.of(transformSettings);
    }

    /**
     * Scales {@code source} according to the resize option in {@code settings}.
     * Returns the original bitmap reference when no scaling is requested.
     */
    private static Bitmap scaleSource(ImageSource source, BitmapTransformSettings settings) {
        return switch (settings.getResizeOption()) {
            case ImageResizeOptions.RESIZE_OPTION_CUSTOM -> BitmapTransformer.resizeBitmap(
                    source.bitmap(),
                    settings.getCustomWidth(),
                    settings.getCustomHeight()
            );
            case ImageResizeOptions.RESIZE_OPTION_AUTOMATIC -> BitmapTransformer.createScaledBitmapToMaxLength(
                    source.bitmap(),
                    source.maxSideSize(),
                    source.maxSideSize()
            );
            default -> source.bitmap(); // RESIZE_OPTION_ORIGINAL — keep at full resolution.
        };
    }
}
