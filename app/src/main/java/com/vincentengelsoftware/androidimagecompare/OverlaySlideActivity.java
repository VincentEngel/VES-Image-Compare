package com.vincentengelsoftware.androidimagecompare;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.ImageUpdater;
import com.vincentengelsoftware.androidimagecompare.helper.SlideHelper;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;

public class OverlaySlideActivity extends AppCompatActivity {

    private final UtilMutableBoolean leftToRight = new UtilMutableBoolean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Status.activityIsOpening = false;

            FullScreenHelper.setFullScreenFlags(this.getWindow());

            setContentView(R.layout.activity_overlay_slide);

            ImageUpdater.updateImage(
                    findViewById(R.id.overlay_slide_image_view_base),
                    Images.image_holder_first,
                    ImageUpdater.SCREEN_SIZE
            );

            ImageView image_front = findViewById(R.id.overlay_slide_image_view_front);
            Bitmap bitmapSource = Images.image_holder_second.getBitmapScreenSize();

            SeekBar seekBar = findViewById(R.id.overlay_slide_seek_bar);
            SlideHelper.addSeekbarLogic(seekBar, image_front, leftToRight, bitmapSource);
            seekBar.setProgress(50);

            SlideHelper.setSwapSlideDirectionOnClick(
                    findViewById(R.id.overlay_slide_button_swap_seekbar),
                    seekBar,
                    leftToRight
            );
        } catch (Exception e) {
            e.printStackTrace();
            this.onBackPressed();
        }
    }
}