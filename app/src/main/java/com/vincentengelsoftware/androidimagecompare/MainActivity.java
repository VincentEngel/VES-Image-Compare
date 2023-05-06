package com.vincentengelsoftware.androidimagecompare;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.window.layout.WindowMetrics;
import androidx.window.layout.WindowMetricsCalculator;

import com.vincentengelsoftware.androidimagecompare.Activities.CompareModes.CompareModeNames;
import com.vincentengelsoftware.androidimagecompare.Activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.globals.Dimensions;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.AskForReview;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapExtractor;
import com.vincentengelsoftware.androidimagecompare.helper.MainHelper;
import com.vincentengelsoftware.androidimagecompare.helper.Theme;
import com.vincentengelsoftware.androidimagecompare.helper.UriExtractor;
import com.vincentengelsoftware.androidimagecompare.services.KeyValueStorage;
import com.vincentengelsoftware.androidimagecompare.services.UserSettings;
import com.vincentengelsoftware.androidimagecompare.util.ImageHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static String leftImageUri;
    public static String rightImageUri;
    public static final String leftImageUriKey = "leftImageUriKey";
    public static final String rightImageUriKey = "rightImageUriKey";

    private KeyValueStorage keyValueStorage;

    private UserSettings userSettings;

    /**
     * TODO clear / refactor onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.keyValueStorage = new KeyValueStorage(getApplicationContext());
        this.userSettings = new UserSettings(this.keyValueStorage);

        Theme.updateTheme(this.userSettings.getTheme());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            try {
                this.keyValueStorage.setString(MainActivity.leftImageUriKey, null);
                this.keyValueStorage.setString(MainActivity.rightImageUriKey, null);
            } catch (Exception ignored) {
            }
        }

        Status.HAS_HARDWARE_KEY = ViewConfiguration.get(this).hasPermanentMenuKey();

        if (Images.fileUriFirst == null) {
            try {
                Images.fileUriFirst = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        new File(this.getCacheDir(), "camera_image_one.png")
                );
            } catch (Exception ignored) {
            }
        }
        if (Images.fileUriSecond == null) {
            try {
                Images.fileUriSecond = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        new File(this.getCacheDir(), "camera_image_two.png")
                );
            } catch (Exception ignored) {
            }
        }

        if (Dimensions.maxSide == 0) {
            WindowMetrics windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this);
            Dimensions.maxSide = Math.max(windowMetrics.getBounds().height(), windowMetrics.getBounds().width());
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
            restoreImages();
        }
        restoreImageViews();

        setUpActions();

        if (Status.handleIntentOnCreate) {
            Status.handleIntentOnCreate = false;
            this.handleIntent(getIntent());
        }

        if (AskForReview.isItTimeToAsk(getApplicationContext(), this.keyValueStorage))
        {
            askForReview();
            this.keyValueStorage.setBoolean(KeyValueStorage.ASKED_FOR_REVIEW, true);
        }
    }

    @Override
    protected void onStop() {
        if (MainActivity.leftImageUri != null) {
            this.keyValueStorage.setString(MainActivity.leftImageUriKey, MainActivity.leftImageUri);
        }
        if (MainActivity.rightImageUri != null) {
            this.keyValueStorage.setString(MainActivity.rightImageUriKey, MainActivity.rightImageUri);
        }
        super.onStop();
    }

    public void restoreImages()
    {
        if (Images.first.getBitmap() == null) {
            try {
                Uri uri = Uri.parse(this.keyValueStorage.getString(MainActivity.leftImageUriKey, null));
                Images.first.updateFromBitmap(
                        BitmapExtractor.fromUri(this.getContentResolver(), uri),
                        Dimensions.maxSide,
                        Dimensions.maxSideForPreview,
                        MainHelper.getImageName(this, uri)
                );
                MainActivity.leftImageUri = uri.toString();
            } catch (Exception ignored) {
            }
        }
        if (Images.second.getBitmap() == null) {
            try {
                Uri uri = Uri.parse(this.keyValueStorage.getString(MainActivity.rightImageUriKey, null));
                Images.second.updateFromBitmap(
                        BitmapExtractor.fromUri(this.getContentResolver(), uri),
                        Dimensions.maxSide,
                        Dimensions.maxSideForPreview,
                        MainHelper.getImageName(this, uri)
                );
                MainActivity.rightImageUri = uri.toString();
            } catch (Exception ignored) {
            }
        }
    }

    public void restoreImageViews()
    {
        if (Images.first.getBitmap() != null) {
            ImageView imageView = findViewById(R.id.home_image_first);
            Images.first.updateImageViewPreviewImage(imageView);
            TextView imageNameLeft = findViewById(R.id.main_text_view_name_image_left);
            imageNameLeft.setText(Images.first.getImageName());
        }

        if (Images.second.getBitmap() != null) {
            ImageView imageView = findViewById(R.id.home_image_second);
            Images.second.updateImageViewPreviewImage(imageView);
            TextView imageNameRight = findViewById(R.id.main_text_view_name_image_right);
            imageNameRight.setText(Images.second.getImageName());
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
        Uri imageUri = UriExtractor.getOutOfParcelableExtra(intent);

        if (imageUri != null) {
            ImageHolder imageHolder;
            ImageView imageView;
            TextView imageName;

            if (Images.first.getBitmap() == null) {
                imageHolder = Images.first;
                imageView = findViewById(R.id.home_image_first);
                imageName = findViewById(R.id.main_text_view_name_image_left);
                MainActivity.leftImageUri = imageUri.toString();
            } else {
                imageHolder = Images.second;
                imageView = findViewById(R.id.home_image_second);
                imageName = findViewById(R.id.main_text_view_name_image_right);
                MainActivity.rightImageUri = imageUri.toString();
            }

            MainHelper.updateImageFromIntent(
                    imageHolder,
                    BitmapExtractor.fromUri(this.getContentResolver(), imageUri),
                    Dimensions.maxSide,
                    Dimensions.maxSideForPreview,
                    MainHelper.getImageName(this, imageUri),
                    imageView,
                    imageName
            );
        }
    }

    /**
     * TODO: Combine with handleSendImage by getting the urls and then passing them to a single handler method
     */
    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = UriExtractor.getOutOfParcelableArrayListExtra(intent);

        if (imageUris != null) {
            if (imageUris.get(0) != null) {
                MainHelper.updateImageFromIntent(
                        Images.first,
                        BitmapExtractor.fromUri(this.getContentResolver(), imageUris.get(0)),
                        Dimensions.maxSide,
                        Dimensions.maxSideForPreview,
                        MainHelper.getImageName(this, imageUris.get(0)),
                        findViewById(R.id.home_image_first),
                        findViewById(R.id.main_text_view_name_image_left)
                );
                MainActivity.leftImageUri = imageUris.get(0).toString();
            }

            if (imageUris.get(1) != null) {
                MainHelper.updateImageFromIntent(
                        Images.second,
                        BitmapExtractor.fromUri(this.getContentResolver(), imageUris.get(1)),
                        Dimensions.maxSide,
                        Dimensions.maxSideForPreview,
                        MainHelper.getImageName(this, imageUris.get(1)),
                        findViewById(R.id.home_image_second),
                        findViewById(R.id.main_text_view_name_image_right)
                );
                MainActivity.rightImageUri = imageUris.get(1).toString();
            }

            if (imageUris.size() > 2) {
                Toast.makeText(getApplicationContext(), "You can only compare two images at once", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setUpActions()
    {
        SwitchCompat resizeLeftImage = findViewById(R.id.main_switch_resize_image_left);
        resizeLeftImage.setChecked(this.userSettings.isResizeLeftImage());
        resizeLeftImage.setOnCheckedChangeListener((compoundButton, b) -> {
            this.userSettings.setResizeLeftImage(b);
            Images.first.setResizeImageToScreen(this.userSettings.isResizeLeftImage());
        });
        resizeLeftImage.setOnLongClickListener(view -> {
            Toast.makeText(getApplicationContext(), "Resize left image to screen size", Toast.LENGTH_SHORT).show();
            return true;
        });

        SwitchCompat resizeRightImage = findViewById(R.id.main_switch_resize_image_right);
        resizeRightImage.setChecked(this.userSettings.isResizeRightImage());
        resizeRightImage.setOnCheckedChangeListener((compoundButton, b) -> {
            this.userSettings.setResizeRightImage(b);
            Images.second.setResizeImageToScreen(this.userSettings.isResizeRightImage());
        });
        resizeRightImage.setOnLongClickListener(view -> {
            Toast.makeText(getApplicationContext(), "Resize right image to screen size", Toast.LENGTH_SHORT).show();
            return true;
        });

        Button lastCompareMode = findViewById(R.id.main_button_last_compare);
        lastCompareMode.setText(
                CompareModeNames.getUserCompareModeNameFromInternalName(this.userSettings.getLastCompareMode())
        );
        lastCompareMode.setOnClickListener(view -> {
            switch (CompareModeNames.getInternalCompareModeNameFromUserCompareModeName(lastCompareMode.getText().toString())) {
                case CompareModeNames.SIDE_BY_SIDE -> openCompareActivity(SideBySideActivity.class);
                case CompareModeNames.OVERLAY_SLIDE -> openCompareActivity(OverlaySlideActivity.class);
                case CompareModeNames.OVERLAY_TAP -> openCompareActivity(OverlayTapActivity.class);
                case CompareModeNames.OVERLAY_TRANSPARENT -> openCompareActivity(OverlayTransparentActivity.class);
                case CompareModeNames.META_DATA -> openCompareActivity(MetaDataActivity.class);
                default -> openCompareDialog();
            }
        });

        findViewById(R.id.main_button_compare).setOnClickListener(view -> openCompareDialog());

        findViewById(R.id.home_button_info).setOnClickListener(view -> {
            if (Status.activityIsOpening) {
                return;
            }

            Status.activityIsOpening = true;
            Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
            startActivity(intent);
        });

        ImageButton swapImages = findViewById(R.id.home_button_swap_images);
        MainHelper.addSwapImageLogic(
                swapImages,
                Images.first,
                Images.second,
                findViewById(R.id.home_image_first),
                findViewById(R.id.home_image_second),
                findViewById(R.id.main_text_view_name_image_left),
                findViewById(R.id.main_text_view_name_image_right),
                findViewById(R.id.main_switch_resize_image_left),
                findViewById(R.id.main_switch_resize_image_right)
        );
        swapImages.setOnLongClickListener(view -> {
            Toast.makeText(getApplicationContext(), "Swap Images", Toast.LENGTH_SHORT).show();
            return true;
        });

        ImageButton extensions = findViewById(R.id.home_button_extensions);
        if (this.userSettings.isShowExtensions()) {
            extensions.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_extension_on));
        } else {
            extensions.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_extension_off));
        }
        extensions.setOnClickListener(view -> {
            this.userSettings.setShowExtensions(!this.userSettings.isShowExtensions());
            if (this.userSettings.isShowExtensions()) {
                extensions.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_extension_on));
            } else {
                extensions.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_extension_off));
            }
        });
        extensions.setOnLongClickListener(view -> {
            Toast.makeText(getApplicationContext(), "Show details in Tap and Side-by-Side", Toast.LENGTH_SHORT).show();
            return true;
        });

        ImageButton linkedZoom = findViewById(R.id.home_button_link_zoom);
        if (this.userSettings.isSyncedZoom()) {
            linkedZoom.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link));
        } else {
            linkedZoom.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off));
        }
        linkedZoom.setOnClickListener(view -> {
            this.userSettings.setSyncedZoom(!this.userSettings.isSyncedZoom());
            if (this.userSettings.isSyncedZoom()) {
                linkedZoom.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link));
            } else {
                linkedZoom.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off));
            }
        });
        linkedZoom.setOnLongClickListener(view -> {
            Toast.makeText(getApplicationContext(), "Sync zoom between images in comparison modes", Toast.LENGTH_SHORT).show();
            return true;
        });

        ImageButton rotateImageLeft = findViewById(R.id.home_button_rotate_image_left);
        MainHelper.addRotateImageLogic(
                rotateImageLeft,
                Images.first,
                findViewById(R.id.home_image_first)
        );
        rotateImageLeft.setOnLongClickListener(view -> {
            Toast.makeText(getApplicationContext(), "Rotate the left image", Toast.LENGTH_SHORT).show();
            return true;
        });

        ImageButton rotateImageRight = findViewById(R.id.home_button_rotate_image_right);
        MainHelper.addRotateImageLogic(
                rotateImageRight,
                Images.second,
                findViewById(R.id.home_image_second)
        );
        rotateImageRight.setOnLongClickListener(view -> {
            Toast.makeText(getApplicationContext(), "Rotate the right image", Toast.LENGTH_SHORT).show();
            return true;
        });

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
        Theme.updateButtonText(button, this.userSettings.getTheme());

        button.setOnClickListener(view -> {
            this.userSettings.setTheme((this.userSettings.getTheme() + 1) % 3);
            Theme.updateButtonText(button, this.userSettings.getTheme());
            Theme.updateTheme(this.userSettings.getTheme());
        });
    }

    private void openCompareDialog()
    {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.compare_mode_selection_dialog);

        addCompareDialogButtonOnClickLogic(
                R.id.select_compare_mode_dialog_btn_side_by_side,
                SideBySideActivity.class,
                dialog
        );
        addCompareDialogButtonOnClickLogic(
                R.id.select_compare_mode_dialog_btn_overlay_slide,
                OverlaySlideActivity.class,
                dialog
        );
        addCompareDialogButtonOnClickLogic(
                R.id.select_compare_mode_dialog_btn_transparent,
                OverlayTransparentActivity.class,
                dialog
        );
        addCompareDialogButtonOnClickLogic(
                R.id.select_compare_mode_dialog_btn_overlay_tap,
                OverlayTapActivity.class,
                dialog
        );
        addCompareDialogButtonOnClickLogic(
                R.id.select_compare_mode_dialog_btn_metadata,
                MetaDataActivity.class,
                dialog
        );

        dialog.show();
    }

    private void addCompareDialogButtonOnClickLogic(
            @IdRes int id,
            Class<?> targetActivity,
            Dialog dialog
    ) {
        Button button = dialog.findViewById(id);
        button.setOnClickListener(view -> {
            openCompareActivity(targetActivity);
            dialog.dismiss();
        });
    }

    private void openCompareActivity(Class<?> targetActivity)
    {
        if (
                Images.first.getBitmap() == null
                        || Images.second.getBitmap() == null
                        || Status.activityIsOpening
        ) {
            Toast.makeText(getApplicationContext(), "Select two pictures first", Toast.LENGTH_SHORT).show();
            return;
        }

        String activityName = targetActivity.toString().substring(targetActivity.toString().lastIndexOf(".")+1);
        String internalCompareModeName = CompareModeNames.getInternalCompareModeNameFromActivityName(activityName);
        this.userSettings.setLastCompareMode(internalCompareModeName);
        Button button = findViewById(R.id.main_button_last_compare);
        button.setText(CompareModeNames.getUserCompareModeNameFromInternalName(internalCompareModeName));

        Status.activityIsOpening = true;

        Intent intent = new Intent(getApplicationContext(), targetActivity);
        intent.putExtra(IntentExtras.SHOW_EXTENSIONS, this.userSettings.isShowExtensions());
        intent.putExtra(IntentExtras.SYNCED_ZOOM, this.userSettings.isSyncedZoom());
        intent.putExtra(IntentExtras.HAS_HARDWARE_KEY, Status.HAS_HARDWARE_KEY);

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
                                MainActivity.leftImageUri = uri.toString();
                                imageHolder = Images.first;
                            } else {
                                imageHolder = Images.second;
                                MainActivity.rightImageUri = uri.toString();
                            }

                            imageHolder.updateFromBitmap(
                                    BitmapExtractor.fromUri(this.getContentResolver(), uri),
                                    Dimensions.maxSide,
                                    Dimensions.maxSideForPreview,
                                    MainHelper.getImageName(this, uri)
                            );
                            ImageView imageView = findViewById(imageViewId);
                            imageHolder.updateImageViewPreviewImage(imageView);
                            TextView imageNameText = findViewById(imageNameTextId);
                            imageNameText.setText(imageHolder.getImageName());
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
                            Status.isTakingPicture = false;
                            return;
                        }

                        try {
                            ImageHolder imageHolder;
                            Uri uri;
                            if (Objects.equals(Images.fileUriFirst.getPath(), UriPath)) {
                                uri = Images.fileUriFirst;
                                imageHolder = Images.first;
                                MainActivity.leftImageUri = uri.toString();
                            } else {
                                uri = Images.fileUriSecond;
                                imageHolder = Images.second;
                                MainActivity.rightImageUri = uri.toString();
                            }

                            imageHolder.updateFromBitmap(
                                    BitmapExtractor.fromUri(this.getContentResolver(), uri),
                                    Dimensions.maxSide,
                                    Dimensions.maxSideForPreview,
                                    MainHelper.getImageName(this, uri)
                            );
                        } catch (Exception ignored) {
                        }

                        runOnUiThread(() -> {
                            restoreImageViews();
                            Status.isTakingPicture = false;
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
                            Status.isTakingPicture = true;
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

        builder.setMessage("If you like this App, please support it by leaving a review on the Google PlayStore!");

        builder.setPositiveButton("Open PlayStore", (dialogInterface, i) -> {
            if (isPlayStoreInstalled()) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        });

        builder.setNegativeButton("Don't show up again", (dialogInterface, i) -> {});

        builder.show();
    }

    private boolean isPlayStoreInstalled() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getPackageManager().getPackageInfo("com.android.vending", PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA));
            } else {
                getPackageManager().getPackageInfo("com.android.vending", PackageManager.GET_META_DATA);
            }

            return true;
        } catch (Exception ignored) {
        }

        return false;
    }
}