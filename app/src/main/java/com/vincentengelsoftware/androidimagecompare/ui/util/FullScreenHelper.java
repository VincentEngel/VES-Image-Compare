package com.vincentengelsoftware.androidimagecompare.ui.util;

import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Utility class for applying immersive full-screen display and preventing
 * the screen from timing out while a compare Activity is active.
 *
 * <p>Non-instantiable – all methods are static.</p>
 */
public final class FullScreenHelper {

    private FullScreenHelper() { /* utility class */ }

    /**
     * Applies immersive full-screen flags to the given {@link Window} and keeps
     * the screen on for the lifetime of the window.
     *
     * <p>System bars are hidden and will reappear transiently on swipe.</p>
     *
     * @param window the window to configure
     */
    public static void setFullScreenFlags(Window window) {
        WindowCompat.setDecorFitsSystemWindows(window, false);

        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(window, window.getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
