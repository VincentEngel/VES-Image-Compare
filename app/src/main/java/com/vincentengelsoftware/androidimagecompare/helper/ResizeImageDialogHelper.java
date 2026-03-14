package com.vincentengelsoftware.androidimagecompare.helper;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Rect;
import android.view.View;
import android.widget.Toast;

import androidx.window.layout.WindowMetrics;
import androidx.window.layout.WindowMetricsCalculator;

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.DialogResizeImageBinding;
import com.vincentengelsoftware.androidimagecompare.globals.ImageResizeOptions;
import com.vincentengelsoftware.androidimagecompare.services.settings.ImageResizeSettings;
import com.vincentengelsoftware.androidimagecompare.util.imageInformation.ImageInfoHolder;

/**
 * Builds and shows the resize-image options dialog for a given image slot.
 * <p>
 * Use the single static entry point {@link #show(Activity, ImageInfoHolder, ImageResizeSettings)}.
 */
public class ResizeImageDialogHelper {

    private ResizeImageDialogHelper() {}

    /**
     * Inflates, configures, and shows the resize dialog.
     *
     * @param activity           the host activity (used for inflating and window-size queries)
     * @param imageInfoHolder    the holder whose resize option will be updated on confirm
     * @param imageResizeSettings the persisted settings object that will be updated on confirm
     */
    public static void show(
            Activity activity,
            ImageInfoHolder imageInfoHolder,
            ImageResizeSettings imageResizeSettings
    ) {
        Dialog dialog = new Dialog(activity);
        DialogResizeImageBinding dialogBinding =
                DialogResizeImageBinding.inflate(activity.getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        // Size the dialog to 90 % of the screen width.
        WindowMetrics windowMetrics =
                WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity);
        Rect bounds = windowMetrics.getBounds();
        dialogBinding.dialogResizeImageLinearLayout.setMinimumWidth((int) (bounds.width() * 0.9));

        dialogBinding.dialogResizeImageRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isOriginal  = checkedId == R.id.dialog_resize_image_radio_button_original;
            boolean isAutomatic = checkedId == R.id.dialog_resize_image_radio_button_automatic;
            boolean isCustom    = checkedId == R.id.dialog_resize_image_radio_button_custom;
            dialogBinding.dialogResizeImageOriginalInfoText
                    .setVisibility(isOriginal  ? View.VISIBLE : View.GONE);
            dialogBinding.dialogResizeImageAutomaticInfoText
                    .setVisibility(isAutomatic ? View.VISIBLE : View.GONE);
            dialogBinding.dialogResizeImageCustomSettings
                    .setVisibility(isCustom    ? View.VISIBLE : View.GONE);
        });

        // Pre-select the currently persisted option.
        switch (imageResizeSettings.getImageResizeOption()) {
            case ImageResizeOptions.RESIZE_OPTION_ORIGINAL:
                dialogBinding.dialogResizeImageRadioGroup.check(
                        R.id.dialog_resize_image_radio_button_original);
                break;
            case ImageResizeOptions.RESIZE_OPTION_CUSTOM:
                dialogBinding.dialogResizeImageRadioGroup.check(
                        R.id.dialog_resize_image_radio_button_custom);
                dialogBinding.dialogResizeImageInputHeight.setText(
                        String.valueOf(imageResizeSettings.getImageResizeHeight()));
                dialogBinding.dialogResizeImageInputWidth.setText(
                        String.valueOf(imageResizeSettings.getImageResizeWidth()));
                break;
            default: // RESIZE_OPTION_AUTOMATIC
                dialogBinding.dialogResizeImageRadioGroup.check(
                        R.id.dialog_resize_image_radio_button_automatic);
                break;
        }

        dialogBinding.dialogResizeImageBtnDone.setOnClickListener(v -> {
            int checkedId = dialogBinding.dialogResizeImageRadioGroup.getCheckedRadioButtonId();

            if (checkedId == R.id.dialog_resize_image_radio_button_original) {
                imageInfoHolder.setResizeOption(ImageResizeOptions.RESIZE_OPTION_ORIGINAL);
                imageResizeSettings.setImageResizeOption(ImageResizeOptions.RESIZE_OPTION_ORIGINAL);

            } else if (checkedId == R.id.dialog_resize_image_radio_button_automatic) {
                imageInfoHolder.setResizeOption(ImageResizeOptions.RESIZE_OPTION_AUTOMATIC);
                imageResizeSettings.setImageResizeOption(ImageResizeOptions.RESIZE_OPTION_AUTOMATIC);

            } else if (checkedId == R.id.dialog_resize_image_radio_button_custom) {
                int height, width;
                try {
                    height = Integer.parseInt(
                            dialogBinding.dialogResizeImageInputHeight.getText().toString());
                    width  = Integer.parseInt(
                            dialogBinding.dialogResizeImageInputWidth.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(activity.getApplicationContext(),
                            R.string.error_msg_invalid_input_not_a_number, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (height <= 0 || width <= 0) {
                    Toast.makeText(activity.getApplicationContext(),
                            R.string.error_msg_invalid_input_number_gt_zero, Toast.LENGTH_SHORT).show();
                    return;
                }

                imageResizeSettings.setImageResizeOption(ImageResizeOptions.RESIZE_OPTION_CUSTOM);
                imageResizeSettings.setImageResizeHeight(height);
                imageResizeSettings.setImageResizeWidth(width);
                imageInfoHolder.setResizeOption(ImageResizeOptions.RESIZE_OPTION_CUSTOM);
                imageInfoHolder.setCustomSize(height, width);
            }

            imageInfoHolder.resetBitmapResized();
            dialog.dismiss();
        });

        dialog.show();
    }
}

