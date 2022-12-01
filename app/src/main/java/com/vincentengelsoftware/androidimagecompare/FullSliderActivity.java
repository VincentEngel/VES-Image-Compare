package com.vincentengelsoftware.androidimagecompare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.Calculator;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.viewClasses.VesImageInterface;

public class FullSliderActivity extends AppCompatActivity {
    public SeekBar recentSeekBar;
    public SeekBar currentSeekBar;

    public static UtilMutableBoolean sync = new UtilMutableBoolean();

    private static int color_active;
    private static int color_inactive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Status.activityIsOpening) {
            sync.value = Status.SYNCED_ZOOM;
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
        Bitmap bitmapSource = Images.second.getAdjustedBitmap();

        SyncZoom.setLinkedTargets(image_front, image_back, OverlaySlideActivity.sync);

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
    }

    private void updateImage()
    {

    }

    private void addSeekbarLogic(SeekBar seekBarView)
    {
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

                    updateImage();
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
}