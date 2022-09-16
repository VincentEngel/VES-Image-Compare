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

    private final static UtilMutableBoolean leftToRight = new UtilMutableBoolean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Status.activityIsOpening = false;

            FullScreenHelper.setFullScreenFlags(this.getWindow());

            setContentView(R.layout.activity_overlay_slide);

            Images.first.updateVesImageViewWithAdjustedImage(findViewById(R.id.overlay_slide_image_view_base));

            VesImageInterface image_front = findViewById(R.id.overlay_slide_image_view_front);
            Bitmap bitmapSource = Images.second.getAdjustedBitmap();


            ImageButton hideShow = findViewById(R.id.overlay_transparent_button_hide_front_image);

            SeekBar seekBar = findViewById(R.id.overlay_slide_seek_bar);
            SlideHelper.addSeekbarLogic(seekBar, image_front, leftToRight, bitmapSource, hideShow);
            seekBar.setProgress(50);

            ImageButton swapDirection = findViewById(R.id.overlay_slide_button_swap_seekbar);
            if (leftToRight.value) {
                swapDirection.setImageResource(R.drawable.ic_slide_ltr_vector);
            } else {
                swapDirection.setImageResource(R.drawable.ic_slide_rtl_vector);
            }
            SlideHelper.setSwapSlideDirectionOnClick(
                swapDirection,
                seekBar,
                leftToRight
            );

            hideShow.setOnClickListener(view -> {
                if (image_front.getVisibility() == View.VISIBLE) {
                    hideShow.setImageResource(R.drawable.ic_visibility_off_vector);
                    image_front.setVisibility(View.GONE);
                } else {
                    hideShow.setImageResource(R.drawable.ic_visibility_vector);
                    image_front.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception ignored) {}
    }
}
