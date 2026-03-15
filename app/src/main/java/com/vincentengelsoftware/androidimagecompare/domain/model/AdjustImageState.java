package com.vincentengelsoftware.androidimagecompare.domain.model;

import com.vincentengelsoftware.androidimagecompare.constants.ImageResizeOptions;
import java.io.File;

/**
 * Immutable snapshot of the image state at the time the compare-output file was last written.
 *
 * <p>Callers obtain a new instance via {@link #of(BitmapTransformSettings)} after writing the file,
 * or reset to {@link #empty()} to force regeneration on the next check. Use {@link
 * #requiresRecalculation(File, BitmapTransformSettings)} to decide whether the file is still valid.
 */
public record AdjustImageState(
    int savedRotation, int savedResizeOption, int savedCustomHeight, int savedCustomWidth) {
  /** Sentinel instance that always causes {@link #requiresRecalculation} to return {@code true}. */
  public static AdjustImageState empty() {
    return new AdjustImageState(-1, -1, -1, -1);
  }

  /** Captures the current image state into a new snapshot. */
  public static AdjustImageState of(BitmapTransformSettings settings) {
    return new AdjustImageState(
        settings.getCurrentRotation(),
        settings.getResizeOption(),
        settings.getCustomHeight(),
        settings.getCustomWidth());
  }

  public static AdjustImageState of(AdjustImageState adjustImageState) {
    return new AdjustImageState(
        adjustImageState.savedRotation,
        adjustImageState.savedResizeOption,
        adjustImageState.savedCustomHeight,
        adjustImageState.savedCustomWidth);
  }

  /**
   * Returns {@code true} when the compare-output file must be regenerated because the image, its
   * rotation, or its resize settings have changed since the last save, or because the file does not
   * exist yet.
   */
  public boolean requiresRecalculation(File compareFile, BitmapTransformSettings settings) {
    if (!compareFile.exists()) return true;
    if (savedRotation != settings.getCurrentRotation()) return true;
    if (savedResizeOption != settings.getResizeOption()) return true;
    if (settings.getResizeOption() == ImageResizeOptions.RESIZE_OPTION_CUSTOM) {
      return savedCustomHeight != settings.getCustomHeight()
          || savedCustomWidth != settings.getCustomWidth();
    }
    return false;
  }
}
