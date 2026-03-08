package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vincentengelsoftware.androidimagecompare.Activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;
import com.vincentengelsoftware.androidimagecompare.animations.ResizeAnimation;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityOverlaySlideBinding;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapExtractor;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapHelper;
import com.vincentengelsoftware.androidimagecompare.helper.Calculator;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SlideHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.ImageView.ImageScaleCenter;
import com.vincentengelsoftware.androidimagecompare.ImageView.VesImageInterface;

import java.util.concurrent.atomic.AtomicBoolean;

public class OverlaySlideActivity extends AppCompatActivity implements FadeActivity {

    private static final String KEY_SYNC_IMAGE_INTERACTIONS = "key_sync_image_interactions";
    private static final String KEY_LEFT_TO_RIGHT = "key_left_to_right";

    /** Sync state retained across configuration changes via savedInstanceState. */
    private final AtomicBoolean sync = new AtomicBoolean(true);

    /** Slide direction: true = left-to-right, false = right-to-left. */
    private final AtomicBoolean leftToRight = new AtomicBoolean(true);

    /** Controls whether the auto-hide animation should proceed. */
    private final AtomicBoolean continueHiding = new AtomicBoolean(true);

    /** Handler bound to the main thread – used for all deferred UI work. */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** Pending auto-hide runnable. Cancelled on every user interaction. */
    private Runnable pendingFadeOutRunnable;

    /** True while a fade-in animation is in flight, prevents stacking animations. */
    private boolean isFadingIn = false;

    /** Background thread processing the current seekbar bitmap crop request. */
    private Thread currentThread;

    /** Next queued bitmap crop request, run after currentThread finishes. */
    private Thread nextThread;

    private ActivityOverlaySlideBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore or initialise state
        if (savedInstanceState != null) {
            sync.set(savedInstanceState.getBoolean(KEY_SYNC_IMAGE_INTERACTIONS, true));
            leftToRight.set(savedInstanceState.getBoolean(KEY_LEFT_TO_RIGHT, true));
        } else {
            sync.set(getIntent().getBooleanExtra(IntentExtras.SYNC_IMAGE_INTERACTIONS, true));
        }

        FullScreenHelper.setFullScreenFlags(getWindow());

        binding = ActivityOverlaySlideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initImages();
        initControls();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SYNC_IMAGE_INTERACTIONS, sync.get());
        outState.putBoolean(KEY_LEFT_TO_RIGHT, leftToRight.get());
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
        interruptBitmapThreads();
        binding = null;
    }

    private void initImages() {
        String uriOne = getIntent().getStringExtra(IntentExtras.IMAGE_URI_ONE);
        String uriTwo = getIntent().getStringExtra(IntentExtras.IMAGE_URI_TWO);

        Bitmap bitmapBase = BitmapExtractor.fromUriString(getContentResolver(), uriOne);
        Bitmap bitmapSource = BitmapExtractor.fromUriString(getContentResolver(), uriTwo);

        binding.overlaySlideImageViewBase.addFadeListener(this);
        try {
            binding.overlaySlideImageViewBase.setBitmapImage(bitmapBase);
        } catch (Exception e) {
            finish();
            return;
        }

        binding.overlaySlideImageViewFront.addFadeListener(this);

        SyncZoom.setLinkedTargets(
                binding.overlaySlideImageViewFront,
                binding.overlaySlideImageViewBase,
                sync
        );

        addSeekBarLogic(
                binding.overlaySlideSeekBar,
                binding.overlaySlideImageViewFront,
                leftToRight,
                bitmapSource,
                binding.overlayTransparentButtonHideFrontImage
        );
    }

    private void initControls() {
        binding.overlaySlideSeekBar.setProgress(50);

        updateSlideDirectionIcon();

        binding.overlayTransparentButtonHideFrontImage.setOnClickListener(view -> {
            instantFadeIn();
            if (binding.overlaySlideImageViewFront.getVisibility() == View.VISIBLE) {
                binding.overlayTransparentButtonHideFrontImage.setImageResource(R.drawable.ic_visibility_off);
                binding.overlaySlideImageViewFront.setVisibility(View.GONE);
            } else if (leftToRight.get() && binding.overlaySlideSeekBar.getProgress() <= 1) {
                binding.overlaySlideSeekBar.setProgress(2);
            } else if (!leftToRight.get() && binding.overlaySlideSeekBar.getProgress() >= 99) {
                binding.overlaySlideSeekBar.setProgress(98);
            } else {
                binding.overlayTransparentButtonHideFrontImage.setImageResource(R.drawable.ic_visibility);
                binding.overlaySlideImageViewFront.setVisibility(View.VISIBLE);
            }
            scheduleFadeOut();
        });

        SlideHelper.setSwapSlideDirectionOnClick(
                binding.overlaySlideButtonSwapSeekbar,
                binding.overlaySlideSeekBar,
                leftToRight,
                this
        );

        if (getIntent().getBooleanExtra(IntentExtras.HAS_HARDWARE_KEY, false)) {
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) binding.overlaySlideExtensions.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, 0);
            binding.overlaySlideExtensions.setLayoutParams(layoutParams);
        }
    }

    private void updateSlideDirectionIcon() {
        binding.overlaySlideButtonSwapSeekbar.setImageResource(
                leftToRight.get() ? R.drawable.ic_slide_ltr : R.drawable.ic_slide_rtl
        );
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
                binding.overlaySlideExtensions,
                Calculator.DpToPx2(48, getResources()),
                ResizeAnimation.CHANGE_HEIGHT,
                ResizeAnimation.IS_SHOWING_ANIMATION,
                continueHiding
        );
        anim.setDuration(ResizeAnimation.DURATION_SHORT);
        binding.overlaySlideExtensions.clearAnimation();
        binding.overlaySlideExtensions.startAnimation(anim);

        isFadingIn = false;
        scheduleFadeOut();
    }

    /** Delegates to {@link #scheduleFadeOut()} – kept for interface compatibility. */
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

        binding.overlaySlideExtensions.clearAnimation();
        binding.overlaySlideExtensions.setVisibility(View.VISIBLE);

        ViewGroup.LayoutParams layoutParams = binding.overlaySlideExtensions.getLayoutParams();
        layoutParams.height = Calculator.DpToPx2(48, getResources());
        binding.overlaySlideExtensions.setLayoutParams(layoutParams);

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
            ResizeAnimation anim = new ResizeAnimation(
                    binding.overlaySlideExtensions,
                    1,
                    ResizeAnimation.CHANGE_HEIGHT,
                    ResizeAnimation.IS_HIDING_ANIMATION,
                    continueHiding
            );
            anim.setDuration(ResizeAnimation.DURATION_SHORT);
            binding.overlaySlideExtensions.startAnimation(anim);
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

    private void addSeekBarLogic(
            SeekBar seekBar,
            VesImageInterface imageView,
            AtomicBoolean cutFromRightToLeft,
            Bitmap bitmapSource,
            ImageButton hideShow
    ) {
        Bitmap transparentBitmap = BitmapHelper.createTransparentBitmap(
                bitmapSource.getWidth(),
                bitmapSource.getHeight()
        );

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                instantFadeIn();

                if (!cutFromRightToLeft.get() && progress >= 99) {
                    hideShow.setImageResource(R.drawable.ic_visibility_off);
                    imageView.setVisibility(View.GONE);
                    scheduleFadeOut();
                    return;
                }

                int width = bitmapSource.getWidth() * progress / 100;

                if (cutFromRightToLeft.get() && (progress <= 1 || width == 0)) {
                    hideShow.setImageResource(R.drawable.ic_visibility_off);
                    imageView.setVisibility(View.GONE);
                    scheduleFadeOut();
                    return;
                }

                enqueueBitmapCrop(new Thread(() -> {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }

                    Bitmap bitmap = BitmapHelper.getCutBitmapWithTransparentBackgroundWithCanvas(
                            bitmapSource,
                            transparentBitmap,
                            width,
                            cutFromRightToLeft.get()
                    );

                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }

                    mainHandler.post(() -> {
                        if (binding == null) {
                            return;
                        }
                        ImageScaleCenter imageScaleCenter = imageView.getImageScaleCenter();
                        imageView.setBitmapImage(bitmap);
                        imageView.setImageScaleCenter(imageScaleCenter);
                        hideShow.setImageResource(R.drawable.ic_visibility);
                        imageView.setVisibility(View.VISIBLE);
                        scheduleFadeOut();
                    });

                    currentThread = null;
                    drainBitmapQueue();
                }));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    /** Starts {@code thread} immediately if no crop is running, otherwise queues it. */
    private synchronized void enqueueBitmapCrop(Thread thread) {
        if (currentThread == null) {
            currentThread = thread;
            currentThread.start();
        } else {
            nextThread = thread;
        }
    }

    /** Promotes the queued thread (if any) once the current one has finished. */
    private synchronized void drainBitmapQueue() {
        if (nextThread != null) {
            currentThread = nextThread;
            nextThread = null;
            currentThread.start();
        }
    }

    /** Interrupts any in-flight bitmap threads (called from onDestroy). */
    private synchronized void interruptBitmapThreads() {
        if (nextThread != null) {
            nextThread.interrupt();
            nextThread = null;
        }
        if (currentThread != null) {
            currentThread.interrupt();
            currentThread = null;
        }
    }
}
