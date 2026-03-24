package com.vincentengelsoftware.androidimagecompare.util;

/**
 * Immutable value object holding the screen-size constraints used when decoding and previewing
 * images.
 *
 * <p>Obtain an instance via {@link DimensionsInitializer#init(android.app.Activity)} once during
 * {@code Activity.onCreate} and pass it wherever it is needed — never store or read global state.
 */
public record Dimensions(int maxSide, int maxSideForPreview) {
  public static final float MAX_SMALL_SIZE_DP = 164.499f;
}
