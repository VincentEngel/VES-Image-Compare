package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

/**
 * Survives configuration changes (e.g. screen rotation) for
 * {@link OverlayCutActivity}.
 *
 * <p>Holds the mutable crop state that cannot be serialised into a
 * {@link android.os.Bundle}: the adjusted bitmap and the last-known seekbar
 * positions so the view can be restored exactly as the user left it.</p>
 */
public class OverlayCutViewModel extends ViewModel {

    /** The bitmap currently shown on the front image view (may be cropped). */
    @Nullable
    Bitmap bitmapAdjusted;

    // Seekbar progress values – valid only when hasSeekBarState is true.
    boolean hasSeekBarState = false;
    int seekBarTopProgress    = 0;
    int seekBarLeftProgress   = 90;
    int seekBarRightProgress  = 10;
    int seekBarBottomProgress = 0;

    /** Snapshot the current seekbar positions before a configuration change. */
    void saveSeekBarState(int top, int left, int right, int bottom) {
        hasSeekBarState       = true;
        seekBarTopProgress    = top;
        seekBarLeftProgress   = left;
        seekBarRightProgress  = right;
        seekBarBottomProgress = bottom;
    }
}

