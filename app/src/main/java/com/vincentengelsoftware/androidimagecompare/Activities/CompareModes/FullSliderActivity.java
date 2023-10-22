package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.vincentengelsoftware.androidimagecompare.Activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapHelper;
import com.vincentengelsoftware.androidimagecompare.helper.Calculator;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.ImageView.VesImageInterface;

public class FullSliderActivity extends AppCompatActivity {
    public SeekBar recentSeekBar;
    public SeekBar currentSeekBar;

    private static Thread currentThread;
    private static Thread nextThread;

    public static UtilMutableBoolean sync = new UtilMutableBoolean();

    private static int color_active;
    private static int color_inactive;

    public static Bitmap nextCalculatedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (nextThread != null) {
            nextThread.interrupt();
            nextThread = null;
        }

        if (currentThread != null) {
            currentThread.interrupt();
            nextThread = null;
        }

        FullSliderActivity.nextCalculatedBitmap = null;

        super.onCreate(savedInstanceState);
        if (Status.activityIsOpening) {
            FullSliderActivity.sync.value = getIntent().getBooleanExtra(IntentExtras.SYNCED_ZOOM, true);
        }
        Status.activityIsOpening = false;
        setContentView(R.layout.activity_full_slider);
        FullScreenHelper.setFullScreenFlags(this.getWindow());

        FullSliderActivity.color_active = getResources().getColor(R.color.orange, null);
        FullSliderActivity.color_inactive = getResources().getColor(android.R.color.transparent, null);

        VesImageInterface image_back = findViewById(R.id.full_slide_image_view_base);
        Images.first.updateVesImageViewWithAdjustedImage(image_back);

        VesImageInterface image_front = findViewById(R.id.full_slide_image_view_front);
        Images.second.updateVesImageViewWithAdjustedImage(image_front);

        SyncZoom.setLinkedTargets(image_front, image_back, FullSliderActivity.sync);

        SeekBar seekBarLeft = findViewById(R.id.full_slider_seekbar_left);
        ViewGroup.LayoutParams layoutParamsSeekbarLeft = seekBarLeft.getLayoutParams();
        layoutParamsSeekbarLeft.width = Resources.getSystem().getDisplayMetrics().heightPixels - Calculator.DpToPx2(48, getResources());

        SeekBar seekBarRight = findViewById(R.id.full_slider_seekbar_right);
        ViewGroup.LayoutParams layoutParamsSeekbarRight = seekBarRight.getLayoutParams();
        layoutParamsSeekbarRight.width = Resources.getSystem().getDisplayMetrics().heightPixels - Calculator.DpToPx2(48, getResources());

        SeekBar seekBarTop = findViewById(R.id.full_slider_seekbar_top);
        SeekBar seekBarBottom = findViewById(R.id.full_slider_seekbar_bottom);


        this.addSeekbarLogic(seekBarTop);
        this.addSeekbarLogic(seekBarLeft);
        this.addSeekbarLogic(seekBarRight);
        this.addSeekbarLogic(seekBarBottom);

        seekBarLeft.setProgress(100);
        seekBarRight.setProgress(1);
    }

    private void updateImage(Bitmap bitmapSource)
    {
        if (currentSeekBar == null || recentSeekBar == null) {
            return;
        }

        boolean topSeekBarActive = false;
        int topSeekBarProgress = 0;
        boolean leftSeekBarActive = false;
        int leftSeekBarProgress = 0;
        boolean rightSeekBarActive = false;
        int rightSeekBarProgress = 0;
        boolean bottomSeekBarActive = false;
        int bottomSeekBarProgress = 0;

        SeekBar seekBarTop = findViewById(R.id.full_slider_seekbar_top);
        SeekBar seekBarLeft = findViewById(R.id.full_slider_seekbar_left);
        SeekBar seekBarRight = findViewById(R.id.full_slider_seekbar_right);
        SeekBar seekBarBottom = findViewById(R.id.full_slider_seekbar_bottom);

        if (currentSeekBar.getId() == seekBarTop.getId() || recentSeekBar.getId() == seekBarTop.getId()) {
            topSeekBarActive = true;
            topSeekBarProgress = seekBarTop.getProgress();
        }

        if (currentSeekBar.getId() == seekBarLeft.getId() || recentSeekBar.getId() == seekBarLeft.getId()) {
            leftSeekBarActive = true;
            leftSeekBarProgress = seekBarLeft.getProgress();
        }

        if (currentSeekBar.getId() == seekBarRight.getId() || recentSeekBar.getId() == seekBarRight.getId()) {
            rightSeekBarActive = true;
            rightSeekBarProgress = seekBarRight.getProgress();
        }

        if (currentSeekBar.getId() == seekBarBottom.getId() || recentSeekBar.getId() == seekBarBottom.getId()) {
            bottomSeekBarActive = true;
            bottomSeekBarProgress = seekBarBottom.getProgress();
        }

        processNextThread(
                new Thread(() -> {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }

                    Bitmap bitmap = BitmapHelper.cutBitmapBetweenFromLeft(
                            bitmapSource,
                            0,
                            0,
                            0,
                            0
                    );

                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }

                    FullSliderActivity.nextCalculatedBitmap = bitmap;

                    runOnUiThread(() -> {
                        if (FullSliderActivity.nextCalculatedBitmap != null) {
                            if (FullSliderActivity.sync.value) {
                                VesImageInterface image_back = findViewById(R.id.full_slide_image_view_base);
                                image_back.resetZoom();
                            }

                            VesImageInterface image_front = findViewById(R.id.full_slide_image_view_front);
                            //image_front.setBitmapImage(FullSliderActivity.nextCalculatedBitmap);
                        }
                    });

                    currentThread = null;

                    processNextThread();
                })
        );
    }

    private void addSeekbarLogic(SeekBar seekBarView)
    {
        Bitmap bitmapSource = Images.second.getAdjustedBitmap();

        seekBarView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.getThumb().setTint(FullSliderActivity.color_active);
                try {
                    if (currentSeekBar == null) {
                        currentSeekBar = seekBar;
                        return;
                    }

                    if (recentSeekBar != null && seekBar.getId() != currentSeekBar.getId() && seekBar.getId() != recentSeekBar.getId()) {
                        recentSeekBar.getThumb().setTint(FullSliderActivity.color_inactive);
                    }

                    if (seekBar.getId() != currentSeekBar.getId()) {
                        recentSeekBar = currentSeekBar;
                        currentSeekBar = seekBar;
                    }

                    updateImage(bitmapSource);
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
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