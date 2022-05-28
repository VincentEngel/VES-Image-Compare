package com.vincentengelsoftware.androidimagecompare;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.vincentengelsoftware.androidimagecompare.helper.BitmapHelper;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SlideHelper;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;

import java.io.InputStream;

public class OverlaySlideActivity extends AppCompatActivity {
    private Bitmap bitmapSource;

    private final UtilMutableBoolean leftToRight = new UtilMutableBoolean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            FullScreenHelper.setFullScreenFlags(this.getWindow());

            setContentView(R.layout.activity_overlay_slide);

            ImageView image_base = findViewById(R.id.overlay_slide_image_view_base);
            image_base.setImageURI(
                    Uri.parse(getIntent().getStringExtra(MainActivity.KEY_URI_IMAGE_FIRST))
            );
            ImageView image_front = findViewById(R.id.overlay_slide_image_view_front);

            bitmapSource = MainActivity.image_holder_second.getBitmapScreenSize();

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