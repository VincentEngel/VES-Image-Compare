package com.vincentengelsoftware.androidimagecompare;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.vincentengelsoftware.androidimagecompare.helper.SlideHelper;

public class OverlaySlideActivity extends AppCompatActivity {
    private Bitmap bitmapSource;

    // Just swap images???
    private boolean leftToRight = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_overlay_slide);

        ImageView image_base = findViewById(R.id.overlay_slide_image_view_base);
        image_base.setImageURI(
                Uri.parse(getIntent().getStringExtra(MainActivity.KEY_URI_IMAGE_FIRST))
        );
        ImageView image_front = findViewById(R.id.overlay_slide_image_view_front);
        image_front.setImageURI(
                Uri.parse(getIntent().getStringExtra(MainActivity.KEY_URI_IMAGE_SECOND))
        );

        try {
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            bitmapSource = SlideHelper.resizeBitmap(
                    MediaStore.Images.Media.getBitmap(
                            this.getContentResolver(),
                            Uri.parse(getIntent().getStringExtra(MainActivity.KEY_URI_IMAGE_SECOND))
                    ),
                    size.x,
                    size.y
            );
        } catch (Exception $e) {
            finish();
        }

        SeekBar seekBar = findViewById(R.id.overlay_slide_seek_bar);
        addSeekbarLogic(seekBar, image_front);
        seekBar.setProgress(50);

        findViewById(R.id.overlay_slide_button_swap_seekbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leftToRight = !leftToRight;
                int progress = 50;
                // onProgressChanged is not triggered if setProgress is called with current progress
                if (seekBar.getProgress() == progress) {
                    progress = 51;
                }
                seekBar.setProgress(progress);
            }
        });
    }

    // TODO move to helper class
    private void addSeekbarLogic(SeekBar seekBar, ImageView image_front)
    {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int width = bitmapSource.getWidth() * i / 100;

                if (width == 0) {
                    width = 1;
                }

                Bitmap bitmapCopy = bitmapSource.copy(bitmapSource.getConfig(), true);

                int[] pixels = new int[bitmapSource.getHeight()*bitmapSource.getWidth()];
                bitmapSource.getPixels(
                        pixels,
                        0,
                        bitmapSource.getWidth(),
                        0,
                        0,
                        bitmapSource.getWidth(),
                        bitmapSource.getHeight()
                );

                if (leftToRight) {
                    for (int x = width; x < bitmapSource.getWidth(); x++) {
                        for (int y = 0; y < bitmapSource.getHeight(); y++) {
                            pixels[x + (y * bitmapSource.getWidth())] = Color.alpha(Color.TRANSPARENT);
                        }
                    }
                } else {
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < bitmapSource.getHeight(); y++) {
                            pixels[x + (y * bitmapSource.getWidth())] = Color.alpha(Color.TRANSPARENT);
                        }
                    }
                }

                bitmapCopy.setPixels(
                        pixels,
                        0,
                        bitmapSource.getWidth(),
                        0,
                        0,
                        bitmapSource.getWidth(),
                        bitmapSource.getHeight()
                );

                image_front.setImageBitmap(bitmapCopy);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
}