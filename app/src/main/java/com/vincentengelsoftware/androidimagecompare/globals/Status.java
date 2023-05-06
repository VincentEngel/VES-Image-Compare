package com.vincentengelsoftware.androidimagecompare.globals;

public class Status {
    public static boolean handleIntentOnCreate = true;
    public static boolean activityIsOpening = false;
    public static int THEME = Status.THEME_SYSTEM;

    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    public static boolean SYNCED_ZOOM = true;
    public static boolean SHOW_EXTENSIONS = true;

    public static boolean RESIZE_LEFT_IMAGE;

    public static boolean RESIZE_RIGHT_IMAGE;

    public static boolean isTakingPicture = false;

    public static boolean hasHardwareKey = false;
}
