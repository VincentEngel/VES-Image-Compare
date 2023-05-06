package com.vincentengelsoftware.androidimagecompare.helper;

import android.graphics.drawable.Drawable;
import android.widget.ToggleButton;

import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.viewClasses.VesImageInterface;

public class SyncZoom {
    public static void setLinkedTargets(
            VesImageInterface imageOne,
            VesImageInterface imageTwo,
            UtilMutableBoolean sync
    ) {
        imageOne.setLinkedTarget(imageTwo, sync);
        imageTwo.setLinkedTarget(imageOne, sync);
    }

    public static void setUpSyncZoomToggleButton(
            VesImageInterface imageOne,
            VesImageInterface imageTwo,
            ToggleButton toggleButton,
            Drawable iconLinkedOn,
            Drawable iconLinkedOff,
            UtilMutableBoolean sync,
            FadeActivity activity
    )
    {
        toggleButton.setChecked(sync.value);
        if (sync.value) {
            toggleButton.setBackgroundDrawable(iconLinkedOn);
        } else {
            toggleButton.setBackgroundDrawable(iconLinkedOff);
        }

        toggleButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (activity != null) {
                activity.instantFadeIn();
            }
            if (b) {
                imageOne.resetScaleAndCenter();
                imageTwo.resetScaleAndCenter();
                toggleButton.setBackgroundDrawable(iconLinkedOn);
            } else {
                toggleButton.setBackgroundDrawable(iconLinkedOff);
            }
            sync.value = !sync.value;
            if (activity != null) {
                activity.triggerFadeOutThread();
            }
        });
    }
}
