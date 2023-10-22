package com.vincentengelsoftware.androidimagecompare.ImageView.Listeners;

import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;

public class FadeListener implements OnTouchListenerInterface
{
    private final FadeActivity fadeActivity;

    public FadeListener(FadeActivity fadeActivity)
    {
        this.fadeActivity = fadeActivity;
    }
    public void trigger()
    {
        this.fadeActivity.triggerFadeIn();
    }
}
