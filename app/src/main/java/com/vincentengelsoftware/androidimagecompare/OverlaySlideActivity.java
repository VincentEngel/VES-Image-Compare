package com.vincentengelsoftware.androidimagecompare;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SlideHelper;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.viewClasses.VesImageInterface;

public class OverlaySlideActivity extends AppCompatActivity {

    private final UtilMutableBoolean leftToRight = new UtilMutableBoolean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Status.activityIsOpening = false;

            FullScreenHelper.setFullScreenFlags(this.getWindow());

            setContentView(R.layout.activity_overlay_slide);

            Images.image_holder_first.updateVesImageViewWithAdjustedImage(findViewById(R.id.overlay_slide_image_view_base));

            VesImageInterface image_front = findViewById(R.id.overlay_slide_image_view_front);
            Bitmap bitmapSource = Images.image_holder_second.getAdjustedBitmap();

            SeekBar seekBar = findViewById(R.id.overlay_slide_seek_bar);
            SlideHelper.addSeekbarLogic(seekBar, image_front, leftToRight, bitmapSource);
            seekBar.setProgress(50);

            SlideHelper.setSwapSlideDirectionOnClick(
                    findViewById(R.id.overlay_slide_button_swap_seekbar),
                    seekBar,
                    leftToRight
            );

            ImageButton hideShow = findViewById(R.id.overlay_transparent_button_hide_front_image);
            hideShow.setOnClickListener(view -> {
                if (image_front.getVisibility() == View.VISIBLE) {
                    image_front.setVisibility(View.GONE);
                } else {
                    image_front.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception ignored) {}
    }
}