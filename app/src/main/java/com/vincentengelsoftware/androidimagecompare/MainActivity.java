package com.vincentengelsoftware.androidimagecompare;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.FileProvider;

import com.vincentengelsoftware.androidimagecompare.globals.Dimensions;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.KeyValueStorage;
import com.vincentengelsoftware.androidimagecompare.helper.MainHelper;
import com.vincentengelsoftware.androidimagecompare.helper.ShouldAskForReview;
import com.vincentengelsoftware.androidimagecompare.helper.Theme;
import com.vincentengelsoftware.androidimagecompare.helper.UriHelper;
import com.vincentengelsoftware.androidimagecompare.util.ImageHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private long pressedTime;
    public static final String lastImageForFirstFromCameraKey = "lastImageForFirstFromCamera";
    public static final String lastImageForSecondFromCameraKey = "lastImageForSecondFromCamera";
    public static boolean lastImageForFirstFromCamera = false;
    public static boolean lastImageForSecondFromCamera = false;

    /**
     * TODO clear / refactor onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Status.THEME_DEFAULT_SYSTEM == -1) {
            Status.isFirstStart = false;
            Status.THEME_DEFAULT_SYSTEM = Theme.getCurrentTheme(getResources());
        }

        if (Images.fileUriFirst == null) {
            try {
                Images.fileUriFirst = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        new File(this.getCacheDir(), "camera_image_first.png")
                );
            } catch (Exception ignored) {
            }
        }
        if (Images.fileUriSecond == null) {
            try {
                Images.fileUriSecond = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        new File(this.getCacheDir(), "camera_image_second.png")
                );
            } catch (Exception ignored) {
            }
        }
        
        if (Dimensions.maxSide == 0) {
            Point point = new Point();
            getWindowManager().getDefaultDisplay().getSize(point);
            Dimensions.maxSide = Math.max(point.x, point.y);
        }
        
        if (Dimensions.maxSideForPreview == 0) {
            Dimensions.maxSideForPreview = Math.round(
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            Dimensions.MAX_SMALL_SIZE_DP,
                            getResources().getDisplayMetrics()
                    )
            );
        }
        if (savedInstanceState != null) {
            lastImageForFirstFromCamera = savedInstanceState.getBoolean(lastImageForFirstFromCameraKey, false);
            lastImageForSecondFromCamera = savedInstanceState.getBoolean(lastImageForSecondFromCameraKey, false);
        }

        if (savedInstanceState != null && lastImageForFirstFromCamera && Images.first.getBitmap() == null) {
            try {
                Images.first.updateFromBitmap(
                        UriHelper.getBitmap(this.getContentResolver(), Images.fileUriFirst),
                        Dimensions.maxSide,
                        Dimensions.maxSideForPreview,
                        MainHelper.getImageName(this, Images.fileUriFirst)
                );
            } catch (Exception ignored) {
            }
        }
        if (savedInstanceState != null && lastImageForSecondFromCamera && Images.second.getBitmap() == null) {
            try {
                Images.second.updateFromBitmap(
                        UriHelper.getBitmap(this.getContentResolver(), Images.fileUriSecond),
                        Dimensions.maxSide,
                        Dimensions.maxSideForPreview,
                        MainHelper.getImageName(this, Images.fileUriSecond)
                );
            } catch (Exception ignored) {
            }
        }

        setUpActions();

        if (ShouldAskForReview.check(getApplicationContext()))
        {
            askForReview();
            KeyValueStorage.setBoolean(getApplicationContext(), KeyValueStorage.ASKED_FOR_REVIEW, true);
        }

        if (Images.first.getBitmap() != null) {
            Images.first.updateImageViewPreviewImage(findViewById(R.id.home_image_first));
            TextView imageNameLeft = findViewById(R.id.main_text_view_name_image_left);
            imageNameLeft.setText(Images.first.getImageName());
        }

        if (Images.second.getBitmap() != null) {
            Images.second.updateImageViewPreviewImage(findViewById(R.id.home_image_second));
            TextView imageNameRight = findViewById(R.id.main_text_view_name_image_right);
            imageNameRight.setText(Images.second.getImageName());
        }

        SwitchCompat resizeLeftImage = findViewById(R.id.main_switch_resize_image_left);
        resizeLeftImage.setChecked(Images.first.isResizeImageToScreen());
        resizeLeftImage.setOnCheckedChangeListener((compoundButton, b) -> Images.first.setResizeImageToScreen(b));

        SwitchCompat resizeRightImage = findViewById(R.id.main_switch_resize_image_right);
        resizeRightImage.setChecked(Images.second.isResizeImageToScreen());
        resizeRightImage.setOnCheckedChangeListener((compoundButton, b) -> Images.second.setResizeImageToScreen(b));


        if (Status.handleIntentOnCreate) {
            Status.handleIntentOnCreate = false;
            this.handleIntent(getIntent());
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(lastImageForFirstFromCameraKey, lastImageForFirstFromCamera);
        outState.putBoolean(lastImageForSecondFromCameraKey, lastImageForSecondFromCamera);

        super.onSaveInstanceState(outState);
    }

    private void handleIntent(Intent intent)
    {
        try {
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

    /**
     * called when files are shared to this app when it is already running
     */
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        this.handleIntent(intent);
    }

    /**
     * TODO: Combine with handleSendMultipleImages by getting the urls and then passing them to a single handler method
     */
    void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

        if (imageUri != null) {
            ImageHolder imageHolder;
            ImageView imageView;
            TextView imageName;

            if (Images.first.getBitmap() == null) {
                imageHolder = Images.first;
                imageView = findViewById(R.id.home_image_first);
                imageName = findViewById(R.id.main_text_view_name_image_left);
                lastImageForFirstFromCamera = false;
            } else {
                imageHolder = Images.second;
                imageView = findViewById(R.id.home_image_second);
                imageName = findViewById(R.id.main_text_view_name_image_right);
                lastImageForSecondFromCamera = false;
            }

            MainHelper.updateImageFromIntent(
                    imageHolder,
                    UriHelper.getBitmap(this.getContentResolver(), imageUri),
                    Dimensions.maxSide,
                    Dimensions.maxSideForPreview,
                    MainHelper.getImageName(this, imageUri),
                    imageView,
                    imageName
            );
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);

        if (imageUris != null) {
            if (imageUris.get(0) != null) {
                MainHelper.updateImageFromIntent(
                        Images.first,
                        UriHelper.getBitmap(this.getContentResolver(), imageUris.get(0)),
                        Dimensions.maxSide,
                        Dimensions.maxSideForPreview,
                        MainHelper.getImageName(this, imageUris.get(0)),
                        findViewById(R.id.home_image_first),
                        findViewById(R.id.main_text_view_name_image_left)
                );
                lastImageForFirstFromCamera = false;
            }

            if (imageUris.get(1) != null) {
                MainHelper.updateImageFromIntent(
                        Images.second,
                        UriHelper.getBitmap(this.getContentResolver(), imageUris.get(1)),
                        Dimensions.maxSide,
                        Dimensions.maxSideForPreview,
                        MainHelper.getImageName(this, imageUris.get(1)),
                        findViewById(R.id.home_image_second),
                        findViewById(R.id.main_text_view_name_image_right)
                );
                lastImageForSecondFromCamera = false;
            }

            if (imageUris.size() > 2) {
                Toast.makeText(getApplicationContext(), "You can only compare two images at once", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            this.finishAndRemoveTask();
        } else {
            Toast.makeText(getApplicationContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
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

        MainHelper.addSwapImageLogic(
                findViewById(R.id.home_button_swap_images),
                Images.first,
                Images.second,
                findViewById(R.id.home_image_first),
                findViewById(R.id.home_image_second),
                findViewById(R.id.main_text_view_name_image_left),
                findViewById(R.id.main_text_view_name_image_right),
                findViewById(R.id.main_switch_resize_image_left),
                findViewById(R.id.main_switch_resize_image_right)
        );

        MainHelper.addRotateImageLogic(
                findViewById(R.id.home_button_rotate_image_left),
                Images.first,
                findViewById(R.id.home_image_first)
        );

        MainHelper.addRotateImageLogic(
                findViewById(R.id.home_button_rotate_image_right),
                Images.second,
                findViewById(R.id.home_image_second)
        );

        addLoadImageLogic(
                R.id.home_image_first,
                "first",
                R.id.main_text_view_name_image_left,
                Images.fileUriFirst.getPath()
        );

        addLoadImageLogic(
                R.id.home_image_second,
                "second",
                R.id.main_text_view_name_image_right,
                Images.fileUriSecond.getPath()
        );

        setUpThemeToggleButton(findViewById(R.id.home_theme));
    }

    private void setUpThemeToggleButton(Button button)
    {
        Theme.updateButtonText(button, Status.THEME);

        button.setOnClickListener(view -> {
            Status.THEME = (Status.THEME + 1) % 3;
            Theme.updateButtonText(button, Status.THEME);
            Theme.updateTheme(Theme.map(Status.THEME), Theme.getCurrentTheme(getResources()));
        });
    }
    private void addButtonChangeActivityLogic(Button btn, Class<?> targetActivity)
    {
        btn.setOnClickListener(view -> {
            if (
                    Images.first.getBitmap() == null
                            || Images.second.getBitmap() == null
                            || Status.activityIsOpening
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

                Images.first.calculateRotatedBitmap();
                Images.second.calculateRotatedBitmap();

                runOnUiThread(() -> {
                    ProgressBar spinner = findViewById(R.id.pbProgess);
                    spinner.setVisibility(View.GONE);
                });

                startActivity(intent);
            });

            t.start();
        });
    }

    private void addLoadImageLogic(int imageViewId , String imageHolderName, int imageNameTextId, String UriPath) {
        ActivityResultLauncher<String> mGetContentGallery = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    runOnUiThread(() -> {
                        try {
                            ImageHolder imageHolder;
                            if (Objects.equals(imageHolderName, "first")) {
                                imageHolder = Images.first;
                            } else {
                                imageHolder = Images.second;
                            }

                            imageHolder.updateFromBitmap(
                                    UriHelper.getBitmap(this.getContentResolver(), uri),
                                    Dimensions.maxSide,
                                    Dimensions.maxSideForPreview,
                                    MainHelper.getImageName(this, uri)
                            );
                            ImageView imageView = findViewById(imageViewId);
                            imageHolder.updateImageViewPreviewImage(imageView);
                            TextView imageNameText = findViewById(imageNameTextId);
                            imageNameText.setText(imageHolder.getImageName());
                            if (Objects.equals(imageHolderName, "first")) {
                                lastImageForFirstFromCamera = false;
                            } else {
                                lastImageForSecondFromCamera = false;
                            }
                        } catch (Exception ignored) {
                        }
                    });
                });

        try {
            ActivityResultLauncher<Uri> mGetContentCamera = registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    result -> {
                        if (!result) {
                            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        runOnUiThread(() -> {
                            try {
                                Uri uri;
                                if (Objects.equals(Images.fileUriFirst.getPath(), UriPath)) {
                                    uri = Images.fileUriFirst;
                                } else {
                                    uri = Images.fileUriSecond;
                                }

                                ImageHolder imageHolder;
                                if (Objects.equals(imageHolderName, "first")) {
                                    imageHolder = Images.first;
                                } else {
                                    imageHolder = Images.second;
                                }

                                imageHolder.updateFromBitmap(
                                        UriHelper.getBitmap(this.getContentResolver(), uri),
                                        Dimensions.maxSide,
                                        Dimensions.maxSideForPreview,
                                        MainHelper.getImageName(this, uri)
                                );
                                ImageView imageView = findViewById(imageViewId);
                                imageHolder.updateImageViewPreviewImage(imageView);
                                TextView imageNameText = findViewById(imageNameTextId);
                                imageNameText.setText(imageHolder.getImageName());
                                if (Objects.equals(imageHolderName, "first")) {
                                    lastImageForFirstFromCamera = true;
                                } else {
                                    lastImageForSecondFromCamera = true;
                                }
                            } catch (Exception ignored) {
                            }
                        });
                    }
            );

            ImageView imageView = findViewById(imageViewId);
            Uri uri;
            if (Objects.equals(Images.fileUriFirst.getPath(), UriPath)) {
                uri = Images.fileUriFirst;
            } else {
                uri = Images.fileUriSecond;
            }
            imageView.setOnClickListener(view -> {
                if (Status.activityIsOpening) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                final CharSequence[] optionsMenu = {"Take Photo", "Choose from Gallery"};


                builder.setItems(optionsMenu, (dialogInterface, i) -> {
                    if (optionsMenu[i].equals("Take Photo")) {
                        if (MainHelper.checkPermission(MainActivity.this)) {
                            mGetContentCamera.launch(uri);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alertDialog);

        builder.setCancelable(false);

        builder.setMessage("If you like this App please support it by leaving a review in the Google PlayStore!");

        builder.setPositiveButton("Open PlayStore", (dialogInterface, i) -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" +getPackageName())));
            } catch (ActivityNotFoundException e1) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" +getPackageName())));
                } catch (ActivityNotFoundException ignored) {
                }
            }
        });
        builder.setNegativeButton("Don't show up again", (dialogInterface, i) -> {});

        builder.create().show();
    }
}