package com.vincentengelsoftware.androidimagecompare.constants;

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
  public static final String HAS_HARDWARE_KEY = "HAS_HARDWARE_KEY";
  public static final String IMAGE_URI_ONE = "IMAGE_URI_ONE";
  public static final String IMAGE_URI_TWO = "IMAGE_URI_TWO";
  public static final String IMAGE_NAME_ONE = "IMAGE_NAME_ONE";
  public static final String IMAGE_NAME_TWO = "IMAGE_NAME_TWO";
}
