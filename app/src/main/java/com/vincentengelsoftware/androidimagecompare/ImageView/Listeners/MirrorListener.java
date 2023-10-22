package com.vincentengelsoftware.androidimagecompare.ImageView.Listeners;

import com.vincentengelsoftware.androidimagecompare.ImageView.VesImageInterface;
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
            this.target.applyScaleAndCenter(this.source);
        }
    }
}
