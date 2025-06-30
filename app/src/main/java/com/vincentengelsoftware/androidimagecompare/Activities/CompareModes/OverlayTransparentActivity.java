package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.Activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;
import com.vincentengelsoftware.androidimagecompare.animations.ResizeAnimation;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityOverlayTransparentBinding;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.Calculator;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.helper.TransparentHelper;

import java.util.concurrent.atomic.AtomicBoolean;

public class OverlayTransparentActivity extends AppCompatActivity implements FadeActivity {
    public static AtomicBoolean sync = new AtomicBoolean(true);

    private final static AtomicBoolean continueHiding = new AtomicBoolean(true);
    private static Thread fadeOutThread;
    private static Thread fadeInThread;

    private ActivityOverlayTransparentBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (fadeOutThread != null) {
            fadeOutThread.interrupt();
            fadeOutThread = null;
        }

        if (fadeInThread != null) {
            fadeInThread.interrupt();
            fadeInThread = null;
        }

        if (Status.activityIsOpening) {
            sync.set(getIntent().getBooleanExtra(IntentExtras.SYNCED_ZOOM, true));
        }

        Status.activityIsOpening = false;

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        binding = ActivityOverlayTransparentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.overlayTransparentImageViewBase.addFadeListener(this);
        try {
            Images.first.updateVesImageViewWithAdjustedImage(binding.overlayTransparentImageViewBase);
        } catch (Exception e) {
            this.finish();
        }

        binding.overlayTransparentImageViewTransparent.addFadeListener(this);
        Images.second.updateVesImageViewWithAdjustedImage(binding.overlayTransparentImageViewTransparent);

        binding.overlayTransparentImageViewTransparent.bringToFront();

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
            triggerFadeOutThread();
        });

        SyncZoom.setLinkedTargets(
                binding.overlayTransparentImageViewBase,
                binding.overlayTransparentImageViewTransparent,
                OverlayTransparentActivity.sync,
                new AtomicBoolean(false)
        );
        SyncZoom.setUpSyncZoomToggleButton(
                binding.overlayTransparentImageViewBase,
                binding.overlayTransparentImageViewTransparent,
                binding.overlayTransparentButtonZoomSync,
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off),
                OverlayTransparentActivity.sync,
                this
        );

        if (getIntent().getBooleanExtra(IntentExtras.HAS_HARDWARE_KEY, false)) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.overlayTransparentExtensions.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, 0);
            binding.overlayTransparentExtensions.setLayoutParams(layoutParams);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        triggerFadeOutThread();
    }

    public void triggerFadeIn() {
        continueHiding.set(false);
        if (fadeOutThread != null) {
            fadeOutThread.interrupt();
        }

        if (fadeInThread != null) {
            return;
        }

        fadeInThread = new Thread(() -> {
            runOnUiThread(() -> {
                try {
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
                    fadeInThread = null;
                    triggerFadeOutThread();
                } catch (Exception ignored) {
                }
            });
        });

        fadeInThread.start();
    }

    public void triggerFadeOutThread() {
        if (fadeOutThread != null) {
            fadeOutThread.interrupt();
        }

        fadeOutThread = new Thread(() -> {
            SystemClock.sleep(ResizeAnimation.DURATION_LONG);
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            runOnUiThread(() -> {
                try {
                    continueHiding.set(true);
                    ResizeAnimation anim = new ResizeAnimation(
                            binding.overlayTransparentExtensions,
                            1,
                            ResizeAnimation.CHANGE_HEIGHT,
                            ResizeAnimation.IS_HIDING_ANIMATION,
                            continueHiding
                    );
                    anim.setDuration(ResizeAnimation.DURATION_SHORT);
                    binding.overlayTransparentExtensions.startAnimation(anim);
                    fadeOutThread = null;
                } catch (Exception ignored) {
                }
            });
        });

        fadeOutThread.start();
    }

    public void instantFadeIn() {
        continueHiding.set(false);
        runOnUiThread(() -> {
            try {
                binding.overlayTransparentExtensions.clearAnimation();
                ViewGroup.LayoutParams layoutParams = binding.overlayTransparentExtensions.getLayoutParams();
                binding.overlayTransparentExtensions.setVisibility(View.VISIBLE);
                layoutParams.height = Calculator.DpToPx2(48, getResources());
                fadeInThread = null;
                triggerFadeOutThread();
            } catch (Exception ignored) {
            }
        });
    }
}