package com.vincentengelsoftware.androidimagecompare;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.ImageUpdater;
import com.vincentengelsoftware.androidimagecompare.helper.TransparentHelper;

public class OverlayTransparentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Status.activityIsOpening = false;

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        setContentView(R.layout.activity_overlay_transparent);

        String imageSize = ImageUpdater.SCREEN_SIZE;
        if (Status.keepOriginalSize) {
            imageSize = ImageUpdater.ORIGINAL;
        }

        ImageUpdater.updateSubsamplingScaleImageViewImage(
                findViewById(R.id.overlay_transparent_image_view_base),
                Images.image_holder_first,
                imageSize
        );

        SubsamplingScaleImageView image_transparent = findViewById(R.id.overlay_transparent_image_view_transparent);
        ImageUpdater.updateSubsamplingScaleImageViewImage(
                image_transparent,
                Images.image_holder_second,
                imageSize
        );
        image_transparent.bringToFront();

        SeekBar seekBar = findViewById(R.id.overlay_slide_seek_bar);

        TransparentHelper.makeTargetTransparent(seekBar, image_transparent);

        seekBar.setProgress(seekBar.getMax() / 2);

        ImageButton hideShow = findViewById(R.id.overlay_transparent_button_hide_front_image);
        hideShow.setOnClickListener(view -> {
            if (image_transparent.getVisibility() == View.VISIBLE) {
                image_transparent.setVisibility(View.GONE);
            } else {
                image_transparent.setVisibility(View.VISIBLE);
            }
        });
    }
}