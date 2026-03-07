package com.vincentengelsoftware.androidimagecompare.services.Settings;

import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.helper.Theme;
import com.vincentengelsoftware.androidimagecompare.util.ImageInfoHolder;

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

        if (imageResizeSettings.getImageResizeOption() == Images.RESIZE_OPTION_CUSTOM) {
            imageInfoHolder.setCustomSize(
                    imageResizeSettings.getImageResizeHeight(),
                    imageResizeSettings.getImageResizeWidth()
            );
        }
    }
}
