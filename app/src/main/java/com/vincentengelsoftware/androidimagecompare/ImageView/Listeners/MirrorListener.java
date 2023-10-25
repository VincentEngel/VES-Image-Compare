package com.vincentengelsoftware.androidimagecompare.ImageView.Listeners;

import com.vincentengelsoftware.androidimagecompare.ImageView.VesImageInterface;
import com.vincentengelsoftware.androidimagecompare.globals.Settings;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;

public class MirrorListener implements OnTouchListenerInterface
{
    private final VesImageInterface source;
    private final VesImageInterface target;
    private final UtilMutableBoolean sync;

    public MirrorListener(
            VesImageInterface source,
            VesImageInterface target,
            UtilMutableBoolean sync
    ) {
        this.source = source;
        this.target = target;
        this.sync = sync;
    }
    public void trigger()
    {
        if (this.sync.value) {
            if (Settings.LOOSE_MIRRORING) {
                this.target.setImageScale(this.source.getImageScaleCenter());
            } else {
                this.target.setImageScaleCenter(this.source.getImageScaleCenter());
            }
        }
    }
}
