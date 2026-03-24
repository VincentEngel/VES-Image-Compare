package com.vincentengelsoftware.androidimagecompare.util;

import android.app.Activity;
import android.util.TypedValue;
import androidx.window.layout.WindowMetrics;
import androidx.window.layout.WindowMetricsCalculator;

/**
 * Factory that computes the screen-size constraints required by {@link Dimensions}.
 *
 * <p>Call {@link #init(Activity)} once from {@code Activity.onCreate} and keep the returned {@link
 * Dimensions} instance alive for the lifetime of the activity. Pass it by dependency injection to
 * every component that needs it — never store it in static state.
 */
public class DimensionsInitializer {

  private DimensionsInitializer() {}

  /**
   * Computes {@link Dimensions#maxSide()} and {@link Dimensions#maxSideForPreview()} from the
   * current window metrics and returns an immutable {@link Dimensions} value object.
   */
  public static Dimensions init(Activity activity) {
    WindowMetrics windowMetrics =
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity);
    int maxSide = Math.max(windowMetrics.getBounds().height(), windowMetrics.getBounds().width());

    int maxSideForPreview =
        Math.round(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                Dimensions.MAX_SMALL_SIZE_DP,
                activity.getResources().getDisplayMetrics()));

    return new Dimensions(maxSide, maxSideForPreview);
  }
}
