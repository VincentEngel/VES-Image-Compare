package com.vincentengelsoftware.androidimagecompare.constants;

import android.content.Intent;

/**
 * Keys for extras passed between Activities via {@link android.content.Intent}.
 *
 * <p>Non-instantiable – all members are static constants.
 */
public final class IntentExtras {

  private IntentExtras() {
    /* constants class */
  }

  public static final String SYNC_IMAGE_INTERACTIONS = "SYNCED_IMAGE_INTERACTIONS";
  public static final String SHOW_EXTENSIONS = "SHOW_EXTENSIONS";
  public static final String SHOW_NAVIGATION_BAR = "SHOW_NAVIGATION_BAR";
  public static final String TAP_HIDE_MODE = "TAP_HIDE_MODE";
  public static final String MIRRORING_TYPE = "MIRRORING_TYPE";
  public static final String RESET_IMAGE_ON_LINKING = "RESET_IMAGE_ON_LINKING";
  public static final String MAX_ZOOM = "MAX_ZOOM";
  public static final String MIN_ZOOM = "MIN_ZOOM";
  public static final String HAS_HARDWARE_KEY = "HAS_HARDWARE_KEY";
  public static final String IMAGE_URI_ONE = "IMAGE_URI_ONE";
  public static final String IMAGE_URI_TWO = "IMAGE_URI_TWO";
  public static final String IMAGE_NAME_ONE = "IMAGE_NAME_ONE";
  public static final String IMAGE_NAME_TWO = "IMAGE_NAME_TWO";

  private static final int DEFAULT_MAX_ZOOM = 100;
  private static final float DEFAULT_MIN_ZOOM = 1.0F;

  public static int getMaxZoom(Intent intent) {
    return intent.getIntExtra(MAX_ZOOM, DEFAULT_MAX_ZOOM);
  }

  public static float getMinZoom(Intent intent) {
    return intent.getFloatExtra(MIN_ZOOM, DEFAULT_MIN_ZOOM);
  }
}
