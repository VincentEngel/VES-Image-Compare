package com.vincentengelsoftware.androidimagecompare.ui.widget.listeners;

import android.view.MotionEvent;
import android.view.View;

public interface OnTouchListenerInterface
{
    void trigger(View view, MotionEvent motionEvent);
}
