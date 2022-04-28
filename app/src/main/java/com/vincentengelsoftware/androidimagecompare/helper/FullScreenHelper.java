package com.vincentengelsoftware.androidimagecompare.helper;

import android.view.Window;
import android.view.WindowManager;

public class FullScreenHelper {
    public static void setFullScreenFlags(Window window)
    {
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }
}
