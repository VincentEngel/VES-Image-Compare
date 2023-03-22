package com.vincentengelsoftware.androidimagecompare.helper;

import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class FullScreenHelper {
    // Maybe this can be moved to a config file???
    public static void setFullScreenFlags(Window window)
    {
        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat a = new WindowInsetsControllerCompat(window, window.getDecorView());
        a.hide(WindowInsetsCompat.Type.systemBars());
        a.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

       window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
