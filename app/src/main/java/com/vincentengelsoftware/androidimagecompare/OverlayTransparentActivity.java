package com.vincentengelsoftware.androidimagecompare;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.vincentengelsoftware.androidimagecompare.helper.TransparentHelper;

public class OverlayTransparentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_overlay_transparent);

        ImageView image_base = findViewById(R.id.overlay_transparent_image_view_base);
        image_base.setImageURI(
                Uri.parse(getIntent().getStringExtra(MainActivity.KEY_URI_IMAGE_FIRST))
        );
        ImageView image_transparent = findViewById(R.id.overlay_transparent_image_view_transparent);
        image_transparent.setImageURI(
                Uri.parse(getIntent().getStringExtra(MainActivity.KEY_URI_IMAGE_SECOND))
        );
        image_transparent.bringToFront();

        SeekBar seekBar = findViewById(R.id.overlay_transparent_seek_bar);

        TransparentHelper.makeTargetTransparent(seekBar, image_transparent);

        seekBar.setProgress(seekBar.getMax() / 2);
    }
}