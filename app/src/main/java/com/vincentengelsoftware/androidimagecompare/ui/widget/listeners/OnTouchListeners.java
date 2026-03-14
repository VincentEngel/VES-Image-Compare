package com.vincentengelsoftware.androidimagecompare.ui.widget.listeners;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * A composite {@link View.OnTouchListener} that dispatches each touch event to
 * every registered {@link OnTouchListenerInterface} in insertion order.
 */
public class OnTouchListeners implements View.OnTouchListener {

    private final List<OnTouchListenerInterface> listeners = new ArrayList<>();

    /** Registers an additional listener to be notified on every touch event. */
    public void add(OnTouchListenerInterface listener) {
        listeners.add(listener);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        for (OnTouchListenerInterface listener : listeners) {
            listener.trigger(view, event);
        }
        return false;
    }
}
