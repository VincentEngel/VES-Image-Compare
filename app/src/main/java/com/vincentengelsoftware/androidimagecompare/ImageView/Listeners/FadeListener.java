package com.vincentengelsoftware.androidimagecompare.ImageView.Listeners;

import android.view.MotionEvent;
import android.view.View;

import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;

public class FadeListener implements OnTouchListenerInterface
{
    private final FadeActivity fadeActivity;

    public FadeListener(FadeActivity fadeActivity)
    {
        this.fadeActivity = fadeActivity;
    }
    public void trigger(View view, MotionEvent motionEvent)
    {
        this.fadeActivity.triggerFadeIn();
    }
}
