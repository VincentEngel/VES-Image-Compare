package com.vincentengelsoftware.androidimagecompare.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.window.layout.WindowMetrics;
import androidx.window.layout.WindowMetricsCalculator;

import com.vincentengelsoftware.androidimagecompare.Activities.CompareModes.CompareModeNames;
import com.vincentengelsoftware.androidimagecompare.Activities.CompareModes.OverlayCutActivity;
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
import com.vincentengelsoftware.androidimagecompare.helper.ImageFileSaver;
import com.vincentengelsoftware.androidimagecompare.helper.MainHelper;
import com.vincentengelsoftware.androidimagecompare.helper.UriExtractor;
import com.vincentengelsoftware.androidimagecompare.services.KeyValueStorage;
import com.vincentengelsoftware.androidimagecompare.services.Settings.ApplyUserSettings;
import com.vincentengelsoftware.androidimagecompare.services.Settings.ImageResizeSettings;
import com.vincentengelsoftware.androidimagecompare.services.Settings.UserSettings;
import com.vincentengelsoftware.androidimagecompare.util.ImageInfoHolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static Uri leftImageUri;
    public static Uri rightImageUri;
    public static final String leftImageUriKey = "leftImageUriKey";
    public static final String rightImageUriKey = "rightImageUriKey";

    private UserSettings userSettings;

    protected ActivityMainBinding binding;

    protected void onCreate(Bundle savedInstanceState) {
        KeyValueStorage keyValueStorage = new KeyValueStorage(getApplicationContext());
        this.userSettings = UserSettings.getInstance(keyValueStorage);
        Settings.init(userSettings);

        ApplyUserSettings.apply(this.userSettings, Images.first, Images.second);

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Status.HAS_HARDWARE_KEY = ViewConfiguration.get(this).hasPermanentMenuKey();

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
            String savedLeft = savedInstanceState.getString(MainActivity.leftImageUriKey);
            String savedRight = savedInstanceState.getString(MainActivity.rightImageUriKey);
            if (savedLeft != null) {
                MainActivity.leftImageUri = Uri.parse(savedLeft);
            }
            if (savedRight != null) {
                MainActivity.rightImageUri = Uri.parse(savedRight);
            }
            restoreImages();
        }

        restoreImageViews();

        setUpActions();

        if (Status.handleIntentOnCreate) {
            Status.handleIntentOnCreate = false;
            this.handleIntent(getIntent());
        }

        AskForReview.askForReviewWhenNecessary(getApplicationContext(), keyValueStorage);
    }

    @Override
    protected void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (MainActivity.leftImageUri != null) {
            outState.putString(MainActivity.leftImageUriKey, MainActivity.leftImageUri.toString());
        }
        if (MainActivity.rightImageUri != null) {
            outState.putString(MainActivity.rightImageUriKey, MainActivity.rightImageUri.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            File cacheDir = getCacheDir();
            File[] files = cacheDir.listFiles();
            if (files != null) {
                Set<String> keepPaths = getKeepPaths();
                for (File file : files) {
                    if (!keepPaths.contains(file.getCanonicalPath())) {
                        file.delete();
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @NonNull
    private static Set<String> getKeepPaths() throws IOException {
        Set<String> keepPaths = new java.util.HashSet<>();

        if (MainActivity.leftImageUri != null) {
            Uri leftUri = MainActivity.leftImageUri;
            if (leftUri.getPath() != null) {
                keepPaths.add(new File(leftUri.getPath()).getCanonicalPath());
            }
        }

        if (MainActivity.rightImageUri != null) {
            Uri rightUri = MainActivity.rightImageUri;
            if (rightUri.getPath() != null) {
                keepPaths.add(new File(rightUri.getPath()).getCanonicalPath());
            }
        }

        return keepPaths;
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

    private static String stripSlotPrefix(String name) {
        if (name != null && name.length() > 2 && (name.startsWith("1_") || name.startsWith("2_"))) {
            return name.substring(2);
        }
        return name;
    }

    public void restoreImages() {
        if (Images.first.getBitmap() == null) {
            try {
                Uri uri = MainActivity.leftImageUri;
                Images.first.updateFromBitmap(
                        BitmapExtractor.fromUri(this.getContentResolver(), uri),
                        Dimensions.maxSide,
                        Dimensions.maxSideForPreview,
                        stripSlotPrefix(MainHelper.getImageName(this, uri))
                );
            } catch (Exception ignored) {
            }
        }
        if (Images.second.getBitmap() == null) {
            try {
                Uri uri = MainActivity.rightImageUri;
                Images.second.updateFromBitmap(
                        BitmapExtractor.fromUri(this.getContentResolver(), uri),
                        Dimensions.maxSide,
                        Dimensions.maxSideForPreview,
                        stripSlotPrefix(MainHelper.getImageName(this, uri))
                );
            } catch (Exception ignored) {
            }
        }

        if (Images.first.getBitmap() != null) {
            Images.first.updateImageViewPreviewImage(binding.homeImageLeft);
            binding.mainTextViewNameImageLeft.setText(Images.first.getImageName());
        }

        if (Images.second.getBitmap() != null) {
            Images.second.updateImageViewPreviewImage(binding.homeImageRight);
            binding.mainTextViewNameImageRight.setText(Images.second.getImageName());
        }
    }

    public void restoreImageViews() {
        if (Images.first.getBitmap() != null) {
            Images.first.updateImageViewPreviewImage(binding.homeImageLeft);
            binding.mainTextViewNameImageLeft.setText(Images.first.getImageName());
        }

        if (Images.second.getBitmap() != null) {
            Images.second.updateImageViewPreviewImage(binding.homeImageRight);
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
            ImageInfoHolder imageInfoHolder;
            ImageView imageView;
            TextView imageName;

            boolean isFirst = Images.first.getBitmap() == null;

            if (isFirst) {
                imageInfoHolder = Images.first;
                imageView = binding.homeImageLeft;
                imageName = binding.mainTextViewNameImageLeft;
            } else {
                imageInfoHolder = Images.second;
                imageView = binding.homeImageRight;
                imageName = binding.mainTextViewNameImageRight;
            }

            String originalName = MainHelper.getImageName(this, imageUri);
            String fileName = isFirst ? "1_" + originalName : "2_" + originalName;
            java.io.File localFile = new java.io.File(getCacheDir(), fileName);
            Uri localUri = ImageFileSaver.saveToFile(getContentResolver(), imageUri, localFile);
            if (localUri == null) return;

            if (isFirst) {
                MainActivity.leftImageUri = localUri;
            } else {
                MainActivity.rightImageUri = localUri;
            }

            MainHelper.updateImageFromIntent(
                    imageInfoHolder,
                    BitmapExtractor.fromUri(this.getContentResolver(), localUri),
                    Dimensions.maxSide,
                    Dimensions.maxSideForPreview,
                    originalName,
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
                Uri srcUri = imageUris.get(0);
                java.io.File localFile = new java.io.File(getCacheDir(), "1_" + MainHelper.getImageName(this, srcUri));
                Uri localUri = ImageFileSaver.saveToFile(getContentResolver(), srcUri, localFile);
                if (localUri != null) {
                    MainHelper.updateImageFromIntent(
                            Images.first,
                            BitmapExtractor.fromUri(this.getContentResolver(), localUri),
                            Dimensions.maxSide,
                            Dimensions.maxSideForPreview,
                            MainHelper.getImageName(this, srcUri),
                            binding.homeImageLeft,
                            binding.mainTextViewNameImageLeft
                    );
                    MainActivity.leftImageUri = localUri;
                }
            }

            if (imageUris.size() > 1 && imageUris.get(1) != null) {
                Uri srcUri = imageUris.get(1);
                java.io.File localFile = new java.io.File(getCacheDir(), "2_" + MainHelper.getImageName(this, srcUri));
                Uri localUri = ImageFileSaver.saveToFile(getContentResolver(), srcUri, localFile);
                if (localUri != null) {
                    MainHelper.updateImageFromIntent(
                            Images.second,
                            BitmapExtractor.fromUri(this.getContentResolver(), localUri),
                            Dimensions.maxSide,
                            Dimensions.maxSideForPreview,
                            MainHelper.getImageName(this, srcUri),
                            binding.homeImageRight,
                            binding.mainTextViewNameImageRight
                    );
                    MainActivity.rightImageUri = localUri;
                }
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
                binding.homeImageLeft,
                binding.homeImageRight,
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
                binding.homeImageLeft
        );
        binding.homeButtonRotateImageLeft.setOnLongClickListener(view -> {
            Toast.makeText(getApplicationContext(), getString(R.string.rotate_image_left), Toast.LENGTH_SHORT).show();
            return true;
        });

        MainHelper.addRotateImageLogic(
                binding.homeButtonRotateImageRight,
                Images.second,
                binding.homeImageRight
        );
        binding.homeButtonRotateImageRight.setOnLongClickListener(view -> {
            Toast.makeText(getApplicationContext(), getString(R.string.rotate_image_right), Toast.LENGTH_SHORT).show();
            return true;
        });

        addLoadImageLogic(
                binding.homeImageLeft,
                "left",
                binding.mainTextViewNameImageLeft
        );

        addLoadImageLogic(
                binding.homeImageRight,
                "right",
                binding.mainTextViewNameImageRight
        );
    }

    private void openResizeImageDialog(ImageInfoHolder imageInfoHolder, ImageResizeSettings imageResizeSettings) {
        Dialog dialog = new Dialog(this);
        // Inflate the layout using view binding
        DialogResizeImageBinding dialogBinding = DialogResizeImageBinding.inflate(getLayoutInflater());
        // Set the dialog's content view to the root of the inflated binding
        dialog.setContentView(dialogBinding.getRoot());

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
                imageInfoHolder.setResizeOption(Images.RESIZE_OPTION_ORIGINAL);
                imageResizeSettings.setImageResizeOption(Images.RESIZE_OPTION_ORIGINAL);
            } else if (checkedId == R.id.dialog_resize_image_radio_button_automatic) {
                imageInfoHolder.setResizeOption(Images.RESIZE_OPTION_AUTOMATIC);
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

                imageInfoHolder.setResizeOption(Images.RESIZE_OPTION_CUSTOM);
                imageInfoHolder.setCustomSize(height, width);
            }

            imageInfoHolder.resetBitmapResized();

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

            Thread t = new Thread(() -> {
                try {
                    runOnUiThread(() -> binding.pbProgess.setVisibility(View.VISIBLE));

                    Images.first.calculateRotatedBitmap();
                    Images.second.calculateRotatedBitmap();

                    File fileOne = new File(getCacheDir(), "compare_image_one.png");
                    File fileTwo = new File(getCacheDir(), "compare_image_two.png");

                    ImageFileSaver.saveBitmapToFile(Images.first.getAdjustedBitmap(), fileOne);
                    ImageFileSaver.saveBitmapToFile(Images.second.getAdjustedBitmap(), fileTwo);

                    Uri uriOne = FileProvider.getUriForFile(
                            getApplicationContext(),
                            getApplicationContext().getPackageName() + ".fileprovider",
                            fileOne
                    );
                    Uri uriTwo = FileProvider.getUriForFile(
                            getApplicationContext(),
                            getApplicationContext().getPackageName() + ".fileprovider",
                            fileTwo
                    );

                    intent.putExtra(IntentExtras.IMAGE_URI_ONE, uriOne.toString());
                    intent.putExtra(IntentExtras.IMAGE_URI_TWO, uriTwo.toString());
                    intent.putExtra(IntentExtras.IMAGE_NAME_ONE, Images.first.getImageName());
                    intent.putExtra(IntentExtras.IMAGE_NAME_TWO, Images.second.getImageName());

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

    private void addLoadImageLogic(ImageView imageView, String imageHolderName, TextView imageNameText) {
        ActivityResultLauncher<String> imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                receivedImageUri -> {
                    if (receivedImageUri == null) {
                        Toast.makeText(getApplicationContext(), R.string.error_message_general, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String originalImageName = MainHelper.getImageName(this, receivedImageUri);
                    String fileName = (Objects.equals(imageHolderName, "left") ? "1_" : "2_") + originalImageName;

                    java.io.File localFile = new java.io.File(getCacheDir(), fileName);

                    // Copy raw bytes to local file without decoding into memory
                    android.net.Uri localUri = ImageFileSaver.saveToFile(
                            getContentResolver(), receivedImageUri, localFile
                    );

                    if (localUri == null) {
                        Toast.makeText(getApplicationContext(), R.string.error_message_general, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    runOnUiThread(() -> {
                        try {
                            ImageInfoHolder imageInfoHolder;
                            if (Objects.equals(imageHolderName, "left")) {
                                MainActivity.leftImageUri = localUri;
                                imageInfoHolder = Images.first;
                            } else {
                                imageInfoHolder = Images.second;
                                MainActivity.rightImageUri = localUri;
                            }

                            imageInfoHolder.updateFromBitmap(
                                    BitmapExtractor.fromUri(this.getContentResolver(), localUri),
                                    Dimensions.maxSide,
                                    Dimensions.maxSideForPreview,
                                    originalImageName
                            );
                            imageInfoHolder.updateImageViewPreviewImage(imageView);
                            imageNameText.setText(imageInfoHolder.getImageName());
                        } catch (Exception ignored) {
                        }
                    });
                });

        try {
            Uri fileUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    new File(this.getCacheDir(), "camera_image_" + System.currentTimeMillis() + ".png")
            );

            ActivityResultLauncher<Uri> mGetContentCamera = registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    result -> {
                        if (!result) {
                            Toast.makeText(getApplicationContext(), R.string.error_message_general, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            ImageInfoHolder imageInfoHolder;
                            if (Objects.equals(imageHolderName, "left")) {
                                imageInfoHolder = Images.first;
                                MainActivity.leftImageUri = fileUri;
                            } else {
                                imageInfoHolder = Images.second;
                                MainActivity.rightImageUri = fileUri;
                            }

                            imageInfoHolder.updateFromBitmap(
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
                                imageUri = MainActivity.leftImageUri;
                            } else {
                                if (MainActivity.rightImageUri == null) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.error_msg_missing_images), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                imageUri = MainActivity.rightImageUri;
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
}