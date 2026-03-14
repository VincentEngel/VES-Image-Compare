package com.vincentengelsoftware.androidimagecompare.ui.compare;

import android.widget.ImageButton;
import android.widget.SeekBar;

import com.vincentengelsoftware.androidimagecompare.R;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility that configures the slide-direction toggle button.
 *
 * <p>When clicked the button flips the {@code leftToRight} flag, updates its icon,
 * and nudges the seekbar so that {@code onProgressChanged} fires and triggers a
 * fresh crop via the Activity's seek-bar listener.</p>
 */
public class SlideHelper {

    /**
     * Attaches a click listener to {@code directionButton} that toggles slide direction
     * and resets the seekbar to the midpoint.
     *
     * @param directionButton the button that swaps LTR ↔ RTL
     * @param seekBar         the seekbar whose progress is reset on direction change
     * @param leftToRight     mutable flag shared with the ViewModel
     */
    public static void setSwapSlideDirectionOnClick(
            ImageButton directionButton,
            SeekBar seekBar,
            AtomicBoolean leftToRight
    ) {
        directionButton.setOnClickListener(view -> {
            leftToRight.set(!leftToRight.get());
            directionButton.setImageResource(
                    leftToRight.get() ? R.drawable.ic_slide_ltr : R.drawable.ic_slide_rtl);

            // onProgressChanged is not triggered when setProgress is called with
            // the current value, so nudge by 1 to guarantee the callback fires.
            int progress = 50;
            if (seekBar.getProgress() == progress) progress = 51;
            seekBar.setProgress(progress);
        });
    }
}
