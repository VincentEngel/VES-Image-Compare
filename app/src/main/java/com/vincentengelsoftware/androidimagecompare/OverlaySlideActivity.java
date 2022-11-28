package com.vincentengelsoftware.androidimagecompare;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapHelper;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SlideHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.viewClasses.VesImageInterface;

public class OverlaySlideActivity extends AppCompatActivity {
    /**
     * If problematic, add a single synced access method for both of them
     */
    private static Thread currentThread;
    private static Thread nextThread;

    public static UtilMutableBoolean sync = new UtilMutableBoolean();

    private final static UtilMutableBoolean leftToRight = new UtilMutableBoolean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            if (nextThread != null) {
                nextThread.interrupt();
                nextThread = null;
            }

            if (currentThread != null) {
                currentThread.interrupt();
                nextThread = null;
            }

            if (Status.activityIsOpening) {
                sync.value = Status.SYNCED_ZOOM;
            }

            Status.activityIsOpening = false;

            FullScreenHelper.setFullScreenFlags(this.getWindow());

            setContentView(R.layout.activity_overlay_slide);

            VesImageInterface image_back = findViewById(R.id.overlay_slide_image_view_base);
            Images.first.updateVesImageViewWithAdjustedImage(image_back);

            VesImageInterface image_front = findViewById(R.id.overlay_slide_image_view_front);
            Bitmap bitmapSource = Images.second.getAdjustedBitmap();

            SyncZoom.setLinkedTargets(image_front, image_back, OverlaySlideActivity.sync);

            ImageButton hideShow = findViewById(R.id.overlay_transparent_button_hide_front_image);

            SeekBar seekBar = findViewById(R.id.overlay_slide_seek_bar);
            this.addSeekbarLogic(seekBar, image_front, leftToRight, bitmapSource, hideShow, image_back);
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
                    leftToRight
            );

            hideShow.setOnClickListener(view -> {
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
            });
        } catch (Exception ignored) {}
    }

    private void addSeekbarLogic(
            SeekBar seekBar,
            VesImageInterface imageView,
            UtilMutableBoolean cutFromRightToLeft,
            Bitmap bitmapSource,
            ImageButton hideShow,
            VesImageInterface imageBack
    ) {
        Bitmap transparentBitmap = BitmapHelper.createTransparentBitmap(
                bitmapSource.getWidth(),
                bitmapSource.getHeight()
        );

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (!cutFromRightToLeft.value && (progress >= 99)) {
                    hideShow.setImageResource(R.drawable.ic_visibility_off);
                    imageView.setVisibility(View.GONE);
                    return;
                }

                int width = bitmapSource.getWidth() * progress / 100;

                if (cutFromRightToLeft.value && ((progress <= 1) || width == 0)) {
                    hideShow.setImageResource(R.drawable.ic_visibility_off);
                    imageView.setVisibility(View.GONE);
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
                                if (OverlaySlideActivity.sync.value) {
                                    imageBack.resetScaleAndCenter();
                                }
                                imageView.setBitmapImage(bitmap);
                                hideShow.setImageResource(R.drawable.ic_visibility);
                                imageView.setVisibility(View.VISIBLE);
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
