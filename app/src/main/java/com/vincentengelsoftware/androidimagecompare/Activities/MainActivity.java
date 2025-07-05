package com.vincentengelsoftware.androidimagecompare.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.window.layout.WindowMetrics;
import androidx.window.layout.WindowMetricsCalculator;

import com.vincentengelsoftware.androidimagecompare.Activities.CompareModes.CompareModeNames;
import com.vincentengelsoftware.androidimagecompare.Activities.CompareModes.OverlayCutActivity;
import com.vincentengelsoftware.androidimagecompare.Activities.CompareModes.MetaDataActivity;
import com.vincentengelsoftware.androidimagecompare.Activities.CompareModes.OverlaySlideActivity;
import com.vincentengelsoftware.androidimagecompare.Activities.CompareModes.OverlayTapActivity;
import com.vincentengelsoftware.androidimagecompare.Activities.CompareModes.OverlayTransparentActivity;
import com.vincentengelsoftware.androidimagecompare.Activities.CompareModes.SideBySideActivity;
import com.vincentengelsoftware.androidimagecompare.Activities.Settings.ConfigActivity;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityMainBinding;
import com.vincentengelsoftware.androidimagecompare.databinding.DialogCompareModeSelectionBinding;
import com.vincentengelsoftware.androidimagecompare.databinding.DialogResizeImageBinding;
import com.vincentengelsoftware.androidimagecompare.globals.Dimensions;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Settings;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.AskForReview;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapExtractor;
import com.vincentengelsoftware.androidimagecompare.helper.MainHelper;
import com.vincentengelsoftware.androidimagecompare.helper.UriExtractor;
import com.vincentengelsoftware.androidimagecompare.services.KeyValueStorage;
import com.vincentengelsoftware.androidimagecompare.services.Settings.ApplyUserSettings;
import com.vincentengelsoftware.androidimagecompare.services.Settings.ImageResizeSettings;
import com.vincentengelsoftware.androidimagecompare.services.Settings.UserSettings;
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

    protected ActivityMainBinding binding;

    /**
     * TODO clear / refactor onCreate
     */
    protected void onCreate(Bundle savedInstanceState) {
        this.keyValueStorage = new KeyValueStorage(getApplicationContext());
        this.userSettings = UserSettings.getInstance(this.keyValueStorage);
        Settings.init(userSettings);

        ApplyUserSettings.apply(this.userSettings, Images.first, Images.second);

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        if (AskForReview.isItTimeToAsk(getApplicationContext(), this.keyValueStorage)) {
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

    @Override
    protected void onResume() {
        super.onResume();

        Settings.init(userSettings);

        binding.mainButtonLastCompare.setText(
                CompareModeNames.getUserCompareModeNameFromInternalName(getBaseContext(), this.userSettings.getLastCompareMode())
        );

        if (this.userSettings.isShowExtensions()) {
            binding.homeButtonExtensions.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_extension_on));
        } else {
            binding.homeButtonExtensions.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_extension_off));
        }

        if (this.userSettings.isSyncedZoom()) {
            binding.homeButtonLinkZoom.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link));
        } else {
            binding.homeButtonLinkZoom.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off));
        }
    }

    public void restoreImages() {
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

        if (Images.first.getBitmap() != null) {
            Images.first.updateImageViewPreviewImage(binding.homeImageFirst);
            binding.mainTextViewNameImageLeft.setText(Images.first.getImageName());
        }

        if (Images.second.getBitmap() != null) {
            Images.second.updateImageViewPreviewImage(binding.homeImageSecond);
            binding.mainTextViewNameImageRight.setText(Images.second.getImageName());
        }
    }

    public void restoreImageViews() {
        if (Images.first.getBitmap() != null) {
            Images.first.updateImageViewPreviewImage(binding.homeImageFirst);
            binding.mainTextViewNameImageLeft.setText(Images.first.getImageName());
        }

        if (Images.second.getBitmap() != null) {
            Images.second.updateImageViewPreviewImage(binding.homeImageSecond);
            binding.mainTextViewNameImageRight.setText(Images.second.getImageName());
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
                imageView = binding.homeImageFirst;
                imageName = binding.mainTextViewNameImageLeft;
                MainActivity.leftImageUri = imageUri.toString();
            } else {
                imageHolder = Images.second;
                imageView = binding.homeImageSecond;
                imageName = binding.mainTextViewNameImageRight;
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
                        binding.homeImageFirst,
                        binding.mainTextViewNameImageLeft
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
                        binding.homeImageSecond,
                        binding.mainTextViewNameImageRight
                );
                MainActivity.rightImageUri = imageUris.get(1).toString();
            }

            if (imageUris.size() > 2) {
                Toast.makeText(getApplicationContext(), R.string.error_message_intent_more_than_two_images, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setUpActions() {
        binding.mainBtnResizeImageLeft.setOnClickListener(view ->
                openResizeImageDialog(Images.first, this.userSettings.getLeftImageResizeSettings())
        );

        binding.mainBtnResizeImageRight.setOnClickListener(view ->
                openResizeImageDialog(Images.second, this.userSettings.getRightImageResizeSettings())
        );

        binding.mainButtonLastCompare.setText(
                CompareModeNames.getUserCompareModeNameFromInternalName(getBaseContext(), this.userSettings.getLastCompareMode())
        );
        binding.mainButtonLastCompare.setOnClickListener(view -> {
            switch (CompareModeNames.getInternalCompareModeNameFromUserCompareModeName(getBaseContext(), binding.mainButtonLastCompare.getText().toString())) {
                case CompareModeNames.SIDE_BY_SIDE -> openCompareActivity(SideBySideActivity.class);
                case CompareModeNames.OVERLAY_SLIDE -> openCompareActivity(OverlaySlideActivity.class);
                case CompareModeNames.OVERLAY_TAP -> openCompareActivity(OverlayTapActivity.class);
                case CompareModeNames.OVERLAY_TRANSPARENT -> openCompareActivity(OverlayTransparentActivity.class);
                case CompareModeNames.META_DATA -> openCompareActivity(MetaDataActivity.class);
                case CompareModeNames.OVERLAY_CUT -> openCompareActivity(OverlayCutActivity.class);
                default -> openCompareDialog();
            }
        });

        binding.mainButtonCompare.setOnClickListener(view -> openCompareDialog());

        binding.homeButtonInfo.setOnClickListener(view -> {
            if (Status.activityIsOpening) {
                return;
            }
            Status.activityIsOpening = true;
            Intent intent = new Intent(getApplicationContext(), ConfigActivity.class);
            startActivity(intent);
        });

        MainHelper.addSwapImageLogic(
                binding.homeButtonSwapImages,
                Images.first,
                Images.second,
                binding.homeImageFirst,
                binding.homeImageSecond,
                binding.mainTextViewNameImageLeft,
                binding.mainTextViewNameImageRight
        );
        binding.homeButtonSwapImages.setOnLongClickListener(view -> {
            Toast.makeText(getApplicationContext(), getString(R.string.swap_images), Toast.LENGTH_SHORT).show();
            return true;
        });

        if (this.userSettings.isShowExtensions()) {
            binding.homeButtonExtensions.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_extension_on));
        } else {
            binding.homeButtonExtensions.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_extension_off));
        }
        binding.homeButtonExtensions.setOnClickListener(view -> {
            this.userSettings.setShowExtensions(!this.userSettings.isShowExtensions());
            if (this.userSettings.isShowExtensions()) {
                binding.homeButtonExtensions.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_extension_on));
            } else {
                binding.homeButtonExtensions.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_extension_off));
            }
        });
        binding.homeButtonExtensions.setOnLongClickListener(view -> {
            Toast.makeText(getApplicationContext(), getString(R.string.show_extensions_in_compare_modes), Toast.LENGTH_SHORT).show();
            return true;
        });

        if (this.userSettings.isSyncedZoom()) {
            binding.homeButtonLinkZoom.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link));
        } else {
            binding.homeButtonLinkZoom.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off));
        }
        binding.homeButtonLinkZoom.setOnClickListener(view -> {
            this.userSettings.setSyncedZoom(!this.userSettings.isSyncedZoom());
            if (this.userSettings.isSyncedZoom()) {
                binding.homeButtonLinkZoom.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link));
            } else {
                binding.homeButtonLinkZoom.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off));
            }
        });
        binding.homeButtonLinkZoom.setOnLongClickListener(view -> {
            Toast.makeText(getApplicationContext(), getString(R.string.globally_enable_or_disable_linked_zoom), Toast.LENGTH_SHORT).show();
            return true;
        });

        MainHelper.addRotateImageLogic(
                binding.homeButtonRotateImageLeft,
                Images.first,
                binding.homeImageFirst
        );
        binding.homeButtonRotateImageLeft.setOnLongClickListener(view -> {
            Toast.makeText(getApplicationContext(), getString(R.string.rotate_image_left), Toast.LENGTH_SHORT).show();
            return true;
        });

        MainHelper.addRotateImageLogic(
                binding.homeButtonRotateImageRight,
                Images.second,
                binding.homeImageSecond
        );
        binding.homeButtonRotateImageRight.setOnLongClickListener(view -> {
            Toast.makeText(getApplicationContext(), getString(R.string.rotate_image_right), Toast.LENGTH_SHORT).show();
            return true;
        });

        addLoadImageLogic(
                binding.homeImageFirst,
                "first",
                binding.mainTextViewNameImageLeft,
                Images.fileUriFirst
        );

        addLoadImageLogic(
                binding.homeImageSecond,
                "second",
                binding.mainTextViewNameImageRight,
                Images.fileUriSecond
        );
    }

    private void openResizeImageDialog(ImageHolder imageHolder, ImageResizeSettings imageResizeSettings) {
        Dialog dialog = new Dialog(this);
        // Inflate the layout using view binding
        DialogResizeImageBinding dialogBinding = DialogResizeImageBinding.inflate(getLayoutInflater());
        // Set the dialog's content view to the root of the inflated binding
        dialog.setContentView(dialogBinding.getRoot()); //MODIFIED LINE

        WindowMetricsCalculator windowMetricsCalculator = WindowMetricsCalculator.getOrCreate();
        WindowMetrics windowMetrics = windowMetricsCalculator.computeCurrentWindowMetrics(this);
        Rect bounds = windowMetrics.getBounds();
        int widthPixel = bounds.width();

        widthPixel = (int) (widthPixel * 0.9);

        // Now, modifications to views via dialogBinding will apply to the displayed dialog
        dialogBinding.dialogResizeImageLinearLayout.setMinimumWidth(widthPixel);

        dialogBinding.dialogResizeImageRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.dialog_resize_image_radio_button_original) {
                dialogBinding.dialogResizeImageOriginalInfoText.setVisibility(View.VISIBLE);
                dialogBinding.dialogResizeImageAutomaticInfoText.setVisibility(View.GONE);
                dialogBinding.dialogResizeImageCustomSettings.setVisibility(View.GONE);
            } else if (checkedId == R.id.dialog_resize_image_radio_button_automatic) {
                dialogBinding.dialogResizeImageOriginalInfoText.setVisibility(View.GONE);
                dialogBinding.dialogResizeImageAutomaticInfoText.setVisibility(View.VISIBLE);
                dialogBinding.dialogResizeImageCustomSettings.setVisibility(View.GONE);
            } else if (checkedId == R.id.dialog_resize_image_radio_button_custom) {
                dialogBinding.dialogResizeImageOriginalInfoText.setVisibility(View.GONE);
                dialogBinding.dialogResizeImageAutomaticInfoText.setVisibility(View.GONE);
                dialogBinding.dialogResizeImageCustomSettings.setVisibility(View.VISIBLE);
            }
        });

        switch (imageResizeSettings.getImageResizeOption()) {
            case Images.RESIZE_OPTION_ORIGINAL:
                dialogBinding.dialogResizeImageRadioGroup.check(R.id.dialog_resize_image_radio_button_original);
                break;
            case Images.RESIZE_OPTION_AUTOMATIC:
                dialogBinding.dialogResizeImageRadioGroup.check(R.id.dialog_resize_image_radio_button_automatic);
                break;
            case Images.RESIZE_OPTION_CUSTOM: {
                dialogBinding.dialogResizeImageRadioGroup.check(R.id.dialog_resize_image_radio_button_custom);
                dialogBinding.dialogResizeImageInputHeight.setText(String.valueOf(imageResizeSettings.getImageResizeHeight()));
                dialogBinding.dialogResizeImageInputWidth.setText(String.valueOf(imageResizeSettings.getImageResizeWidth()));
                break;
            }
            default:
                dialogBinding.dialogResizeImageRadioGroup.check(R.id.dialog_resize_image_radio_button_automatic);
                break;
        }

        dialogBinding.dialogResizeImageBtnDone.setOnClickListener(v -> {
            int checkedId = dialogBinding.dialogResizeImageRadioGroup.getCheckedRadioButtonId();

            if (checkedId == R.id.dialog_resize_image_radio_button_original) {
                imageHolder.setResizeOption(Images.RESIZE_OPTION_ORIGINAL);
                imageResizeSettings.setImageResizeOption(Images.RESIZE_OPTION_ORIGINAL);
            } else if (checkedId == R.id.dialog_resize_image_radio_button_automatic) {
                imageHolder.setResizeOption(Images.RESIZE_OPTION_AUTOMATIC);
                imageResizeSettings.setImageResizeOption(Images.RESIZE_OPTION_AUTOMATIC);
            } else if (checkedId == R.id.dialog_resize_image_radio_button_custom) {
                int height;
                int width;

                try {
                    height = Integer.parseInt(dialogBinding.dialogResizeImageInputHeight.getText().toString());
                    width = Integer.parseInt(dialogBinding.dialogResizeImageInputWidth.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_msg_invalid_input_not_a_number), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (height <= 0 || width <= 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_msg_invalid_input_number_gt_zero), Toast.LENGTH_SHORT).show();
                    return;
                }

                imageResizeSettings.setImageResizeOption(Images.RESIZE_OPTION_CUSTOM);
                imageResizeSettings.setImageResizeHeight(height);
                imageResizeSettings.setImageResizeWidth(width);

                imageHolder.setResizeOption(Images.RESIZE_OPTION_CUSTOM);
                imageHolder.setCustomSize(height, width);
            }

            imageHolder.resetBitmapResized();

            dialog.dismiss();
        });

        dialog.show();
    }

    private void openCompareDialog() {
        DialogCompareModeSelectionBinding dialogBinding =
                DialogCompareModeSelectionBinding.inflate(getLayoutInflater());

        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogBinding.getRoot());

        addCompareDialogButtonOnClickLogic(dialogBinding.selectCompareModeDialogBtnSideBySide, SideBySideActivity.class, dialog);
        addCompareDialogButtonOnClickLogic(dialogBinding.selectCompareModeDialogBtnOverlaySlide, OverlaySlideActivity.class, dialog);
        addCompareDialogButtonOnClickLogic(dialogBinding.selectCompareModeDialogBtnTransparent, OverlayTransparentActivity.class, dialog);
        addCompareDialogButtonOnClickLogic(dialogBinding.selectCompareModeDialogBtnOverlayTap, OverlayTapActivity.class, dialog);
        addCompareDialogButtonOnClickLogic(dialogBinding.selectCompareModeDialogBtnMetadata, MetaDataActivity.class, dialog);
        addCompareDialogButtonOnClickLogic(dialogBinding.selectCompareModeDialogBtnOverlayCut, OverlayCutActivity.class, dialog);

        dialog.show();
    }

    private void addCompareDialogButtonOnClickLogic(
            Button button,
            Class<?> targetActivity,
            Dialog dialog
    ) {
        button.setOnClickListener(view -> {
            openCompareActivity(targetActivity);
            dialog.dismiss();
        });
    }

    private void openCompareActivity(Class<?> targetActivity) {
        try {
            if (Images.first.getBitmap() == null || Images.second.getBitmap() == null || Status.activityIsOpening) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_msg_missing_images), Toast.LENGTH_SHORT).show();
                return;
            }
            Status.activityIsOpening = true;

            String internalCompareModeName = CompareModeNames.getInternalCompareModeNameByActivity(targetActivity);
            this.userSettings.setLastCompareMode(internalCompareModeName);
            binding.mainButtonLastCompare.setText(
                    CompareModeNames.getUserCompareModeNameFromInternalName(getBaseContext(), internalCompareModeName)
            );

            Intent intent = new Intent(getApplicationContext(), targetActivity);
            intent.putExtra(IntentExtras.SHOW_EXTENSIONS, this.userSettings.isShowExtensions());
            intent.putExtra(IntentExtras.SYNCED_ZOOM, this.userSettings.isSyncedZoom());
            intent.putExtra(IntentExtras.HAS_HARDWARE_KEY, Status.HAS_HARDWARE_KEY);
            intent.putExtra(IntentExtras.IMAGE_URI_ONE, "bitmap_uri");
            intent.putExtra(IntentExtras.IMAGE_URI_TWO, "bitmap_uri");

            Thread t = new Thread(() -> {
                try {
                    runOnUiThread(() -> binding.pbProgess.setVisibility(View.VISIBLE));

                    // Run in separate threads to improve performance
                    Images.first.calculateRotatedBitmap();
                    Images.second.calculateRotatedBitmap();

                    runOnUiThread(() -> binding.pbProgess.setVisibility(View.GONE));

                    startActivity(intent);
                } catch (Exception ignored) {
                    Status.activityIsOpening = false;
                    Toast.makeText(getApplicationContext(), R.string.error_message_general, Toast.LENGTH_SHORT).show();
                }
            });

            t.start();
        } catch (Exception ignored) {
            Status.activityIsOpening = false;
            Toast.makeText(getApplicationContext(), R.string.error_message_general, Toast.LENGTH_SHORT).show();
        }
    }

    private void addLoadImageLogic(ImageView imageView, String imageHolderName, TextView imageNameText, Uri fileUri) {
        ActivityResultLauncher<String> imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        Toast.makeText(getApplicationContext(), R.string.error_message_general, Toast.LENGTH_SHORT).show();
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
                            imageHolder.updateImageViewPreviewImage(imageView);
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
                            Toast.makeText(getApplicationContext(), R.string.error_message_general, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            ImageHolder imageHolder;
                            if (Objects.equals(fileUri.getPath(), Images.fileUriFirst.getPath())) {
                                imageHolder = Images.first;
                                MainActivity.leftImageUri = fileUri.toString();
                            } else {
                                imageHolder = Images.second;
                                MainActivity.rightImageUri = fileUri.toString();
                            }

                            imageHolder.updateFromBitmap(
                                    BitmapExtractor.fromUri(this.getContentResolver(), fileUri),
                                    Dimensions.maxSide,
                                    Dimensions.maxSideForPreview,
                                    MainHelper.getImageName(this, fileUri)
                            );
                        } catch (Exception ignored) {
                        }

                        runOnUiThread(this::restoreImageViews);
                    }
            );

            imageView.setOnClickListener(view -> {
                if (Status.activityIsOpening) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                final CharSequence[] optionsMenu = {getString(R.string.load_image_camera), getString(R.string.load_image_gallery), getString(R.string.share_image)};

                builder.setItems(optionsMenu, (dialogInterface, i) -> {
                    if (optionsMenu[i].equals(getString(R.string.load_image_camera))) {
                        if (MainHelper.checkPermission(MainActivity.this)) {
                            mGetContentCamera.launch(fileUri);
                        } else {
                            MainHelper.requestPermission(MainActivity.this);
                            imageView.callOnClick();
                        }
                    } else if (optionsMenu[i].equals(getString(R.string.load_image_gallery))) {
                        imagePicker.launch("image/*");
                    } else if (optionsMenu[i].equals(getString(R.string.share_image))) {
                        try {
                            Uri imageUri;
                            if (Objects.equals(imageHolderName, "first")) {
                                if (MainActivity.leftImageUri == null) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.error_msg_missing_images), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                imageUri = Uri.parse(MainActivity.leftImageUri);
                            } else {
                                if (MainActivity.rightImageUri == null) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.error_msg_missing_images), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                imageUri = Uri.parse(MainActivity.rightImageUri);
                            }

                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("image/*");
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                            startActivity(Intent.createChooser(intent, getString(R.string.share_image)));
                        } catch (Exception ignored) {
                            Toast.makeText(getApplicationContext(), getString(R.string.error_message_general), Toast.LENGTH_SHORT).show();
                        }
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

        builder.setMessage(R.string.ask_for_review_text);

        builder.setPositiveButton(R.string.ask_for_review_positive, (dialogInterface, i) -> {
            if (isPlayStoreInstalled()) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        });

        builder.setNegativeButton(R.string.ask_for_review_negative, (dialogInterface, i) -> {});

        builder.show();
    }

    @SuppressWarnings("deprecation")
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