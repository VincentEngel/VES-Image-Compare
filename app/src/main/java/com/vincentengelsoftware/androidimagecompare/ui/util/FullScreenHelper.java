package com.vincentengelsoftware.androidimagecompare.ui.util;

import android.view.Window;
import android.view.WindowManager;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Utility class for applying immersive full-screen display and preventing the screen from timing
 * out while a compare Activity is active.
 *
 * <p>Non-instantiable – all methods are static.
 */
public final class FullScreenHelper {

  private FullScreenHelper() {
    /* utility class */
  }

  /**
   * Conditionally applies full-screen flags based on the {@code showNavigationBar} parameter.
   *
   * <p>When {@code showNavigationBar} is {@code true} the navigation bar and status bar are hidden
   * and the screen is kept on. When {@code false} the system bars are explicitly shown, {@code
   * decorFitsSystemWindows} is restored to {@code true} (content stays inside the bars), and only
   * the keep-screen-on flag is applied.
   *
   * @param window the window to configure
   * @param showNavigationBar whether to enter immersive showNavigationBar mode
   */
  public static void apply(Window window, boolean showNavigationBar) {
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    WindowCompat.setDecorFitsSystemWindows(window, showNavigationBar);
    WindowInsetsControllerCompat controller =
        new WindowInsetsControllerCompat(window, window.getDecorView());
    if (showNavigationBar) {
      controller.show(WindowInsetsCompat.Type.systemBars());
    } else {
      controller.hide(WindowInsetsCompat.Type.systemBars());
      controller.setSystemBarsBehavior(
          WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }
  }
}
