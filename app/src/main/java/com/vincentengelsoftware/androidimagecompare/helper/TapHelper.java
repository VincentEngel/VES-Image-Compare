package com.vincentengelsoftware.androidimagecompare.helper;

import android.view.View;
import android.widget.ImageView;

public class TapHelper {
    public static void setOnClickListener(ImageView imageViewListener, ImageView imageViewTarget)
    {
        imageViewListener.setOnClickListener(view -> {
            imageViewListener.setVisibility(View.INVISIBLE);
            imageViewTarget.setVisibility(View.VISIBLE);
        });
    }
}
