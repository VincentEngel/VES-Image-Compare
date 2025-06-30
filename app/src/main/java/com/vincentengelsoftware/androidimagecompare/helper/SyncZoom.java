package com.vincentengelsoftware.androidimagecompare.helper;

import android.graphics.drawable.Drawable;
import android.widget.ToggleButton;

import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;
import com.vincentengelsoftware.androidimagecompare.globals.Settings;
import com.vincentengelsoftware.androidimagecompare.ImageView.VesImageInterface;

import java.util.concurrent.atomic.AtomicBoolean;

public class SyncZoom {
    public static void setLinkedTargets(
            VesImageInterface imageOne,
            VesImageInterface imageTwo,
            AtomicBoolean sync,
            AtomicBoolean disabled
    ) {
        imageOne.addMirrorListener(imageTwo, sync, disabled);
        imageTwo.addMirrorListener(imageOne, sync, disabled);
    }

    public static void setUpSyncZoomToggleButton(
            VesImageInterface imageOne,
            VesImageInterface imageTwo,
            ToggleButton toggleButton,
            Drawable iconLinkedOn,
            Drawable iconLinkedOff,
            AtomicBoolean sync,
            FadeActivity activity
    )
    {
        toggleButton.setChecked(sync.get());
        if (sync.get()) {
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
            sync.set(!sync.get());
            if (activity != null) {
                activity.triggerFadeOutThread();
            }
        });
    }
}
