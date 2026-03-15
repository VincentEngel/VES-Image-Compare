package com.vincentengelsoftware.androidimagecompare.data.preferences;

import com.vincentengelsoftware.androidimagecompare.constants.ImageResizeOptions;
import com.vincentengelsoftware.androidimagecompare.constants.Status;
import com.vincentengelsoftware.androidimagecompare.ui.compare.CompareModeNames;

public class DefaultSettings {
  public static int MAX_ZOOM = 100;
  public static String COMPARE_MODE = CompareModeNames.SIDE_BY_SIDE;
  public static boolean SYNC_IMAGE_INTERACTIONS = true;
  public static boolean SHOW_EXTENSIONS = true;
  public static int THEME = Status.THEME_SYSTEM;

  public static int IMAGE_RESIZE_OPTION = ImageResizeOptions.RESIZE_OPTION_AUTOMATIC;
  public static int IMAGE_RESIZE_WIDTH = 1024;
  public static int IMAGE_RESIZE_HEIGHT = 1024;
  public static boolean RESET_IMAGE_ON_LINKING = true;
  public static int MIRRORING_TYPE = Status.NATURAL_MIRRORING;

  public static float MIN_ZOOM = 1.0F;

  public static int TAP_HIDE_MODE = Status.TAP_HIDE_MODE_INVISIBLE;

  public static boolean SHOW_NAVIGATION_BAR = true;
}
