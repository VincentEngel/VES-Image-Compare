package com.vincentengelsoftware.androidimagecompare.helper;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.vincentengelsoftware.androidimagecompare.util.ImageHolder;

public class ImageUpdater {
    public static final String ORIGINAL = "ORIGINAL";
    public static final String SCREEN_SIZE = "SCREEN_SIZE";
    public static final String SMALL = "SMALL";

    public static void updateImage(
            ImageView imageView,
            ImageHolder imageHolder,
            String IMAGE_SIZE
    ) {
        Bitmap bitmap;

        switch (IMAGE_SIZE) {
            case SMALL:
                bitmap = imageHolder.getBitmapSmall();
                break;
            case SCREEN_SIZE:
                bitmap = imageHolder.getBitmapScreenSize();
                break;
            case ORIGINAL:
            default:
                bitmap = imageHolder.rotatedBitmap;
                break;
        }

        imageView.setImageBitmap(bitmap);
    }
}
