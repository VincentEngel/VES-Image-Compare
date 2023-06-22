package com.vincentengelsoftware.androidimagecompare.services;

import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.helper.Theme;
import com.vincentengelsoftware.androidimagecompare.util.ImageHolder;

public class ApplyUserSettings {
    public static void apply(
            UserSettings userSettings,
            ImageHolder firstImageHolder,
            ImageHolder secondImageHolder
    ) {
        Theme.updateTheme(userSettings.getTheme());

        ApplyUserSettings.applyImageSettings(
                userSettings.getLeftImageResizeSettings(),
                firstImageHolder
        );

        ApplyUserSettings.applyImageSettings(
                userSettings.getRightImageResizeSettings(),
                secondImageHolder
        );
    }

    private static void applyImageSettings(
            ImageResizeSettings imageResizeSettings,
            ImageHolder imageHolder
    ) {
        imageHolder.setResizeOption(imageResizeSettings.getImageResizeOption());

        if (imageResizeSettings.getImageResizeOption() == Images.RESIZE_OPTION_CUSTOM) {
            imageHolder.setCustomSize(
                    imageResizeSettings.getImageResizeHeight(),
                    imageResizeSettings.getImageResizeWidth()
            );
        }
    }
}
