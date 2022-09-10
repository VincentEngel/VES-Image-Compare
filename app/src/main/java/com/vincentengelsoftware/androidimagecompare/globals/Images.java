package com.vincentengelsoftware.androidimagecompare.globals;

import android.net.Uri;

import com.vincentengelsoftware.androidimagecompare.util.ImageHolder;

public class Images {
    public static ImageHolder image_holder_first = new ImageHolder();
    public static ImageHolder image_holder_second = new ImageHolder();
    public static Uri fileUri; // TODO MOVE TO ImageHolder
    public final static String DEFAULT_IMAGE_NAME = "Unknown";
}
