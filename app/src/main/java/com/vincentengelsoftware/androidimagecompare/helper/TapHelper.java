package com.vincentengelsoftware.androidimagecompare.helper;

import android.view.View;
import android.widget.TextView;

import com.vincentengelsoftware.androidimagecompare.util.ImageHolder;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.ImageView.VesImageInterface;

public class TapHelper {
    public static void setOnClickListener(
            VesImageInterface imageViewListener,
            VesImageInterface imageViewTarget,
            UtilMutableBoolean utilMutableBoolean,
            TextView textViewImageName,
            ImageHolder targetImageHolder
    )
    {
        imageViewListener.setOnClickListener(view -> {
            if (utilMutableBoolean.value) {
                imageViewTarget.applyScaleAndCenter(imageViewListener);
            }

            imageViewListener.setVisibility(View.INVISIBLE);
            imageViewTarget.setVisibility(View.VISIBLE);
            textViewImageName.setText(targetImageHolder.getImageName());
        });
    }
}
