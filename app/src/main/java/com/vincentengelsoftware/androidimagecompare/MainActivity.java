package com.vincentengelsoftware.androidimagecompare;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.helper.ImageUpdater;
import com.vincentengelsoftware.androidimagecompare.helper.MainHelper;
import com.vincentengelsoftware.androidimagecompare.util.ImageHolder;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpActivityButtons();

        ImageView first = findViewById(R.id.home_image_first);
        ImageView second = findViewById(R.id.home_image_second);

        addLoadImageLogic(
                first,
                Images.image_holder_first
        );

        addLoadImageLogic(
                second,
                Images.image_holder_second
        );

        if (Images.image_holder_first.uri != null) {
            ImageUpdater.updateImage(first, Images.image_holder_first, ImageUpdater.SMALL);
        }

        if (Images.image_holder_second.uri != null) {
            ImageUpdater.updateImage(second, Images.image_holder_second, ImageUpdater.SMALL);
        }

        if (Images.fileUri == null) {
            try {
                Images.fileUri = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        File.createTempFile("camera_image", null, this.getCacheDir())
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            this.finishAndRemoveTask();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }

    private void setUpActivityButtons()
    {
        addButtonChangeActivityLogic(
                findViewById(R.id.button_side_by_side),
                SideBySideActivity.class
        );

        addButtonChangeActivityLogic(
                findViewById(R.id.button_overlay_tap),
                OverlayTapActivity.class
        );

        addButtonChangeActivityLogic(
                findViewById(R.id.button_overlay_slide),
                OverlaySlideActivity.class
        );

        addButtonChangeActivityLogic(
                findViewById(R.id.button_overlay_transparent),
                OverlayTransparentActivity.class
        );

        findViewById(R.id.home_button_info).setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
            startActivity(intent);
        });

        MainHelper.passClickToUnderlyingView(
                findViewById(R.id.frame_layout_image_right),
                findViewById(R.id.home_button_swap_images)
        );

        MainHelper.addSwapImageLogic(
                findViewById(R.id.home_button_swap_images),
                Images.image_holder_first,
                Images.image_holder_second,
                findViewById(R.id.home_image_first),
                findViewById(R.id.home_image_second)
        );

        MainHelper.addRotateImageLogic(
                findViewById(R.id.home_button_rotate_image_left),
                Images.image_holder_first,
                findViewById(R.id.home_image_first)
        );

        MainHelper.addRotateImageLogic(
                findViewById(R.id.home_button_rotate_image_right),
                Images.image_holder_second,
                findViewById(R.id.home_image_second)
        );
    }

    private void addButtonChangeActivityLogic(Button btn, Class<?> targetActivity)
    {
        btn.setOnClickListener(view -> {
            if (Images.image_holder_first.bitmap == null || Images.image_holder_second.bitmap == null) {
                return;
            }

            Intent intent = new Intent(getApplicationContext(), targetActivity);

            Thread t = new Thread(() -> {
                runOnUiThread(() -> {
                    ProgressBar progressBar = findViewById(R.id.pbProgess);
                    progressBar.setVisibility(View.VISIBLE);
                });

                Images.image_holder_first.calculateRotatedBitmap();
                Images.image_holder_second.calculateRotatedBitmap();

                runOnUiThread(() -> {
                    ProgressBar progressBar = findViewById(R.id.pbProgess);
                    progressBar.setVisibility(View.GONE);
                });

                startActivity(intent);
            });

            t.start();
        });
    }

    private void addLoadImageLogic(ImageView imageView, ImageHolder imageHolder) {
        ActivityResultLauncher<String> mGetContentGallery = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        return;
                    }

                    Point size = new Point();
                    getWindowManager().getDefaultDisplay().getSize(size);
                    imageHolder.updateFromUri(
                            uri,
                            this.getContentResolver(),
                            size,
                            getResources().getDisplayMetrics()
                    );
                    imageView.setImageBitmap(imageHolder.getBitmapSmall());
                });

        try {
            ActivityResultLauncher<Uri> mGetContentCamera = registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    result -> {
                        if (!result) {
                            Toast.makeText(getBaseContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Point size = new Point();
                        getWindowManager().getDefaultDisplay().getSize(size);
                        imageHolder.updateFromUri(Images.fileUri, this.getContentResolver(), size, getResources().getDisplayMetrics());
                        ImageUpdater.updateImage(imageView, imageHolder, ImageUpdater.SMALL);
                    }
            );

            imageView.setOnClickListener(view -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                final CharSequence[] optionsMenu = {"Take Photo", "Choose from Gallery"};


                builder.setItems(optionsMenu, (dialogInterface, i) -> {
                    if (optionsMenu[i].equals("Take Photo")) {
                        if (MainHelper.checkPermission(MainActivity.this)) {
                            mGetContentCamera.launch(Images.fileUri);
                        } else {
                            MainHelper.requestPermission(MainActivity.this);
                            imageView.callOnClick();
                        }
                    } else if (optionsMenu[i].equals("Choose from Gallery")) {
                        mGetContentGallery.launch("image/*");
                    }

                    dialogInterface.dismiss();
                });

                builder.create().show();
            });
        } catch (Exception ignored) {
        }
    }
}