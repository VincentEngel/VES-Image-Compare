package com.vincentengelsoftware.androidimagecompare.ImageView.Listeners;

import android.view.MotionEvent;
import android.view.View;

public interface OnTouchListenerInterface
{
    void trigger(View view, MotionEvent motionEvent);
}
