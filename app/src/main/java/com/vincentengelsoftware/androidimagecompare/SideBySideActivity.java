package com.vincentengelsoftware.androidimagecompare;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.ImageUpdater;

public class SideBySideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Status.activityIsOpening = false;

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        setContentView(R.layout.activity_side_by_side);

        String imageSize = ImageUpdater.SCREEN_SIZE;
        if (Status.keepOriginalSize) {
            imageSize = ImageUpdater.ORIGINAL;
        }

        SubsamplingScaleImageView image_first = findViewById(R.id.side_by_side_image_left);
        ImageUpdater.updateSubsamplingScaleImageViewImage(
                image_first,
                Images.image_holder_first,
                imageSize
        );

        SubsamplingScaleImageView image_second = findViewById(R.id.side_by_side_image_right);
        ImageUpdater.updateSubsamplingScaleImageViewImage(
                image_second,
                Images.image_holder_second,
                imageSize
        );
    }
}