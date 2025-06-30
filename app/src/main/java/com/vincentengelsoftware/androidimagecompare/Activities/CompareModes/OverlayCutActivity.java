package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.vincentengelsoftware.androidimagecompare.Activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityOverlayCutBinding;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapHelper;
import com.vincentengelsoftware.androidimagecompare.helper.Calculator;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;

import java.util.concurrent.atomic.AtomicBoolean;

public class OverlayCutActivity extends AppCompatActivity {
    public SeekBar recentSeekBar;
    public SeekBar currentSeekBar;

    private static Thread currentThread;
    private static Thread nextThread;

    public static AtomicBoolean sync = new AtomicBoolean(true);

    private static int color_active;
    private static int color_inactive;

    public static Bitmap nextCalculatedBitmap;

    public static Bitmap bitmapSource;
    public static Bitmap bitmapAdjusted;

    private ActivityOverlayCutBinding binding;

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
            OverlayCutActivity.sync.set(getIntent().getBooleanExtra(IntentExtras.SYNCED_ZOOM, true));
        }
        Status.activityIsOpening = false;

        binding = ActivityOverlayCutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FullScreenHelper.setFullScreenFlags(this.getWindow());

        OverlayCutActivity.color_active = getResources().getColor(R.color.orange, null);
        OverlayCutActivity.color_inactive = getResources().getColor(android.R.color.darker_gray, null);

        try {
            Images.first.updateVesImageViewWithAdjustedImage(binding.fullSlideImageViewBase);
            Images.second.updateVesImageViewWithAdjustedImage(binding.fullSlideImageViewFront);
        } catch (Exception e) {
            this.finish();
        }

        SyncZoom.setLinkedTargets(
                binding.fullSlideImageViewFront,
                binding.fullSlideImageViewBase,
                OverlayCutActivity.sync,
                new AtomicBoolean(false)
        );

        ViewGroup.LayoutParams layoutParamsSeekbarLeft = binding.fullSliderSeekbarLeft.getLayoutParams();
        layoutParamsSeekbarLeft.width = Resources.getSystem().getDisplayMetrics().heightPixels - Calculator.DpToPx2(48, getResources());

        ViewGroup.LayoutParams layoutParamsSeekbarRight = binding.fullSliderSeekbarRight.getLayoutParams();
        layoutParamsSeekbarRight.width = Resources.getSystem().getDisplayMetrics().heightPixels - Calculator.DpToPx2(48, getResources());

        if (getIntent().getBooleanExtra(IntentExtras.SHOW_EXTENSIONS, false)) {
            binding.overlayCutBtnReset.setOnClickListener(view -> {
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
            binding.overlayCutBtnCheck.setOnClickListener(view -> {
                OverlayCutActivity.bitmapAdjusted = binding.fullSlideImageViewFront.getCurrentBitmap().copy(Bitmap.Config.ARGB_8888, false);
            });
        } else {
            binding.overlayCutBtnReset.setVisibility(View.INVISIBLE);
            binding.overlayCutBtnCheck.setVisibility(View.INVISIBLE);
        }

        this.addSeekbarLogic(binding.fullSliderSeekbarTop);
        this.addSeekbarLogic(binding.fullSliderSeekbarLeft);
        this.addSeekbarLogic(binding.fullSliderSeekbarRight);
        this.addSeekbarLogic(binding.fullSliderSeekbarBottom);

        binding.fullSliderSeekbarLeft.setProgress(90);
        binding.fullSliderSeekbarRight.setProgress(10);
    }

    private void updateImage(Bitmap bitmapSource) {
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

        if (currentSeekBar.getId() == binding.fullSliderSeekbarTop.getId() || recentSeekBar.getId() == binding.fullSliderSeekbarTop.getId()) {
            topSeekBarActive = true;
            topSeekBarProgress = binding.fullSliderSeekbarTop.getProgress();
        } else {
            topSeekBarProgress = 0;
            topSeekBarActive = false;
        }

        if (currentSeekBar.getId() == binding.fullSliderSeekbarLeft.getId() || recentSeekBar.getId() == binding.fullSliderSeekbarLeft.getId()) {
            leftSeekBarActive = true;
            leftSeekBarProgress = binding.fullSliderSeekbarLeft.getProgress();
        } else {
            leftSeekBarProgress = 0;
            leftSeekBarActive = false;
        }

        if (currentSeekBar.getId() == binding.fullSliderSeekbarRight.getId() || recentSeekBar.getId() == binding.fullSliderSeekbarRight.getId()) {
            rightSeekBarActive = true;
            rightSeekBarProgress = binding.fullSliderSeekbarRight.getProgress();
        } else {
            rightSeekBarProgress = 0;
            rightSeekBarActive = false;
        }

        if (currentSeekBar.getId() == binding.fullSliderSeekbarBottom.getId() || recentSeekBar.getId() == binding.fullSliderSeekbarBottom.getId()) {
            bottomSeekBarActive = true;
            bottomSeekBarProgress = binding.fullSliderSeekbarBottom.getProgress();
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
                            binding.fullSlideImageViewFront.setBitmapImage(OverlayCutActivity.nextCalculatedBitmap);
                        }
                    });

                    currentThread = null;

                    processNextThread();
                })
        );
    }

    private void addSeekbarLogic(SeekBar seekBarView) {
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