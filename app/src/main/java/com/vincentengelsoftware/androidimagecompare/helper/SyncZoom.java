package com.vincentengelsoftware.androidimagecompare.helper;

import android.graphics.drawable.Drawable;
import android.widget.ToggleButton;

import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;
import com.vincentengelsoftware.androidimagecompare.globals.Settings;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.ImageView.VesImageInterface;

public class SyncZoom {
    public static void setLinkedTargets(
            VesImageInterface imageOne,
            VesImageInterface imageTwo,
            UtilMutableBoolean sync
    ) {
        imageOne.addMirrorListener(imageTwo, sync);
        imageTwo.addMirrorListener(imageOne, sync);
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
                if (Settings.RESET_IMAGE_ON_LINKING) {
                    imageOne.resetZoom();
                    imageTwo.resetZoom();
                }

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
