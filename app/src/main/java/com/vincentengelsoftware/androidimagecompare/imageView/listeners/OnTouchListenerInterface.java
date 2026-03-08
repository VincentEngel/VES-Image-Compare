package com.vincentengelsoftware.androidimagecompare.imageView.listeners;

import android.view.MotionEvent;
import android.view.View;

public interface OnTouchListenerInterface
{
    void trigger(View view, MotionEvent motionEvent);
}
