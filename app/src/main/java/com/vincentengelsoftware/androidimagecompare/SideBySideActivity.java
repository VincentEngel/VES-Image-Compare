package com.vincentengelsoftware.androidimagecompare;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

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

        Images.first.updateVesImageViewWithAdjustedImage(findViewById(R.id.side_by_side_image_left));

        Images.second.updateVesImageViewWithAdjustedImage(findViewById(R.id.side_by_side_image_right));
    }
}