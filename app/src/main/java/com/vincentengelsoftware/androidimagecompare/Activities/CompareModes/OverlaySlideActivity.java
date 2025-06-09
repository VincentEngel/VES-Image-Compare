package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.vincentengelsoftware.androidimagecompare.Activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;
import com.vincentengelsoftware.androidimagecompare.animations.ResizeAnimation;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityOverlaySlideBinding;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapHelper;
import com.vincentengelsoftware.androidimagecompare.helper.Calculator;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SlideHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.ImageView.ImageScaleCenter;
import com.vincentengelsoftware.androidimagecompare.ImageView.VesImageInterface;

public class OverlaySlideActivity extends AppCompatActivity implements FadeActivity {
    private static Thread currentThread;
    private static Thread nextThread;

    public static UtilMutableBoolean sync = new UtilMutableBoolean(true);
    private final static UtilMutableBoolean leftToRight = new UtilMutableBoolean(true);
    private final static UtilMutableBoolean continueHiding = new UtilMutableBoolean(true);
    private static Thread fadeOutThread;
    private static Thread fadeInThread;

    private ActivityOverlaySlideBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            if (fadeOutThread != null) {
                fadeOutThread.interrupt();
                fadeOutThread = null;
            }

            if (fadeInThread != null) {
                fadeInThread.interrupt();
                fadeInThread = null;
            }

            if (nextThread != null) {
                nextThread.interrupt();
                nextThread = null;
            }

            if (currentThread != null) {
                currentThread.interrupt();
                nextThread = null;
            }

            if (Status.activityIsOpening) {
                sync.value = getIntent().getBooleanExtra(IntentExtras.SYNCED_ZOOM, true);
            }

            Status.activityIsOpening = false;

            FullScreenHelper.setFullScreenFlags(this.getWindow());

            binding = ActivityOverlaySlideBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            binding.overlaySlideImageViewBase.addFadeListener(this);

            try {
                Images.first.updateVesImageViewWithAdjustedImage(binding.overlaySlideImageViewBase);
            } catch (Exception e) {
                this.finish();
            }

            binding.overlaySlideImageViewFront.addFadeListener(this);
            Bitmap bitmapSource = Images.second.getAdjustedBitmap();

            SyncZoom.setLinkedTargets(
                    binding.overlaySlideImageViewFront,
                    binding.overlaySlideImageViewBase,
                    OverlaySlideActivity.sync,
                    new UtilMutableBoolean(false)
            );

            binding.overlayTransparentButtonHideFrontImage.setOnClickListener(view -> {
                instantFadeIn();
                if (binding.overlaySlideImageViewFront.getVisibility() == View.VISIBLE) {
                    binding.overlayTransparentButtonHideFrontImage.setImageResource(R.drawable.ic_visibility_off);
                    binding.overlaySlideImageViewFront.setVisibility(View.GONE);
                } else if (leftToRight.value && (binding.overlaySlideSeekBar.getProgress() <= 1)) {
                    binding.overlaySlideSeekBar.setProgress(2);
                } else if (!leftToRight.value && (binding.overlaySlideSeekBar.getProgress() >= 99)) {
                    binding.overlaySlideSeekBar.setProgress(98);
                } else {
                    binding.overlayTransparentButtonHideFrontImage.setImageResource(R.drawable.ic_visibility);
                    binding.overlaySlideImageViewFront.setVisibility(View.VISIBLE);
                }
                triggerFadeOutThread();
            });

            this.addSeekbarLogic(
                    binding.overlaySlideSeekBar,
                    binding.overlaySlideImageViewFront,
                    leftToRight,
                    bitmapSource,
                    binding.overlayTransparentButtonHideFrontImage
            );
            binding.overlaySlideSeekBar.setProgress(50);

            if (leftToRight.value) {
                binding.overlaySlideButtonSwapSeekbar.setImageResource(R.drawable.ic_slide_ltr);
            } else {
                binding.overlaySlideButtonSwapSeekbar.setImageResource(R.drawable.ic_slide_rtl);
            }

            SlideHelper.setSwapSlideDirectionOnClick(
                    binding.overlaySlideButtonSwapSeekbar,
                    binding.overlaySlideSeekBar,
                    leftToRight,
                    this
            );

            if (getIntent().getBooleanExtra(IntentExtras.HAS_HARDWARE_KEY, false)) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.overlaySlideExtensions.getLayoutParams();
                layoutParams.setMargins(0, 0, 0, 0);
                binding.overlaySlideExtensions.setLayoutParams(layoutParams);
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void onStart() {
        super.onStart();
        triggerFadeOutThread();
    }

    public void triggerFadeIn() {
        continueHiding.value = false;
        if (fadeOutThread != null) {
            fadeOutThread.interrupt();
        }

        if (fadeInThread != null) {
            return;
        }

        fadeInThread = new Thread(() -> runOnUiThread(() -> {
            try {
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
                fadeInThread = null;
                triggerFadeOutThread();
            } catch (Exception ignored) {
            }
        }));

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
                    continueHiding.value = true;
                    ResizeAnimation anim = new ResizeAnimation(
                            binding.overlaySlideExtensions,
                            1,
                            ResizeAnimation.CHANGE_HEIGHT,
                            ResizeAnimation.IS_HIDING_ANIMATION,
                            continueHiding
                    );
                    anim.setDuration(ResizeAnimation.DURATION_SHORT);
                    binding.overlaySlideExtensions.startAnimation(anim);
                    fadeOutThread = null;
                } catch (Exception ignored) {
                }
            });
        });

        fadeOutThread.start();
    }

    public void instantFadeIn() {
        continueHiding.value = false;
        runOnUiThread(() -> {
            try {
                binding.overlaySlideExtensions.clearAnimation();
                ViewGroup.LayoutParams layoutParams = binding.overlaySlideExtensions.getLayoutParams();
                binding.overlaySlideExtensions.setVisibility(View.VISIBLE);
                layoutParams.height = Calculator.DpToPx2(48, getResources());
                fadeInThread = null;
                triggerFadeOutThread();
            } catch (Exception ignored) {
            }
        });
    }

    private void addSeekbarLogic(
            SeekBar seekBar,
            VesImageInterface imageView,
            UtilMutableBoolean cutFromRightToLeft,
            Bitmap bitmapSource,
            ImageButton hideShow
    ) {
        Bitmap transparentBitmap = BitmapHelper.createTransparentBitmap(
                bitmapSource.getWidth(),
                bitmapSource.getHeight()
        );

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                instantFadeIn();
                if (!cutFromRightToLeft.value && (progress >= 99)) {
                    hideShow.setImageResource(R.drawable.ic_visibility_off);
                    imageView.setVisibility(View.GONE);
                    triggerFadeOutThread();
                    return;
                }

                int width = bitmapSource.getWidth() * progress / 100;

                if (cutFromRightToLeft.value && ((progress <= 1) || width == 0)) {
                    hideShow.setImageResource(R.drawable.ic_visibility_off);
                    imageView.setVisibility(View.GONE);
                    triggerFadeOutThread();
                    return;
                }

                processNextThread(
                        new Thread(() -> {
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }

                            Bitmap bitmap = BitmapHelper.getCutBitmapWithTransparentBackgroundWithCanvas(
                                    bitmapSource,
                                    transparentBitmap,
                                    width,
                                    cutFromRightToLeft.value
                            );

                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }

                            runOnUiThread(() -> {
                                ImageScaleCenter imageScaleCenter = imageView.getImageScaleCenter();
                                imageView.setBitmapImage(bitmap);
                                imageView.setImageScaleCenter(imageScaleCenter);
                                hideShow.setImageResource(R.drawable.ic_visibility);
                                imageView.setVisibility(View.VISIBLE);
                                triggerFadeOutThread();
                            });

                            currentThread = null;

                            processNextThread();
                        })
                );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private synchronized void processNextThread(Thread thread) {
        if (currentThread == null) {
            currentThread = thread;
            currentThread.start();
        } else {
            nextThread = thread;
        }
    }

    private synchronized void processNextThread() {
        if (nextThread != null) {
            currentThread = nextThread;
            nextThread = null;
            currentThread.start();
        }
    }
}