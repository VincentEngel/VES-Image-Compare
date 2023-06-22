package com.vincentengelsoftware.androidimagecompare.globals;

import android.net.Uri;

import com.vincentengelsoftware.androidimagecompare.util.ImageHolder;

public class Images {
    public static ImageHolder first = new ImageHolder();
    public static ImageHolder second = new ImageHolder();
    public final static String DEFAULT_IMAGE_NAME = "Unknown";

    public static Uri fileUriFirst;
    public static Uri fileUriSecond;

    public static final int RESIZE_OPTION_ORIGINAL = 0;
    public static final int RESIZE_OPTION_AUTOMATIC = 1;
    public static final int RESIZE_OPTION_CUSTOM = 2;
}
