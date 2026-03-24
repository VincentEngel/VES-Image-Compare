package com.vincentengelsoftware.androidimagecompare.data.preferences;

/**
 * Singleton that provides typed access to all persisted user preferences.
 *
 * <p>Obtain the shared instance via {@link #getInstance(KeyValueStorage)}. The first call
 * initialises the singleton; subsequent calls with a different {@link KeyValueStorage} are ignored
 * – the original instance is always returned. The implementation is thread-safe through
 * double-checked locking on a {@code volatile} field.
 */
public final class UserSettings {
  private static volatile UserSettings instance;
  private final KeyValueStorage keyValueStorage;
  public static final String USER_THEME = "USER_THEME";
  public static final String SYNC_IMAGE_INTERACTIONS = "SYNCED_ZOOM";
  public static final String SHOW_EXTENSIONS = "SHOW_EXTENSIONS";
  public static final String LAST_COMPARE_MODE = "LAST_COMPARE_MODE";
  public static final String MAX_ZOOM = "MAX_ZOOM";
  public static final String MIN_ZOOM = "MIN_ZOOM";

  public static final String RESET_IMAGE_ON_LINKING = "RESET_IMAGE_ON_LINKING";

  public static final String MIRRORING_TYPE = "MIRRORING_TYPE";

  public static final String TAP_HIDE_MODE = "TAP_HIDE_MODE";

  public static final String SHOW_NAVIGATION_BAR = "SHOW_NAVIGATION_BAR";

  public static final String DIFFERENCES_MAX_COUNT = "DIFFERENCES_MAX_COUNT";
  public static final String DIFFERENCES_CIRCLE_COLOR = "DIFFERENCES_CIRCLE_COLOR";

  private final ImageResizeSettings leftImageResizeSettings;
  private final ImageResizeSettings rightImageResizeSettings;

  private UserSettings(KeyValueStorage keyValueStorage) {
    this.keyValueStorage = keyValueStorage;

    this.leftImageResizeSettings = new ImageResizeSettings("LEFT_", this.keyValueStorage);
    this.rightImageResizeSettings = new ImageResizeSettings("RIGHT_", this.keyValueStorage);
  }

  public static UserSettings getInstance(KeyValueStorage keyValueStorage) {
    if (instance == null) {
      synchronized (UserSettings.class) {
        if (instance == null) {
          instance = new UserSettings(keyValueStorage);
        }
      }
    }

    return instance;
  }

  public ImageResizeSettings getLeftImageResizeSettings() {
    return this.leftImageResizeSettings;
  }

  public ImageResizeSettings getRightImageResizeSettings() {
    return this.rightImageResizeSettings;
  }

  public String getLastCompareMode() {
    return this.keyValueStorage.getString(
        UserSettings.LAST_COMPARE_MODE, DefaultSettings.COMPARE_MODE);
  }

  public void setLastCompareMode(String lastCompareMode) {
    this.keyValueStorage.setString(UserSettings.LAST_COMPARE_MODE, lastCompareMode);
  }

  public boolean isSyncImageInteractions() {
    return this.keyValueStorage.getBoolean(
        UserSettings.SYNC_IMAGE_INTERACTIONS, DefaultSettings.SYNC_IMAGE_INTERACTIONS);
  }

  public void setSyncImageInteractions(boolean syncedZoom) {
    this.keyValueStorage.setBoolean(UserSettings.SYNC_IMAGE_INTERACTIONS, syncedZoom);
  }

  public boolean isShowExtensions() {
    return this.keyValueStorage.getBoolean(
        UserSettings.SHOW_EXTENSIONS, DefaultSettings.SHOW_EXTENSIONS);
  }

  public void setShowExtensions(boolean showExtensions) {
    this.keyValueStorage.setBoolean(UserSettings.SHOW_EXTENSIONS, showExtensions);
  }

  public int getTheme() {
    return this.keyValueStorage.getInt(UserSettings.USER_THEME, DefaultSettings.THEME);
  }

  public void setTheme(int theme) {
    this.keyValueStorage.setInt(UserSettings.USER_THEME, theme);
  }

  public int getMaxZoom() {
    return this.keyValueStorage.getInt(UserSettings.MAX_ZOOM, DefaultSettings.MAX_ZOOM);
  }

  public void setMaxZoom(int maxZoom) {
    if (maxZoom < 1) {
      maxZoom = 1;
    }

    this.keyValueStorage.setInt(UserSettings.MAX_ZOOM, maxZoom);
  }

  public boolean getResetImageOnLink() {
    return this.keyValueStorage.getBoolean(
        UserSettings.RESET_IMAGE_ON_LINKING, DefaultSettings.RESET_IMAGE_ON_LINKING);
  }

  public void setResetImageOnLinking(boolean resetImageOnLinking) {
    this.keyValueStorage.setBoolean(UserSettings.RESET_IMAGE_ON_LINKING, resetImageOnLinking);
  }

  public int getMirroringType() {
    return this.keyValueStorage.getInt(UserSettings.MIRRORING_TYPE, DefaultSettings.MIRRORING_TYPE);
  }

  public void setMirroringType(int mirroringType) {
    this.keyValueStorage.setInt(UserSettings.MIRRORING_TYPE, mirroringType);
  }

  public int getTapHideMode() {
    return this.keyValueStorage.getInt(UserSettings.TAP_HIDE_MODE, DefaultSettings.TAP_HIDE_MODE);
  }

  public float getMinZoom() {
    return this.keyValueStorage.getFloat(UserSettings.MIN_ZOOM, DefaultSettings.MIN_ZOOM);
  }

  public void setMinZoom(float minZoom) {
    this.keyValueStorage.setFloat(UserSettings.MIN_ZOOM, minZoom);
  }

  /** Sets the tap-hide mode. Corrected spelling (was {@code setTypHideMode}). */
  public void setTapHideMode(int tapHideMode) {
    this.keyValueStorage.setInt(UserSettings.TAP_HIDE_MODE, tapHideMode);
  }

  public boolean getShowNavigationBar() {
    return this.keyValueStorage.getBoolean(
        UserSettings.SHOW_NAVIGATION_BAR, DefaultSettings.SHOW_NAVIGATION_BAR);
  }

  public void setShowNavigationBar(boolean showNavigationBar) {
    this.keyValueStorage.setBoolean(UserSettings.SHOW_NAVIGATION_BAR, showNavigationBar);
  }

  public int getDifferencesMaxCount() {
    return this.keyValueStorage.getInt(
        UserSettings.DIFFERENCES_MAX_COUNT, DefaultSettings.DIFFERENCES_MAX_COUNT);
  }

  public void setDifferencesMaxCount(int maxCount) {
    this.keyValueStorage.setInt(UserSettings.DIFFERENCES_MAX_COUNT, maxCount);
  }

  public int getDifferencesCircleColor() {
    return this.keyValueStorage.getInt(
        UserSettings.DIFFERENCES_CIRCLE_COLOR, DefaultSettings.DIFFERENCES_CIRCLE_COLOR);
  }

  public void setDifferencesCircleColor(int color) {
    this.keyValueStorage.setInt(UserSettings.DIFFERENCES_CIRCLE_COLOR, color);
  }

  public void resetAllSettings() {
    this.keyValueStorage.remove(UserSettings.USER_THEME);
    this.keyValueStorage.remove(UserSettings.SYNC_IMAGE_INTERACTIONS);
    this.keyValueStorage.remove(UserSettings.SHOW_EXTENSIONS);
    this.keyValueStorage.remove(UserSettings.LAST_COMPARE_MODE);
    this.keyValueStorage.remove(UserSettings.MAX_ZOOM);
    this.keyValueStorage.remove(UserSettings.RESET_IMAGE_ON_LINKING);
    this.keyValueStorage.remove(UserSettings.MIRRORING_TYPE);
    this.keyValueStorage.remove(UserSettings.TAP_HIDE_MODE);
    this.keyValueStorage.remove(UserSettings.MIN_ZOOM);
    this.keyValueStorage.remove(UserSettings.SHOW_NAVIGATION_BAR);
    this.keyValueStorage.remove(UserSettings.DIFFERENCES_MAX_COUNT);
    this.keyValueStorage.remove(UserSettings.DIFFERENCES_CIRCLE_COLOR);

    this.leftImageResizeSettings.resetAllSettings();
    this.rightImageResizeSettings.resetAllSettings();
  }
}
