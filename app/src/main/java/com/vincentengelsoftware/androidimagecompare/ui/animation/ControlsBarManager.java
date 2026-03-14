package com.vincentengelsoftware.androidimagecompare.ui.animation;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.vincentengelsoftware.androidimagecompare.util.Calculator;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Encapsulates the animated show/hide behaviour for an Activity's controls bar overlay.
 *
 * <p>The controls bar slides in when the user interacts with the screen and
 * automatically slides out after {@link ResizeAnimation#DURATION_LONG} ms of inactivity.</p>
 *
 * <p>Lifecycle: create in {@code Activity.onCreate()}, call {@link #destroy()} from
 * {@code Activity.onDestroy()} to cancel any pending callbacks and prevent leaks.</p>
 */
public class ControlsBarManager {

    private static final int CONTROLS_BAR_HEIGHT_DP = 48;

    private final View          controlsBarView;
    private final Resources     resources;
    private final AtomicBoolean continueHiding  = new AtomicBoolean(true);
    private final Handler       mainHandler     = new Handler(Looper.getMainLooper());

    private Runnable pendingHideRunnable;
    private boolean  isAnimatingIn = false;

    public ControlsBarManager(@NonNull View controlsBarView, @NonNull Resources resources) {
        this.controlsBarView = controlsBarView;
        this.resources       = resources;
    }

    // ── Public API ──────────────────────────────────────────────────────────────

    /**
     * Animates the controls bar back into view, then schedules the next auto-hide.
     * No-op if a show animation is already in flight.
     */
    public void showAnimated() {
        cancelPendingHide();
        continueHiding.set(false);

        if (isAnimatingIn) return;
        isAnimatingIn = true;

        ResizeAnimation anim = new ResizeAnimation(
                controlsBarView,
                Calculator.dpToPx(CONTROLS_BAR_HEIGHT_DP, resources),
                ResizeAnimation.Dimension.HEIGHT,
                ResizeAnimation.AnimationMode.SHOW,
                continueHiding
        );
        anim.setDuration(ResizeAnimation.DURATION_SHORT);
        controlsBarView.clearAnimation();
        controlsBarView.startAnimation(anim);

        isAnimatingIn = false;
        scheduleHide();
    }

    /**
     * Immediately snaps the controls bar to full height without animation,
     * then schedules the next auto-hide.
     */
    public void showInstant() {
        cancelPendingHide();
        continueHiding.set(false);
        isAnimatingIn = false;

        controlsBarView.clearAnimation();
        controlsBarView.setVisibility(View.VISIBLE);

        ViewGroup.LayoutParams params = controlsBarView.getLayoutParams();
        params.height = Calculator.dpToPx(CONTROLS_BAR_HEIGHT_DP, resources);
        controlsBarView.setLayoutParams(params);

        scheduleHide();
    }

    /**
     * Posts a delayed runnable that slides the controls bar out of view.
     * Cancels and replaces any previously pending hide.
     */
    public void scheduleHide() {
        cancelPendingHide();

        pendingHideRunnable = () -> {
            continueHiding.set(true);
            ResizeAnimation anim = new ResizeAnimation(
                    controlsBarView,
                    1,
                    ResizeAnimation.Dimension.HEIGHT,
                    ResizeAnimation.AnimationMode.HIDE,
                    continueHiding
            );
            anim.setDuration(ResizeAnimation.DURATION_SHORT);
            controlsBarView.startAnimation(anim);
            pendingHideRunnable = null;
        };

        mainHandler.postDelayed(pendingHideRunnable, ResizeAnimation.DURATION_LONG);
    }

    /**
     * Cancels any pending auto-hide runnable.
     * Safe to call at any time, including after {@link #destroy()}.
     */
    public void cancelPendingHide() {
        if (pendingHideRunnable != null) {
            mainHandler.removeCallbacks(pendingHideRunnable);
            pendingHideRunnable = null;
        }
    }

    /**
     * Cancels pending callbacks to prevent Handler leaks.
     * Must be called from the host Activity's {@code onDestroy()}.
     */
    public void destroy() {
        cancelPendingHide();
    }
}

