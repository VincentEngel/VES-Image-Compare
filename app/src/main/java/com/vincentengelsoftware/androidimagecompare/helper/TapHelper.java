package com.vincentengelsoftware.androidimagecompare.helper;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vincentengelsoftware.androidimagecompare.globals.Settings;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
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

            if (Settings.TAP_HIDE_MODE == Status.TAP_HIDE_MODE_INVISIBLE) {
                imageViewListener.setVisibility(View.INVISIBLE);
                imageViewTarget.setVisibility(View.VISIBLE);
            } else {
                ViewGroup parentView = imageViewListener.getParentViewGroup();
                parentView.removeView((View)imageViewTarget);
                parentView.addView((View)imageViewTarget);
            }

            textViewImageName.setText(targetImageHolder.getImageName());
        });
    }
}
