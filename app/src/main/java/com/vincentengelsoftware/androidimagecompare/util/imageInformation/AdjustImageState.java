package com.vincentengelsoftware.androidimagecompare.util.imageInformation;

import android.graphics.Bitmap;

import com.vincentengelsoftware.androidimagecompare.globals.ImageResizeOptions;

import java.io.File;

/**
 * Immutable snapshot of the image state at the time the compare-output file was last written.
 * <p>
 * Callers obtain a new instance via {@link #of(ImageSource, ImageTransformSettings)} after
 * writing the file, or reset to {@link #empty()} to force regeneration on the next check.
 * Use {@link #requiresRecalculation(File, ImageSource, ImageTransformSettings)} to decide whether the
 * file is still valid.
 */
public record AdjustImageState(
        Bitmap savedBitmapRef,
        int savedRotation,
        int savedResizeOption,
        int savedCustomHeight,
        int savedCustomWidth
) {
    /** Sentinel instance that always causes {@link #requiresRecalculation} to return {@code true}. */
    public static AdjustImageState empty() {
        return new AdjustImageState(null, -1, -1, -1, -1);
    }

    /** Captures the current image state into a new snapshot. */
    public static AdjustImageState of(ImageSource source, ImageTransformSettings settings) {
        return new AdjustImageState(
                source.bitmap(),
                settings.getCurrentRotation(),
                settings.getResizeOption(),
                settings.getCustomHeight(),
                settings.getCustomWidth()
        );
    }

    /**
     * Returns {@code true} when the compare-output file must be regenerated because the
     * image, its rotation, or its resize settings have changed since the last save, or
     * because the file does not exist yet.
     */
    public boolean requiresRecalculation(File compareFile, ImageSource source, ImageTransformSettings settings) {
        if (!compareFile.exists()) return true;
        if (savedBitmapRef != source.bitmap()) return true;
        if (savedRotation != settings.getCurrentRotation()) return true;
        if (savedResizeOption != settings.getResizeOption()) return true;
        if (settings.getResizeOption() == ImageResizeOptions.RESIZE_OPTION_CUSTOM) {
            return savedCustomHeight != settings.getCustomHeight()
                    || savedCustomWidth != settings.getCustomWidth();
        }
        return false;
    }
}
