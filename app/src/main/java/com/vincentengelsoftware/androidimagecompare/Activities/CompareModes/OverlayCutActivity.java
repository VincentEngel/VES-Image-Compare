package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class OverlayCutActivity extends AppCompatActivity {
    public SeekBar recentSeekBar;
    public SeekBar currentSeekBar;

    private static Thread currentThread;
    private static Thread nextThread;

    public static UtilMutableBoolean sync = new UtilMutableBoolean(true);

    private static int color_active;
    private static int color_inactive;

    public static Bitmap nextCalculatedBitmap;

    public static Bitmap bitmapSource;
    public static Bitmap bitmapAdjusted;

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

        OverlayCutActivity.nextCalculatedBitmap = null;

        super.onCreate(savedInstanceState);
        if (Status.activityIsOpening) {
            OverlayCutActivity.bitmapSource = Images.second.getAdjustedBitmap();
            OverlayCutActivity.bitmapAdjusted = Images.second.getAdjustedBitmap();
            OverlayCutActivity.sync.value = getIntent().getBooleanExtra(IntentExtras.SYNCED_ZOOM, true);
        }
        Status.activityIsOpening = false;
        setContentView(R.layout.activity_overlay_cut);
        FullScreenHelper.setFullScreenFlags(this.getWindow());

        OverlayCutActivity.color_active = getResources().getColor(R.color.orange, null);
        OverlayCutActivity.color_inactive = getResources().getColor(android.R.color.darker_gray, null);

        VesImageInterface image_back = findViewById(R.id.full_slide_image_view_base);
        Images.first.updateVesImageViewWithAdjustedImage(image_back);

        VesImageInterface image_front = findViewById(R.id.full_slide_image_view_front);
        Images.second.updateVesImageViewWithAdjustedImage(image_front);

        SyncZoom.setLinkedTargets(image_front, image_back, OverlayCutActivity.sync, new UtilMutableBoolean(false));

        SeekBar seekBarLeft = findViewById(R.id.full_slider_seekbar_left);
        ViewGroup.LayoutParams layoutParamsSeekbarLeft = seekBarLeft.getLayoutParams();
        layoutParamsSeekbarLeft.width = Resources.getSystem().getDisplayMetrics().heightPixels - Calculator.DpToPx2(48, getResources());

        SeekBar seekBarRight = findViewById(R.id.full_slider_seekbar_right);
        ViewGroup.LayoutParams layoutParamsSeekbarRight = seekBarRight.getLayoutParams();
        layoutParamsSeekbarRight.width = Resources.getSystem().getDisplayMetrics().heightPixels - Calculator.DpToPx2(48, getResources());

        SeekBar seekBarTop = findViewById(R.id.full_slider_seekbar_top);
        SeekBar seekBarBottom = findViewById(R.id.full_slider_seekbar_bottom);

        ImageButton buttonReset = findViewById(R.id.overlay_cut_btn_reset);
        buttonReset.setOnClickListener(view -> {
            OverlayCutActivity.bitmapAdjusted = OverlayCutActivity.bitmapSource;
            if (currentSeekBar != null) {
                int progress = currentSeekBar.getProgress();
                if (progress > 1) {
                    currentSeekBar.setProgress(progress - 1);
                } else {
                    currentSeekBar.setProgress(1);
                }
            }
        });

        ImageButton buttonKeep = findViewById(R.id.overlay_cut_btn_check);
        buttonKeep.setOnClickListener(view -> {
            OverlayCutActivity.bitmapAdjusted = image_front.getCurrentBitmap().copy(Bitmap.Config.ARGB_8888, false);
        });

        this.addSeekbarLogic(seekBarTop);
        this.addSeekbarLogic(seekBarLeft);
        this.addSeekbarLogic(seekBarRight);
        this.addSeekbarLogic(seekBarBottom);

        seekBarLeft.setProgress(90);
        seekBarRight.setProgress(10);
    }

    private void updateImage(Bitmap bitmapSource)
    {
        if (currentSeekBar == null || recentSeekBar == null) {
            return;
        }

        boolean topSeekBarActive;
        int topSeekBarProgress;
        boolean leftSeekBarActive;
        int leftSeekBarProgress;
        boolean rightSeekBarActive;
        int rightSeekBarProgress;
        boolean bottomSeekBarActive;
        int bottomSeekBarProgress;

        SeekBar seekBarTop = findViewById(R.id.full_slider_seekbar_top);
        SeekBar seekBarLeft = findViewById(R.id.full_slider_seekbar_left);
        SeekBar seekBarRight = findViewById(R.id.full_slider_seekbar_right);
        SeekBar seekBarBottom = findViewById(R.id.full_slider_seekbar_bottom);

        if (currentSeekBar.getId() == seekBarTop.getId() || recentSeekBar.getId() == seekBarTop.getId()) {
            topSeekBarActive = true;
            topSeekBarProgress = seekBarTop.getProgress();
        } else {
            topSeekBarProgress = 0;
            topSeekBarActive = false;
        }

        if (currentSeekBar.getId() == seekBarLeft.getId() || recentSeekBar.getId() == seekBarLeft.getId()) {
            leftSeekBarActive = true;
            leftSeekBarProgress = seekBarLeft.getProgress();
        } else {
            leftSeekBarProgress = 0;
            leftSeekBarActive = false;
        }

        if (currentSeekBar.getId() == seekBarRight.getId() || recentSeekBar.getId() == seekBarRight.getId()) {
            rightSeekBarActive = true;
            rightSeekBarProgress = seekBarRight.getProgress();
        } else {
            rightSeekBarProgress = 0;
            rightSeekBarActive = false;
        }

        if (currentSeekBar.getId() == seekBarBottom.getId() || recentSeekBar.getId() == seekBarBottom.getId()) {
            bottomSeekBarActive = true;
            bottomSeekBarProgress = seekBarBottom.getProgress();
        } else {
            bottomSeekBarProgress = 0;
            bottomSeekBarActive = false;
        }

        processNextThread(
                new Thread(() -> {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }

                    Bitmap bitmap = BitmapHelper.cutBitmapAny(
                            bitmapSource,
                            topSeekBarActive,
                            topSeekBarProgress,
                            leftSeekBarActive,
                            leftSeekBarProgress,
                            rightSeekBarActive,
                            rightSeekBarProgress,
                            bottomSeekBarActive,
                            bottomSeekBarProgress
                    );

                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }

                    OverlayCutActivity.nextCalculatedBitmap = bitmap;

                    runOnUiThread(() -> {
                        if (OverlayCutActivity.nextCalculatedBitmap != null) {

                            VesImageInterface image_front = findViewById(R.id.full_slide_image_view_front);
                            image_front.setBitmapImage(OverlayCutActivity.nextCalculatedBitmap);
                        }
                    });

                    currentThread = null;

                    processNextThread();
                })
        );
    }

    private void addSeekbarLogic(SeekBar seekBarView)
    {
        seekBarView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.getThumb().setTint(OverlayCutActivity.color_active);
                try {
                    if (currentSeekBar == null) {
                        currentSeekBar = seekBar;
                        return;
                    }

                    if (seekBar.getId() != currentSeekBar.getId()) {
                        if (recentSeekBar != null && seekBar.getId() != recentSeekBar.getId()) {
                            recentSeekBar.getThumb().setTint(OverlayCutActivity.color_inactive);
                        }
                        recentSeekBar = currentSeekBar;
                        currentSeekBar = seekBar;
                    }

                    updateImage(OverlayCutActivity.bitmapAdjusted.copy(Bitmap.Config.ARGB_8888, true));
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