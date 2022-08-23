package com.vincentengelsoftware.androidimagecompare.helper;

import android.view.View;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class TapHelper {
    public static void setOnClickListener(SubsamplingScaleImageView imageViewListener, SubsamplingScaleImageView imageViewTarget)
    {
        imageViewListener.setOnClickListener(view -> {
            imageViewListener.setVisibility(View.INVISIBLE);
            imageViewTarget.setVisibility(View.VISIBLE);
        });
    }
}
