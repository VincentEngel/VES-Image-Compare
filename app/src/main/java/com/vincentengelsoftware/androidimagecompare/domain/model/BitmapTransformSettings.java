package com.vincentengelsoftware.androidimagecompare.domain.model;

import com.vincentengelsoftware.androidimagecompare.constants.ImageResizeOptions;

/**
 * Holds the user-configured transform settings for an image: the resize mode and, when using custom
 * mode, the target dimensions.
 */
public class BitmapTransformSettings {
  private static final int DEGREES_PER_ROTATION_STEP = 90;

  private int resizeOption = ImageResizeOptions.RESIZE_OPTION_AUTOMATIC;
  private int customHeight;
  private int customWidth;
  private int currentRotation = 0;
  private boolean mirrored = false;

  public void copyFrom(BitmapTransformSettings other) {
    this.resizeOption = other.resizeOption;
    this.customHeight = other.customHeight;
    this.customWidth = other.customWidth;
    this.currentRotation = other.currentRotation;
    this.mirrored = other.mirrored;
  }

  public void reset() {
    this.currentRotation = 0;
    this.mirrored = false;
    // resizeOption, customHeight and customWidth are preserved — they are user settings,
    // not image-specific, and are re-applied from UserSettings after a reset.
  }

  public void rotate() {
    currentRotation = (currentRotation + 1) % 4;
  }

  public void toggleMirror() {
    mirrored = !mirrored;
  }

  public boolean isMirrored() {
    return mirrored;
  }

  public int getResizeOption() {
    return resizeOption;
  }

  public void setResizeOption(int resizeOption) {
    this.resizeOption = resizeOption;
  }

  public int getCustomHeight() {
    return customHeight;
  }

  public int getCustomWidth() {
    return customWidth;
  }

  public void setCustomSize(int height, int width) {
    this.customHeight = height;
    this.customWidth = width;
  }

  public int getCurrentRotation() {
    return currentRotation;
  }

  /**
   * Returns the current rotation expressed in degrees (multiples of 90°), suitable for passing
   * directly to bitmap transform utilities.
   */
  public int getRotationDegrees() {
    return currentRotation * DEGREES_PER_ROTATION_STEP;
  }
}
