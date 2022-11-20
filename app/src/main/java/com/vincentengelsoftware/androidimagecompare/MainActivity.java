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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.FileProvider;

import com.vincentengelsoftware.androidimagecompare.globals.Dimensions;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.ShouldAskForReview;
import com.vincentengelsoftware.androidimagecompare.helper.CacheClearer;
import com.vincentengelsoftware.androidimagecompare.helper.KeyValueStorage;
import com.vincentengelsoftware.androidimagecompare.helper.MainHelper;
import com.vincentengelsoftware.androidimagecompare.helper.Theme;
import com.vincentengelsoftware.androidimagecompare.helper.UriHelper;
import com.vincentengelsoftware.androidimagecompare.util.ImageHolder;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private long pressedTime;

    /**
     * TODO clear / refactor onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Status.isFirstStart) {
            Status.isFirstStart = false;
            CacheClearer.clear(getApplicationContext());
            Status.THEME_DEFAULT_SYSTEM = Theme.getCurrentTheme(getResources());
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

        if (Images.fileUri == null) {
            try {
                Images.fileUri = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        File.createTempFile("camera_image_", null, this.getCacheDir())
                );
            } catch (Exception ignored) {
            }
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
            } else {
                imageHolder = Images.second;
                imageView = findViewById(R.id.home_image_second);
                imageName = findViewById(R.id.main_text_view_name_image_right);
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
            }

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

        ImageView first = findViewById(R.id.home_image_first);
        ImageView second = findViewById(R.id.home_image_second);

        addLoadImageLogic(
                first,
                Images.first,
                findViewById(R.id.main_text_view_name_image_left)
        );

        addLoadImageLogic(
                second,
                Images.second,
                findViewById(R.id.main_text_view_name_image_right)
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

    private void addLoadImageLogic(ImageView imageView, ImageHolder imageHolder, TextView imageNameText) {
        ActivityResultLauncher<String> mGetContentGallery = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        return;
                    }

                    imageHolder.updateFromBitmap(
                            UriHelper.getBitmap(this.getContentResolver(), uri),
                            Dimensions.maxSide,
                            Dimensions.maxSideForPreview,
                            MainHelper.getImageName(this, uri)
                    );
                    imageHolder.updateImageViewPreviewImage(imageView);
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

                        try {
                            imageHolder.updateFromBitmap(
                                    UriHelper.getBitmap(this.getContentResolver(), Images.fileUri),
                                    Dimensions.maxSide,
                                    Dimensions.maxSideForPreview,
                                    MainHelper.getImageName(this, Images.fileUri)
                            );
                            imageHolder.updateImageViewPreviewImage(imageView);

                            imageNameText.setText(imageHolder.getImageName());
                        } catch (Exception ignored) {
                            Toast.makeText(getBaseContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
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