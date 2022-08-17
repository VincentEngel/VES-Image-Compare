package com.vincentengelsoftware.androidimagecompare;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.ImageUpdater;
import com.vincentengelsoftware.androidimagecompare.helper.TapHelper;

public class OverlayTapActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Status.activityIsOpening = false;

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        setContentView(R.layout.activity_overlay_tap);

        String imageSize = ImageUpdater.SCREEN_SIZE;
        if (Status.keepOriginalSize) {
            imageSize = ImageUpdater.ORIGINAL;
        }

        ImageView image_first = findViewById(R.id.overlay_tap_image_view_one);
        ImageUpdater.updateImage(
                image_first,
                Images.image_holder_first,
                imageSize
        );

        ImageView image_second = findViewById(R.id.overlay_tap_image_view_two);
        ImageUpdater.updateImage(
                image_second,
                Images.image_holder_second,
                imageSize
        );

        image_second.setVisibility(View.INVISIBLE);

        TapHelper.setOnClickListener(image_first, image_second);
        TapHelper.setOnClickListener(image_second, image_first);
    }
}