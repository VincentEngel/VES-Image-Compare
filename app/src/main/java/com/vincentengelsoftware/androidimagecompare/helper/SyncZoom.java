package com.vincentengelsoftware.androidimagecompare.helper;

import android.graphics.drawable.Drawable;
import android.widget.ToggleButton;

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
            UtilMutableBoolean sync
    )
    {
        toggleButton.setChecked(sync.value);
        if (sync.value) {
            toggleButton.setBackgroundDrawable(iconLinkedOn);
        } else {
            toggleButton.setBackgroundDrawable(iconLinkedOff);
        }

        toggleButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                imageOne.resetScaleAndCenter();
                imageTwo.resetScaleAndCenter();
                toggleButton.setBackgroundDrawable(iconLinkedOn);
            } else {
                toggleButton.setBackgroundDrawable(iconLinkedOff);
            }
            sync.value = !sync.value;
        });
    }
}
