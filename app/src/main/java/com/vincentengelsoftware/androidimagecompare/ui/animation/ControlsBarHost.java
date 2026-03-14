package com.vincentengelsoftware.androidimagecompare.ui.animation;

/**
 * Callback interface implemented by Activities that host an auto-hiding controls bar.
 *
 * <p>Image views and other UI components call these methods to signal that the
 * controls bar should be shown or its auto-hide timer reset.</p>
 */
public interface ControlsBarHost {

    /** Animates the controls bar back into view, then schedules the next auto-hide. */
    void showControlsBar();

    /**
     * Immediately snaps the controls bar to full height without animation,
     * then schedules the next auto-hide.
     */
    void showControlsBarInstant();

    /** Schedules the controls bar to auto-hide after a short delay. */
    void scheduleControlsBarHide();
}

