package com.vincentengelsoftware.androidimagecompare.ImageView.Listeners;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class OnTouchListeners implements View.OnTouchListener {
    private final ArrayList<OnTouchListenerInterface> onTouchListeners = new ArrayList<>();

    public void add(OnTouchListenerInterface onTouchListener)
    {
        this.onTouchListeners.add(onTouchListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View view, MotionEvent motionEvent) {
        for (int i = 0; i < onTouchListeners.size(); i++)
        {
            onTouchListeners.get(i).trigger(view, motionEvent);
        }

        return false;
    }
}
