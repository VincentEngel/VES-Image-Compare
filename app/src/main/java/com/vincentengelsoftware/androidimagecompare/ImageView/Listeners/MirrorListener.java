package com.vincentengelsoftware.androidimagecompare.ImageView.Listeners;

import android.view.MotionEvent;
import android.view.View;

import com.vincentengelsoftware.androidimagecompare.ImageView.VesImageInterface;
import com.vincentengelsoftware.androidimagecompare.globals.Settings;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;

public class MirrorListener implements OnTouchListenerInterface
{
    private final VesImageInterface source;
    private final VesImageInterface target;
    private final UtilMutableBoolean sync;
    // Prevent infinite loop
    private final UtilMutableBoolean disabled;

    public MirrorListener(
            VesImageInterface source,
            VesImageInterface target,
            UtilMutableBoolean sync,
            UtilMutableBoolean disabled
    ) {
        this.source = source;
        this.target = target;
        this.sync = sync;
        this.disabled = disabled;
    }
    public void trigger(View view, MotionEvent motionEvent)
    {
        if (this.disabled.value) {
            return;
        }

        if (this.sync.value) {
            this.handleMirroring(motionEvent);
        }
    }

    private void handleMirroring(MotionEvent motionEvent) {
        if (Settings.MIRRORING_TYPE == Status.NATURAL_MIRRORING) {
            this.disabled.value = true;
            this.target.onTouchEvent(motionEvent);
            this.disabled.value = false;
            return;
        }

        if (Settings.MIRRORING_TYPE == Status.STRICT_MIRRORING) {
            this.target.setImageScaleCenter(this.source.getImageScaleCenter());
            return;
        }

        if (Settings.MIRRORING_TYPE == Status.LOOSE_MIRRORING) {
            this.target.setImageScale(this.source.getImageScaleCenter());
            return;
        }
    }
}
