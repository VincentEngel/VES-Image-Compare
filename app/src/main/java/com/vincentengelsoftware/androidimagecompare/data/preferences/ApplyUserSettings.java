package com.vincentengelsoftware.androidimagecompare.data.preferences;

import com.vincentengelsoftware.androidimagecompare.constants.ImageResizeOptions;
import com.vincentengelsoftware.androidimagecompare.domain.model.ImageInfoHolder;
import com.vincentengelsoftware.androidimagecompare.ui.util.Theme;

/**
 * Utility class that applies a {@link UserSettings} snapshot to the running session.
 *
 * <p>Bridges persisted preferences and the in-memory state of the UI without coupling either side
 * directly to the other.
 */
public final class ApplyUserSettings {

  private ApplyUserSettings() {}

  public static void apply(
      UserSettings userSettings,
      ImageInfoHolder firstImageInfoHolder,
      ImageInfoHolder secondImageInfoHolder) {
    Theme.updateTheme(userSettings.getTheme());

    applyImageSettings(userSettings.getLeftImageResizeSettings(), firstImageInfoHolder);
    applyImageSettings(userSettings.getRightImageResizeSettings(), secondImageInfoHolder);
  }

  private static void applyImageSettings(
      ImageResizeSettings imageResizeSettings, ImageInfoHolder imageInfoHolder) {
    imageInfoHolder.setResizeOption(imageResizeSettings.getImageResizeOption());

    if (imageResizeSettings.getImageResizeOption() == ImageResizeOptions.RESIZE_OPTION_CUSTOM) {
      imageInfoHolder.setCustomSize(
          imageResizeSettings.getImageResizeHeight(), imageResizeSettings.getImageResizeWidth());
    }
  }
}
