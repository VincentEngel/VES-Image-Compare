package com.vincentengelsoftware.androidimagecompare.util;

import android.app.Activity;
import android.util.TypedValue;

import androidx.window.layout.WindowMetrics;
import androidx.window.layout.WindowMetricsCalculator;

/**
 * Initialises the global {@link Dimensions} constants that depend on the device screen size.
 * <p>
 * Call {@link #init(Activity)} once from {@code Activity.onCreate}. Subsequent calls are
 * no-ops once both dimension values have been set.
 */
public class DimensionsInitializer {

    private DimensionsInitializer() {}

    /**
     * Computes and stores {@link Dimensions#maxSide} and {@link Dimensions#maxSideForPreview}
     * from the current window metrics. Safe to call multiple times; re-initialises only when
     * a value is still at its default ({@code 0}).
     */
    public static void init(Activity activity) {
        if (Dimensions.maxSide == 0) {
            WindowMetrics windowMetrics =
                    WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity);
            Dimensions.maxSide = Math.max(
                    windowMetrics.getBounds().height(),
                    windowMetrics.getBounds().width()
            );
        }

        if (Dimensions.maxSideForPreview == 0) {
            Dimensions.maxSideForPreview = Math.round(
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            Dimensions.MAX_SMALL_SIZE_DP,
                            activity.getResources().getDisplayMetrics()
                    )
            );
        }
    }
}

