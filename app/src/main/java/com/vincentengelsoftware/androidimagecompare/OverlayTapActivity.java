package com.vincentengelsoftware.androidimagecompare;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.TapHelper;

public class OverlayTapActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        setContentView(R.layout.activity_overlay_tap);

        ImageView image_first = findViewById(R.id.overlay_tap_image_view_one);
        image_first.setImageURI(
                Uri.parse(getIntent().getStringExtra(MainActivity.KEY_URI_IMAGE_FIRST))
        );
        ImageView image_second = findViewById(R.id.overlay_tap_image_view_two);
        image_second.setImageURI(
                Uri.parse(getIntent().getStringExtra(MainActivity.KEY_URI_IMAGE_SECOND))
        );
        image_second.setVisibility(View.INVISIBLE);

        TapHelper.setOnClickListener(image_first, image_second);
        TapHelper.setOnClickListener(image_second, image_first);
    }
}