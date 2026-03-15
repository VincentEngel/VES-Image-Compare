package com.vincentengelsoftware.androidimagecompare.domain.model;

import android.graphics.Bitmap;

/**
 * Holds the raw (source) image data and the display-size constraints that never change for a given
 * loaded image.
 */
public record ImageSource(
    Bitmap bitmap, String imageName, int maxSideSize, int maxSideSizeForSmallBitmap) {}
