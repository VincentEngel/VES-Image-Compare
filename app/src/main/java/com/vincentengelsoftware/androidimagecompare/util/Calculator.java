package com.vincentengelsoftware.androidimagecompare.util;

import android.content.res.Resources;
import android.util.TypedValue;

public class Calculator {

    /**
     * Converts a dp value to pixels using the given {@link Resources}.
     *
     * @param dp        value in density-independent pixels
     * @param resources the current {@link Resources} (for display metrics)
     * @return the equivalent pixel count, rounded down to an integer
     */
    public static int dpToPx(int dp, Resources resources) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
}
