package com.vincentengelsoftware.androidimagecompare.data.preferences;

import com.vincentengelsoftware.androidimagecompare.constants.ImageResizeOptions;
import com.vincentengelsoftware.androidimagecompare.constants.Status;
import com.vincentengelsoftware.androidimagecompare.ui.main.CompareModeNames;

/**
 * Compile-time defaults for every user-configurable setting.
 *
 * <p>All constants are {@code public static final} to guarantee immutability. Call sites in {@link
 * UserSettings} and {@link ImageResizeSettings} reference these values as fallbacks when no
 * persisted value exists yet.
 */
public final class DefaultSettings {

  private DefaultSettings() {}

  public static final int MAX_ZOOM = 100;
  public static final String COMPARE_MODE = CompareModeNames.SIDE_BY_SIDE;
  public static final boolean SYNC_IMAGE_INTERACTIONS = true;
  public static final boolean SHOW_EXTENSIONS = true;
  public static final int THEME = Status.THEME_SYSTEM;

  public static final int IMAGE_RESIZE_OPTION = ImageResizeOptions.RESIZE_OPTION_AUTOMATIC;
  public static final int IMAGE_RESIZE_WIDTH = 1024;
  public static final int IMAGE_RESIZE_HEIGHT = 1024;
  public static final boolean RESET_IMAGE_ON_LINKING = true;
  public static final int MIRRORING_TYPE = Status.NATURAL_MIRRORING;

  public static final float MIN_ZOOM = 1.0F;

  public static final int TAP_HIDE_MODE = Status.TAP_HIDE_MODE_INVISIBLE;

  public static final boolean SHOW_NAVIGATION_BAR = true;

  public static final int DIFFERENCES_MAX_COUNT = 10;
  public static final int DIFFERENCES_CIRCLE_COLOR = Status.DIFF_CIRCLE_COLOR_RED;
}
