package com.vincentengelsoftware.androidimagecompare.ui.compare;

import android.view.View;
import android.widget.SeekBar;
import android.widget.ImageButton;

import com.vincentengelsoftware.androidimagecompare.ui.animation.ControlsBarHost;
import com.vincentengelsoftware.androidimagecompare.ui.widget.VesImageInterface;
import com.vincentengelsoftware.androidimagecompare.R;

/**
 * Utility that wires up the transparency seekbar for the overlay-transparent mode.
 *
 * <p>Non-instantiable – all methods are static.</p>
 */
public final class TransparentHelper {

    /**
     * Seekbar progress value at or below which the front image is considered fully hidden.
     * Used both inside this class and by the hide/show button logic in
     * {@link OverlayTransparentActivity}.
     */
    public static final int HIDE_THRESHOLD = 2;

    private TransparentHelper() { /* utility class */ }

    /**
     * Attaches a seekbar listener that controls the opacity of {@code imageView}.
     * Hides the image entirely when the progress drops to {@link #HIDE_THRESHOLD} or below.
     *
     * @param seekBar         the seekbar that drives the opacity
     * @param imageView       the front image view whose alpha is adjusted
     * @param hideShow        the visibility toggle button whose icon is updated
     * @param controlsBarHost the host Activity used to show/schedule the controls bar
     */
    public static void makeTargetTransparent(
            SeekBar seekBar,
            VesImageInterface imageView,
            ImageButton hideShow,
            ControlsBarHost controlsBarHost
    ) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                controlsBarHost.showControlsBarInstant();
                if (progress <= HIDE_THRESHOLD) {
                    hideShow.setImageResource(R.drawable.ic_visibility_off);
                    imageView.setVisibility(View.GONE);
                    controlsBarHost.scheduleControlsBarHide();
                    return;
                }
                imageView.setAlpha((float) progress / (float) seekBar.getMax());
                hideShow.setImageResource(R.drawable.ic_visibility);
                imageView.setVisibility(View.VISIBLE);
                controlsBarHost.scheduleControlsBarHide();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar)  {}
        });
    }
}
