package com.vincentengelsoftware.androidimagecompare;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.vincentengelsoftware.androidimagecompare.Activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;
import com.vincentengelsoftware.androidimagecompare.animations.ResizeAnimation;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapHelper;
import com.vincentengelsoftware.androidimagecompare.helper.Calculator;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SlideHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.viewClasses.VesImageInterface;

public class OverlaySlideActivity extends AppCompatActivity implements FadeActivity {
    /**
     * If problematic, add a single synced access method for both of them
     */
    private static Thread currentThread;
    private static Thread nextThread;

    public static UtilMutableBoolean sync = new UtilMutableBoolean();

    private final static UtilMutableBoolean leftToRight = new UtilMutableBoolean();

    private final static UtilMutableBoolean continueHiding = new UtilMutableBoolean();
    private static Thread fadeOutThread;
    private static Thread fadeInThread;

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

            setContentView(R.layout.activity_overlay_slide);

            VesImageInterface image_back = findViewById(R.id.overlay_slide_image_view_base);
            image_back.setFadeActivity(this);
            Images.first.updateVesImageViewWithAdjustedImage(image_back);

            VesImageInterface image_front = findViewById(R.id.overlay_slide_image_view_front);
            image_front.setFadeActivity(this);
            Bitmap bitmapSource = Images.second.getAdjustedBitmap();

            SyncZoom.setLinkedTargets(image_front, image_back, OverlaySlideActivity.sync);

            ImageButton hideShow = findViewById(R.id.overlay_transparent_button_hide_front_image);

            SeekBar seekBar = findViewById(R.id.overlay_slide_seek_bar);
            this.addSeekbarLogic(seekBar, image_front, leftToRight, bitmapSource, hideShow);
            seekBar.setProgress(50);

            ImageButton swapDirection = findViewById(R.id.overlay_slide_button_swap_seekbar);
            if (leftToRight.value) {
                swapDirection.setImageResource(R.drawable.ic_slide_ltr);
            } else {
                swapDirection.setImageResource(R.drawable.ic_slide_rtl);
            }
            SlideHelper.setSwapSlideDirectionOnClick(
                    swapDirection,
                    seekBar,
                    leftToRight,
                    this
            );

            hideShow.setOnClickListener(view -> {
                instantFadeIn();
                if (image_front.getVisibility() == View.VISIBLE) {
                    hideShow.setImageResource(R.drawable.ic_visibility_off);
                    image_front.setVisibility(View.GONE);
                } else if (leftToRight.value && (seekBar.getProgress() <= 1)) {
                    seekBar.setProgress(2);
                } else if (!leftToRight.value && (seekBar.getProgress() >= 99)) {
                    seekBar.setProgress(98);
                } else {
                    hideShow.setImageResource(R.drawable.ic_visibility);
                    image_front.setVisibility(View.VISIBLE);
                }
                triggerFadeOutThread();
            });

            if (getIntent().getBooleanExtra(IntentExtras.HAS_HARDWARE_KEY, false)) {
                LinearLayout linearLayout = findViewById(R.id.overlay_slide_extensions);
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) linearLayout.getLayoutParams();
                layoutParams.setMargins(0, 0, 0, 0);
                linearLayout.setLayoutParams(layoutParams);
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void onStart() {
        super.onStart();
        triggerFadeOutThread();
    }

    public void triggerFadeIn()
    {
        continueHiding.value = false;
        if (fadeOutThread != null) {
            fadeOutThread.interrupt();
        }

        if (fadeInThread != null) {
            return;
        }

        fadeInThread = new Thread(() -> runOnUiThread(() -> {
            try {
                LinearLayout linearLayout = findViewById(R.id.overlay_slide_extensions);

                ResizeAnimation anim = new ResizeAnimation(
                        linearLayout,
                        Calculator.DpToPx2(48, getResources()),
                        ResizeAnimation.CHANGE_HEIGHT,
                        ResizeAnimation.IS_SHOWING_ANIMATION,
                        continueHiding
                );
                anim.setDuration(ResizeAnimation.DURATION_SHORT);
                linearLayout.clearAnimation();
                linearLayout.startAnimation(anim);
                fadeInThread = null;
                triggerFadeOutThread();
            } catch (Exception ignored) {
            }
        }));

        fadeInThread.start();
    }

    public void triggerFadeOutThread()
    {
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
                    LinearLayout linearLayout = findViewById(R.id.overlay_slide_extensions);

                    continueHiding.value = true;
                    ResizeAnimation anim = new ResizeAnimation(
                            linearLayout,
                            1,
                            ResizeAnimation.CHANGE_HEIGHT,
                            ResizeAnimation.IS_HIDING_ANIMATION,
                            continueHiding
                    );
                    anim.setDuration(ResizeAnimation.DURATION_SHORT);
                    linearLayout.startAnimation(anim);
                    fadeOutThread = null;
                } catch (Exception ignored) {
                }
            });
        });

        fadeOutThread.start();
    }

    public void instantFadeIn()
    {
        continueHiding.value = false;
        runOnUiThread(() -> {
            try {
                LinearLayout linearLayout = findViewById(R.id.overlay_slide_extensions);
                linearLayout.clearAnimation();
                ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
                linearLayout.setVisibility(View.VISIBLE);
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
                                float scale = imageView.getScale();
                                PointF center = imageView.getImageCenter();
                                imageView.setBitmapImage(bitmap);
                                imageView.applyScaleAndCenter(scale, center);
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

    private synchronized void processNextThread(Thread thread)
    {
        if (currentThread == null) {
            currentThread = thread;
            currentThread.start();
        } else {
            nextThread = thread;
        }
    }

    private synchronized void processNextThread()
    {
        if (nextThread != null) {
            currentThread = nextThread;
            nextThread = null;
            currentThread.start();
        }
    }
}
