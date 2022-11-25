package com.vincentengelsoftware.androidimagecompare.globals;

public class Status {
    public static boolean isFirstStart = true;
    public static boolean handleIntentOnCreate = true;
    public static boolean activityIsOpening = false;
    public static int THEME = Status.THEME_SYSTEM;
    public static int THEME_DEFAULT_SYSTEM = -1;

    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    public static boolean SYNCED_ZOOM = true;
    public static boolean SHOW_EXTENSIONS = true;
}
