package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.res.Resources;
import android.util.TypedValue;

public class Calculator {
    public static int DpToPx2(int dp, Resources resources)
    {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics()));
    }
}
