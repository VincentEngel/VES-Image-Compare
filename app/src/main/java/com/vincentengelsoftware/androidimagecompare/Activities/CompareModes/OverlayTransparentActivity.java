package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.Activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;
import com.vincentengelsoftware.androidimagecompare.animations.ResizeAnimation;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityOverlayTransparentBinding;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapExtractor;
import com.vincentengelsoftware.androidimagecompare.helper.Calculator;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.helper.TransparentHelper;

import java.util.concurrent.atomic.AtomicBoolean;

public class OverlayTransparentActivity extends AppCompatActivity implements FadeActivity {

    private static final String KEY_SYNC_IMAGE_INTERACTIONS = "key_sync_image_interactions";

    /** Sync state retained across config changes via savedInstanceState. */
    private final AtomicBoolean sync = new AtomicBoolean(true);

    /** Controls whether the auto-hide animation should proceed (cancelled on user interaction). */
    private final AtomicBoolean continueHiding = new AtomicBoolean(true);

    /** Handler bound to the main thread – used for all deferred UI work. */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** Pending auto-hide runnable. Cancelled whenever user interaction resets the timer. */
    private Runnable pendingFadeOutRunnable;

    /** True while a fade-in animation is in flight, prevents stacking identical animations. */
    private boolean isFadingIn = false;

    private ActivityOverlayTransparentBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // On first launch read the sync state from the Intent;
        // on configuration changes restore it from savedInstanceState.
        if (savedInstanceState != null) {
            sync.set(savedInstanceState.getBoolean(KEY_SYNC_IMAGE_INTERACTIONS, true));
        } else {
            sync.set(getIntent().getBooleanExtra(IntentExtras.SYNC_IMAGE_INTERACTIONS, true));
        }

        FullScreenHelper.setFullScreenFlags(getWindow());

        binding = ActivityOverlayTransparentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initImages();
        initControls();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SYNC_IMAGE_INTERACTIONS, sync.get());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Always snap the controls bar into view on resume so that it is
        // visible after a configuration change (e.g. rotation), even when
        // Android's view-state restoration had set the bar to INVISIBLE.
        // instantFadeIn() internally schedules the next auto-hide.
        instantFadeIn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelPendingFadeOut();
        binding = null;
    }

    private void initImages() {
        String uriOne = getIntent().getStringExtra(IntentExtras.IMAGE_URI_ONE);
        String uriTwo = getIntent().getStringExtra(IntentExtras.IMAGE_URI_TWO);

        Bitmap bitmapFirst  = BitmapExtractor.fromUriString(getContentResolver(), uriOne);
        Bitmap bitmapSecond = BitmapExtractor.fromUriString(getContentResolver(), uriTwo);

        binding.overlayTransparentImageViewBase.addFadeListener(this);
        try {
            binding.overlayTransparentImageViewBase.setBitmapImage(bitmapFirst);
        } catch (Exception e) {
            finish();
            return;
        }

        binding.overlayTransparentImageViewTransparent.addFadeListener(this);
        binding.overlayTransparentImageViewTransparent.setBitmapImage(bitmapSecond);
        binding.overlayTransparentImageViewTransparent.bringToFront();
    }

    private void initControls() {
        TransparentHelper.makeTargetTransparent(
                binding.overlaySlideSeekBar,
                binding.overlayTransparentImageViewTransparent,
                binding.overlayTransparentButtonHideFrontImage,
                this
        );

        binding.overlaySlideSeekBar.setProgress(50);

        binding.overlayTransparentButtonHideFrontImage.setOnClickListener(view -> {
            instantFadeIn();
            if (binding.overlayTransparentImageViewTransparent.getVisibility() == View.VISIBLE) {
                binding.overlayTransparentButtonHideFrontImage.setImageResource(R.drawable.ic_visibility_off);
                binding.overlayTransparentImageViewTransparent.setVisibility(View.GONE);
            } else if (binding.overlaySlideSeekBar.getProgress() <= 2) {
                binding.overlaySlideSeekBar.setProgress(3);
            } else {
                binding.overlayTransparentButtonHideFrontImage.setImageResource(R.drawable.ic_visibility);
                binding.overlayTransparentImageViewTransparent.setVisibility(View.VISIBLE);
            }
            scheduleFadeOut();
        });

        SyncZoom.setLinkedTargets(
                binding.overlayTransparentImageViewBase,
                binding.overlayTransparentImageViewTransparent,
                sync
        );
        SyncZoom.setUpSyncZoomToggleButton(
                binding.overlayTransparentImageViewBase,
                binding.overlayTransparentImageViewTransparent,
                binding.overlayTransparentButtonZoomSync,
                ContextCompat.getDrawable(this, R.drawable.ic_link),
                ContextCompat.getDrawable(this, R.drawable.ic_link_off),
                sync
        );

        if (getIntent().getBooleanExtra(IntentExtras.HAS_HARDWARE_KEY, false)) {
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) binding.overlayTransparentExtensions.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, 0);
            binding.overlayTransparentExtensions.setLayoutParams(layoutParams);
        }
    }

    /**
     * Animates the controls bar back into view, then schedules the next auto-hide.
     * No-op if a fade-in is already running.
     */
    @Override
    public void triggerFadeIn() {
        cancelPendingFadeOut();
        continueHiding.set(false);

        if (isFadingIn) {
            return;
        }
        isFadingIn = true;

        ResizeAnimation anim = new ResizeAnimation(
                binding.overlayTransparentExtensions,
                Calculator.DpToPx2(48, getResources()),
                ResizeAnimation.CHANGE_HEIGHT,
                ResizeAnimation.IS_SHOWING_ANIMATION,
                continueHiding
        );
        anim.setDuration(ResizeAnimation.DURATION_SHORT);
        binding.overlayTransparentExtensions.clearAnimation();
        binding.overlayTransparentExtensions.startAnimation(anim);

        isFadingIn = false;
        scheduleFadeOut();
    }

    /**
     * Schedules the controls bar to auto-hide after {@link ResizeAnimation#DURATION_LONG} ms.
     * Any previously pending auto-hide is cancelled first.
     */
    @Override
    public void triggerFadeOutThread() {
        scheduleFadeOut();
    }

    /**
     * Immediately snaps the controls bar to full height without animation,
     * then schedules the next auto-hide.
     */
    @Override
    public void instantFadeIn() {
        cancelPendingFadeOut();
        continueHiding.set(false);
        isFadingIn = false;

        binding.overlayTransparentExtensions.clearAnimation();
        binding.overlayTransparentExtensions.setVisibility(View.VISIBLE);

        ViewGroup.LayoutParams layoutParams = binding.overlayTransparentExtensions.getLayoutParams();
        layoutParams.height = Calculator.DpToPx2(48, getResources());
        binding.overlayTransparentExtensions.setLayoutParams(layoutParams);

        scheduleFadeOut();
    }

    /**
     * Posts a delayed runnable on the main thread that hides the controls bar.
     * Replaces any previously scheduled hide.
     */
    private void scheduleFadeOut() {
        cancelPendingFadeOut();

        pendingFadeOutRunnable = () -> {
            if (binding == null) {
                return;
            }
            continueHiding.set(true);
            ResizeAnimation resizeAnimation = new ResizeAnimation(
                    binding.overlayTransparentExtensions,
                    1,
                    ResizeAnimation.CHANGE_HEIGHT,
                    ResizeAnimation.IS_HIDING_ANIMATION,
                    continueHiding
            );
            resizeAnimation.setDuration(ResizeAnimation.DURATION_SHORT);
            binding.overlayTransparentExtensions.startAnimation(resizeAnimation);
            pendingFadeOutRunnable = null;
        };

        mainHandler.postDelayed(pendingFadeOutRunnable, ResizeAnimation.DURATION_LONG);
    }

    /** Removes the pending auto-hide runnable from the main thread queue. */
    private void cancelPendingFadeOut() {
        if (pendingFadeOutRunnable != null) {
            mainHandler.removeCallbacks(pendingFadeOutRunnable);
            pendingFadeOutRunnable = null;
        }
    }
}