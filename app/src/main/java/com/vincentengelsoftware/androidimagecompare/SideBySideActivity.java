package com.vincentengelsoftware.androidimagecompare;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class SideBySideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_side_by_side);

        SubsamplingScaleImageView image_left = findViewById(R.id.side_by_side_image_left);
        image_left.setImage(
                ImageSource.uri(Uri.parse(getIntent().getStringExtra(MainActivity.KEY_URI_IMAGE_FIRST)))
        );
        SubsamplingScaleImageView image_right = findViewById(R.id.side_by_side_image_right);
        image_right.setImage(
                ImageSource.uri(Uri.parse(getIntent().getStringExtra(MainActivity.KEY_URI_IMAGE_SECOND)))
        );
    }
}