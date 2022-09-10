package com.vincentengelsoftware.androidimagecompare;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.CacheClearer;
import com.vincentengelsoftware.androidimagecompare.helper.ImageUpdater;
import com.vincentengelsoftware.androidimagecompare.helper.KeyValueStorage;
import com.vincentengelsoftware.androidimagecompare.helper.MainHelper;
import com.vincentengelsoftware.androidimagecompare.helper.AskForReview;
import com.vincentengelsoftware.androidimagecompare.util.ImageHolder;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Status.isFirstStart) {
            Status.isFirstStart = false;
            CacheClearer.clear(getApplicationContext());
        }

        setUpActions();

        if (AskForReview.shouldAskForReview(getApplicationContext()))
        {
            askForReview();
            KeyValueStorage.setBoolean(getApplicationContext(), KeyValueStorage.ASKED_FOR_REVIEW, true);
        }

        if (Images.image_holder_first.uri != null) {
            ImageUpdater.updateImageViewImage(findViewById(R.id.home_image_first), Images.image_holder_first, ImageUpdater.SMALL);
            TextView imageNameLeft = findViewById(R.id.main_text_view_name_image_left);
            imageNameLeft.setText(Images.image_holder_first.getImageName());
        }

        if (Images.image_holder_second.uri != null) {
            ImageUpdater.updateImageViewImage(findViewById(R.id.home_image_second), Images.image_holder_second, ImageUpdater.SMALL);
            TextView imageNameRight = findViewById(R.id.main_text_view_name_image_right);
            imageNameRight.setText(Images.image_holder_second.getImageName());
        }

        if (Images.fileUri == null) {
            try {
                Images.fileUri = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        File.createTempFile("camera_image", null, this.getCacheDir())
                );
            } catch (Exception ignored) {
            }
        }

        Switch resizeLeftImage = findViewById(R.id.main_switch_resize_image_left);
        resizeLeftImage.setChecked(Status.resize_image_left);
        resizeLeftImage.setOnCheckedChangeListener((compoundButton, b) -> Status.resize_image_left = b);

        Switch resizeRightImage = findViewById(R.id.main_switch_resize_image_right);
        resizeRightImage.setChecked(Status.resize_image_right);
        resizeRightImage.setOnCheckedChangeListener((compoundButton, b) -> Status.resize_image_right = b);

        try {
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();

            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if (type.startsWith("image/")) {
                    handleSendImage(intent);
                }
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
                if (type.startsWith("image/")) {
                    handleSendMultipleImages(intent);
                }
            }
        } catch (Exception ignored) {
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);

            String imageName = MainHelper.getImageName(this, imageUri);

            if (Images.image_holder_first.bitmap == null) {
                Images.image_holder_first.updateFromUri(
                        imageUri,
                        this.getContentResolver(),
                        size,
                        getResources().getDisplayMetrics(),
                        imageName
                );
                ImageView first = findViewById(R.id.home_image_first);
                first.setImageBitmap(Images.image_holder_first.getBitmapSmall());
                TextView imageNameLeft = findViewById(R.id.main_text_view_name_image_left);
                imageNameLeft.setText(Images.image_holder_first.getImageName());
                return;
            }

            Images.image_holder_second.updateFromUri(
                    imageUri,
                    this.getContentResolver(),
                    size,
                    getResources().getDisplayMetrics(),
                    imageName
            );
            ImageView second = findViewById(R.id.home_image_second);
            second.setImageBitmap(Images.image_holder_second.getBitmapSmall());
            TextView imageNameRight = findViewById(R.id.main_text_view_name_image_right);
            imageNameRight.setText(Images.image_holder_second.getImageName());
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            ImageView first = findViewById(R.id.home_image_first);
            ImageView second = findViewById(R.id.home_image_second);
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);

            String imageNameFirst = MainHelper.getImageName(this, imageUris.get(0));
            String imageNameSecond = MainHelper.getImageName(this, imageUris.get(1));

            Images.image_holder_first.updateFromUri(
                    imageUris.get(0),
                    this.getContentResolver(),
                    size,
                    getResources().getDisplayMetrics(),
                    imageNameFirst
            );
            Images.image_holder_second.updateFromUri(
                    imageUris.get(1),
                    this.getContentResolver(),
                    size,
                    getResources().getDisplayMetrics(),
                    imageNameSecond
            );
            first.setImageBitmap(Images.image_holder_first.getBitmapSmall());
            second.setImageBitmap(Images.image_holder_second.getBitmapSmall());

            TextView imageNameLeft = findViewById(R.id.main_text_view_name_image_left);
            imageNameLeft.setText(Images.image_holder_first.getImageName());
            TextView imageNameRight = findViewById(R.id.main_text_view_name_image_right);
            imageNameRight.setText(Images.image_holder_second.getImageName());

            if (imageUris.size() > 2) {
                Toast.makeText(getBaseContext(), "You can only compare two images at once", Toast.LENGTH_LONG).show();
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

    private void setUpActions()
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
            if (Status.activityIsOpening) {
                return;
            }

            Status.activityIsOpening = true;
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
                findViewById(R.id.home_image_second),
                findViewById(R.id.main_text_view_name_image_left),
                findViewById(R.id.main_text_view_name_image_right),
                findViewById(R.id.main_switch_resize_image_left),
                findViewById(R.id.main_switch_resize_image_right)
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

        ImageView first = findViewById(R.id.home_image_first);
        ImageView second = findViewById(R.id.home_image_second);

        addLoadImageLogic(
                first,
                Images.image_holder_first,
                findViewById(R.id.main_text_view_name_image_left)
        );

        addLoadImageLogic(
                second,
                Images.image_holder_second,
                findViewById(R.id.main_text_view_name_image_right)
        );
    }

    private void addButtonChangeActivityLogic(Button btn, Class<?> targetActivity)
    {
        btn.setOnClickListener(view -> {
            if (
                    Images.image_holder_first.bitmap == null ||
                            Images.image_holder_second.bitmap == null ||
                            Status.activityIsOpening
            ) {
                return;
            }

            Status.activityIsOpening = true;

            Intent intent = new Intent(getApplicationContext(), targetActivity);

            Thread t = new Thread(() -> {
                runOnUiThread(() -> {
                    ProgressBar spinner = findViewById(R.id.pbProgess);
                    spinner.setVisibility(View.VISIBLE);
                });

                Images.image_holder_first.calculateRotatedBitmap();
                Images.image_holder_second.calculateRotatedBitmap();

                runOnUiThread(() -> {
                    ProgressBar spinner = findViewById(R.id.pbProgess);
                    spinner.setVisibility(View.GONE);
                });

                startActivity(intent);
            });

            t.start();
        });
    }

    private void addLoadImageLogic(ImageView imageView, ImageHolder imageHolder, TextView imageNameText) {
        ActivityResultLauncher<String> mGetContentGallery = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        return;
                    }

                    String imageName = MainHelper.getImageName(this, uri);

                    Point size = new Point();
                    getWindowManager().getDefaultDisplay().getSize(size);
                    imageHolder.updateFromUri(
                            uri,
                            this.getContentResolver(),
                            size,
                            getResources().getDisplayMetrics(),
                            imageName
                    );
                    imageView.setImageBitmap(imageHolder.getBitmapSmall());
                    imageNameText.setText(imageHolder.getImageName());
                });

        try {
            ActivityResultLauncher<Uri> mGetContentCamera = registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    result -> {
                        if (!result) {
                            Toast.makeText(getBaseContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String imageName = MainHelper.getImageName(this, Images.fileUri);

                        Point size = new Point();
                        getWindowManager().getDefaultDisplay().getSize(size);
                        imageHolder.updateFromUri(Images.fileUri, this.getContentResolver(), size, getResources().getDisplayMetrics(), imageName);
                        ImageUpdater.updateImageViewImage(imageView, imageHolder, ImageUpdater.SMALL);

                        imageNameText.setText(imageHolder.getImageName());
                    }
            );

            imageView.setOnClickListener(view -> {
                if (Status.activityIsOpening) {
                    return;
                }
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

    private void askForReview()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setCancelable(false);

        builder.setMessage("If you like this App please support it by leaving a review in the Google PlayStore!");

        builder.setNegativeButton("Open PlayStore", (dialogInterface, i) -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" +getPackageName())));
            } catch (ActivityNotFoundException e1) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" +getPackageName())));
                } catch (ActivityNotFoundException ignored) {
                }
            }
        });
        builder.setPositiveButton("Don't show up again", (dialogInterface, i) -> {});

        builder.create().show();
    }
}