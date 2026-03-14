package com.vincentengelsoftware.androidimagecompare.data.preferences;

import com.vincentengelsoftware.androidimagecompare.constants.ImageResizeOptions;
import com.vincentengelsoftware.androidimagecompare.ui.util.Theme;
import com.vincentengelsoftware.androidimagecompare.domain.model.ImageInfoHolder;

public class ApplyUserSettings {
    public static void apply(
            UserSettings userSettings,
            ImageInfoHolder firstImageInfoHolder,
            ImageInfoHolder secondImageInfoHolder
    ) {
        Theme.updateTheme(userSettings.getTheme());

        ApplyUserSettings.applyImageSettings(
                userSettings.getLeftImageResizeSettings(),
                firstImageInfoHolder
        );

        ApplyUserSettings.applyImageSettings(
                userSettings.getRightImageResizeSettings(),
                secondImageInfoHolder
        );
    }

    private static void applyImageSettings(
            ImageResizeSettings imageResizeSettings,
            ImageInfoHolder imageInfoHolder
    ) {
        imageInfoHolder.setResizeOption(imageResizeSettings.getImageResizeOption());

        if (imageResizeSettings.getImageResizeOption() == ImageResizeOptions.RESIZE_OPTION_CUSTOM) {
            imageInfoHolder.setCustomSize(
                    imageResizeSettings.getImageResizeHeight(),
                    imageResizeSettings.getImageResizeWidth()
            );
        }
    }
}
