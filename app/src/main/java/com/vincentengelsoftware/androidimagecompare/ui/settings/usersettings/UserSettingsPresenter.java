package com.vincentengelsoftware.androidimagecompare.ui.settings.usersettings;

import androidx.annotation.VisibleForTesting;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.constants.Status;
import com.vincentengelsoftware.androidimagecompare.data.preferences.UserSettings;

/**
 * Pure-Java presenter for {@link UserSettingsActivity}.
 *
 * <p>Contains all business / settings logic with no Android UI dependencies, making it
 * straightforwardly testable with plain JUnit tests. The Activity is responsible only for rendering
 * the returned {@link UserSettingsUiState} and calling the appropriate presenter methods in
 * response to user actions.
 */
public class UserSettingsPresenter {

  @VisibleForTesting static final float MIN_ZOOM_FALLBACK = 0.1F;
  @VisibleForTesting static final int MAX_ZOOM_FALLBACK = 1;

  /** Total number of theme options: System, Light, Dark. */
  private static final int THEME_COUNT = 3;

  private final UserSettings userSettings;

  public UserSettingsPresenter(UserSettings userSettings) {
    this.userSettings = userSettings;
  }

  /**
   * Builds a complete {@link UserSettingsUiState} snapshot from the current {@link UserSettings}
   * values.
   */
  public UserSettingsUiState buildUiState() {
    int mirroringType = userSettings.getMirroringType();
    int tapHideMode = userSettings.getTapHideMode();
    int theme = userSettings.getTheme();
    int circleColor = userSettings.getDifferencesCircleColor();

    return new UserSettingsUiState(
        String.valueOf(userSettings.getMaxZoom()),
        String.valueOf(userSettings.getMinZoom()),
        userSettings.getResetImageOnLink(),
        mirroringType == Status.NATURAL_MIRRORING,
        mirroringType == Status.STRICT_MIRRORING,
        mirroringType == Status.LOOSE_MIRRORING,
        mirroringExplanationResId(mirroringType),
        tapHideMode == Status.TAP_HIDE_MODE_INVISIBLE,
        tapHideMode == Status.TAP_HIDE_MODE_BACKGROUND,
        tapHideModeDescriptionResId(tapHideMode),
        themeButtonTextResId(theme),
        userSettings.getShowNavigationBar(),
        String.valueOf(userSettings.getDifferencesMaxCount()),
        circleColor == Status.DIFF_CIRCLE_COLOR_RED,
        circleColor == Status.DIFF_CIRCLE_COLOR_BLUE,
        circleColor == Status.DIFF_CIRCLE_COLOR_GREEN);
  }

  /**
   * Validates and persists zoom bounds.
   *
   * <p>Values below their minimum are clamped to {@link #MAX_ZOOM_FALLBACK} / {@link
   * #MIN_ZOOM_FALLBACK} respectively.
   *
   * @return a {@link SaveZoomResult} indicating whether any clamping occurred
   */
  public SaveZoomResult saveZoom(int rawMaxZoom, float rawMinZoom) {
    boolean maxZoomClamped = false;
    boolean minZoomClamped = false;

    int maxZoom = rawMaxZoom;
    if (maxZoom < 1) {
      maxZoom = MAX_ZOOM_FALLBACK;
      maxZoomClamped = true;
    }

    float minZoom = rawMinZoom;
    if (minZoom <= 0F) {
      minZoom = MIN_ZOOM_FALLBACK;
      minZoomClamped = true;
    }

    userSettings.setMaxZoom(maxZoom);
    userSettings.setMinZoom(minZoom);

    return new SaveZoomResult(maxZoomClamped || minZoomClamped);
  }

  /**
   * Cycles to the next theme (System → Light → Dark → System → …) and returns the new theme value.
   */
  public int cycleTheme() {
    int newTheme = (userSettings.getTheme() + 1) % THEME_COUNT;
    userSettings.setTheme(newTheme);
    return newTheme;
  }

  /** Persists whether images should reset their transform when linking is toggled. */
  public void setResetImageOnLinking(boolean checked) {
    userSettings.setResetImageOnLinking(checked);
  }

  /** Persists the active mirroring type (e.g. {@link Status#NATURAL_MIRRORING}). */
  public void setMirroringType(int mirroringType) {
    userSettings.setMirroringType(mirroringType);
  }

  /** Persists the active tap-hide mode (e.g. {@link Status#TAP_HIDE_MODE_INVISIBLE}). */
  public void setTapHideMode(int tapHideMode) {
    userSettings.setTapHideMode(tapHideMode);
  }

  /** Persists whether the system navigation bar is shown during compare sessions. */
  public void setShowNavigationBar(boolean showNavigationBar) {
    userSettings.setShowNavigationBar(showNavigationBar);
  }

  /**
   * Validates and persists the max-differences count.
   *
   * @return {@code true} if the input was invalid (clamped to 1)
   */
  public boolean saveDifferencesMaxCount(int raw) {
    if (raw < 1) {
      userSettings.setDifferencesMaxCount(1);
      return true;
    }
    userSettings.setDifferencesMaxCount(raw);
    return false;
  }

  /** Persists the circle colour used to highlight detected differences. */
  public void setDifferencesCircleColor(int color) {
    userSettings.setDifferencesCircleColor(color);
  }

  /**
   * Resets all settings to their defaults and returns the resulting {@link UserSettingsUiState}.
   */
  public UserSettingsUiState resetAllSettings() {
    userSettings.resetAllSettings();
    return buildUiState();
  }

  /** Returns the string resource ID for the description of the given mirroring type. */
  public static int mirroringExplanationResId(int mirroringType) {
    return switch (mirroringType) {
      case Status.STRICT_MIRRORING -> R.string.settings_mirroring_strict_description;
      case Status.LOOSE_MIRRORING -> R.string.settings_mirroring_loose_description;
      default -> R.string.settings_mirroring_natural_description;
    };
  }

  /** Returns the string resource ID for the description of the given tap-hide mode. */
  public static int tapHideModeDescriptionResId(int tapHideMode) {
    return tapHideMode == Status.TAP_HIDE_MODE_BACKGROUND
        ? R.string.settings_tap_hide_mode_description_background
        : R.string.settings_tap_hide_mode_description_invisible;
  }

  /** Returns the string resource ID for the theme toggle button label for the given theme. */
  public static int themeButtonTextResId(int theme) {
    return switch (theme) {
      case Status.THEME_SYSTEM -> R.string.theme_system;
      case Status.THEME_LIGHT -> R.string.theme_light;
      default -> R.string.theme_dark;
    };
  }

  /** Result of a {@link #saveZoom} call, indicating whether any input was out of range. */
  public record SaveZoomResult(boolean hadInvalidInput) {}
}
