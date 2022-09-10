package com.vincentengelsoftware.androidimagecompare.helper;

import android.view.View;

import com.vincentengelsoftware.androidimagecompare.viewClasses.VesImageInterface;

public class TapHelper {
    public static void setOnClickListener(VesImageInterface imageViewListener, VesImageInterface imageViewTarget)
    {
        imageViewListener.setOnClickListener(view -> {
            imageViewListener.setVisibility(View.INVISIBLE);
            imageViewTarget.setVisibility(View.VISIBLE);
        });
    }
}
