package com.vincentengelsoftware.androidimagecompare.ImageView.Listeners;

import java.util.ArrayList;

public class OnTouchListeners {
    private final ArrayList<OnTouchListenerInterface> onTouchListeners = new ArrayList<>();

    public void add(OnTouchListenerInterface onTouchListener)
    {
        this.onTouchListeners.add(onTouchListener);
    }

    public void trigger()
    {
        for (int i = 0; i < onTouchListeners.size(); i++)
        {
            onTouchListeners.get(i).trigger();
        }
    }
}
