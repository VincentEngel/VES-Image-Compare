package com.vincentengelsoftware.androidimagecompare.domain.model;

/**
 * Immutable snapshot of the four crop-seekbar states submitted for a single bitmap-cut operation.
 *
 * <p>Each edge is represented by a {@link CropEdge} that pairs its active flag with its progress
 * percentage, making call sites self-documenting and type-safe.
 *
 * <p>Passed to {@code BitmapTransformer.cutBitmapAny(Bitmap, CropParams)} instead of eight raw
 * primitive parameters.
 */
public record CropParams(CropEdge top, CropEdge left, CropEdge right, CropEdge bottom) {
}
