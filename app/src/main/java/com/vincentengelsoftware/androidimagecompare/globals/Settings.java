package com.vincentengelsoftware.androidimagecompare.globals;

import com.vincentengelsoftware.androidimagecompare.services.Settings.UserSettings;

public class Settings {
    public static void init(UserSettings userSettings) {
        Settings.MAX_ZOOM = userSettings.getMaxZoom();
        Settings.RESET_IMAGE_ON_LINKING = userSettings.getResetImageOnLink();
        Settings.MIRRORING_TYPE = userSettings.getMirroringType();
        Settings.TAP_HIDE_MODE = userSettings.getTypHideMode();
    }
    public static int MAX_ZOOM = 10; // Bad practice: static mutable field
    public static boolean RESET_IMAGE_ON_LINKING = true; // Bad practice: static mutable field

    public static int MIRRORING_TYPE = 0; // Bad practice: static mutable field
    public static int TAP_HIDE_MODE = 0; // Bad practice: static mutable field
}
