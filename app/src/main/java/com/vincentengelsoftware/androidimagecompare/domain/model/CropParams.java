package com.vincentengelsoftware.androidimagecompare.domain.model;

/**
 * Immutable snapshot of the four crop-seekbar states submitted for a single
 * bitmap-cut operation.
 *
 * <p>Passed to {@code BitmapTransformer.cutBitmapAny(Bitmap, CropParams)}
 * instead of nine raw primitive parameters, keeping call sites readable and
 * type-safe.</p>
 */
public record CropParams(
        boolean topActive,
        int     topProgress,
        boolean leftActive,
        int     leftProgress,
        boolean rightActive,
        int     rightProgress,
        boolean bottomActive,
        int     bottomProgress
) {}

