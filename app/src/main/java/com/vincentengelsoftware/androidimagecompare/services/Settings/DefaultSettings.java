package com.vincentengelsoftware.androidimagecompare.services.Settings;

import com.vincentengelsoftware.androidimagecompare.Activities.CompareModes.CompareModeNames;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;

public class DefaultSettings {
    public static int MAX_ZOOM = 100;
    public static String COMPARE_MODE = CompareModeNames.SIDE_BY_SIDE;
    public static boolean SYNCED_ZOOM = true;
    public static boolean SHOW_EXTENSIONS = true;
    public static int THEME = Status.THEME_SYSTEM;

    public static int IMAGE_RESIZE_OPTION = Images.RESIZE_OPTION_AUTOMATIC;
    public static int IMAGE_RESIZE_WIDTH = 1024;
    public static int IMAGE_RESIZE_HEIGHT = 1024;
    public static boolean RESET_IMAGE_ON_LINKING = true;
    public static int MIRRORING_TYPE = 0;
}
