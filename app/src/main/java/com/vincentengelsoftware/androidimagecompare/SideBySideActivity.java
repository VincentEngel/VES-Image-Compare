package com.vincentengelsoftware.androidimagecompare;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;

public class SideBySideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Status.activityIsOpening = false;

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        setContentView(R.layout.activity_side_by_side);

        SubsamplingScaleImageView image_left = findViewById(R.id.side_by_side_image_left);
        image_left.setImage(
                ImageSource.bitmap(Images.image_holder_first.rotatedBitmap)
        );
        SubsamplingScaleImageView image_right = findViewById(R.id.side_by_side_image_right);
        image_right.setImage(
                ImageSource.bitmap(Images.image_holder_second.rotatedBitmap)
        );
    }
}