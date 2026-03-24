package com.vincentengelsoftware.androidimagecompare.ui.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.data.cache.CacheManager;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityMainBinding;
import com.vincentengelsoftware.androidimagecompare.domain.model.ImageInfoHolder;
import com.vincentengelsoftware.androidimagecompare.domain.model.ImageSessionState;
import com.vincentengelsoftware.androidimagecompare.util.BitmapExtractor;
import com.vincentengelsoftware.androidimagecompare.util.Dimensions;
import com.vincentengelsoftware.androidimagecompare.util.ImageFileSaver;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Registers and wires the gallery-picker and camera {@link ActivityResultLauncher}s for a single
 * image slot ({@code "left"} or {@code "right"}).
 *
 * <p><strong>Lifecycle requirement:</strong> call {@link #create} in {@code Activity.onCreate}
 * before the activity reaches the STARTED state, because {@link ActivityResultLauncher}s must be
 * registered before that point.
 *
 * <p>The helper also handles the three-option image-source dialog (camera / gallery / share) that
 * appears when the user taps an image preview.
 */
public class ImageSlotPickerHelper {

  private final AppCompatActivity activity;
  private final String slot; // "left" or "right"
  private final ImageSessionState sessionState;
  private final ActivityMainBinding binding;
  private final AtomicBoolean openingActivity;
  private final Dimensions dimensions;

  private final ActivityResultLauncher<String> galleryPicker;
  private final ActivityResultLauncher<Uri> cameraPicker;

  // ── construction ──────────────────────────────────────────────────────────

  private ImageSlotPickerHelper(
      AppCompatActivity activity,
      String slot,
      ImageSessionState sessionState,
      ActivityMainBinding binding,
      AtomicBoolean openingActivity,
      Dimensions dimensions) {
    this.activity = activity;
    this.slot = slot;
    this.sessionState = sessionState;
    this.binding = binding;
    this.openingActivity = openingActivity;
    this.dimensions = dimensions;

    // Launchers must be registered before the activity starts.
    this.galleryPicker =
        activity.registerForActivityResult(
            new ActivityResultContracts.GetContent(), this::onGalleryResult);
    this.cameraPicker =
        activity.registerForActivityResult(
            new ActivityResultContracts.TakePicture(), this::onCameraResult);
  }

  /**
   * Creates and returns an {@code ImageSlotPickerHelper} with its launchers registered.
   *
   * @param slot either {@code "left"} or {@code "right"}
   */
  public static ImageSlotPickerHelper create(
      AppCompatActivity activity,
      String slot,
      ImageSessionState sessionState,
      ActivityMainBinding binding,
      AtomicBoolean openingActivity,
      Dimensions dimensions) {
    return new ImageSlotPickerHelper(
        activity, slot, sessionState, binding, openingActivity, dimensions);
  }

  // ── public API ────────────────────────────────────────────────────────────

  /**
   * Returns the {@link View.OnClickListener} to attach to the image preview view. Tapping shows a
   * dialog offering camera, gallery, or share actions.
   */
  public View.OnClickListener buildClickListener() {
    return view -> {
      if (openingActivity.get()) return;

      CharSequence[] options = {
        activity.getString(R.string.load_image_camera),
        activity.getString(R.string.load_image_gallery),
        activity.getString(R.string.share_image)
      };

      new AlertDialog.Builder(activity)
          .setItems(
              options,
              (dialogInterface, i) -> {
                if (options[i].equals(activity.getString(R.string.load_image_camera))) {
                  launchCamera();
                } else if (options[i].equals(activity.getString(R.string.load_image_gallery))) {
                  galleryPicker.launch("image/*");
                } else {
                  shareCurrentImage();
                }
                dialogInterface.dismiss();
              })
          .create()
          .show();
    };
  }

  // ── private: camera ───────────────────────────────────────────────────────

  private void launchCamera() {
    if (!MainHelper.checkPermission(activity)) {
      MainHelper.requestPermission(activity);
      return;
    }
    try {
      File cameraTemp = CacheManager.getCameraTempFile(activity.getCacheDir());
      Uri cameraFileUri =
          FileProvider.getUriForFile(
              activity,
              activity.getApplicationContext().getPackageName() + ".fileprovider",
              cameraTemp);
      cameraPicker.launch(cameraFileUri);
    } catch (Exception ignored) {
      Toast.makeText(activity, R.string.error_message_general, Toast.LENGTH_SHORT).show();
    }
  }

  private void onCameraResult(boolean success) {
    if (!success) {
      Toast.makeText(activity, R.string.error_message_general, Toast.LENGTH_SHORT).show();
      return;
    }
    // File I/O and bitmap decoding are too heavy for the main thread (ANR risk for large
    // camera photos). The ActivityResultLauncher callback is always delivered on the main
    // thread, so we hand off immediately to a background thread.
    new Thread(
            () -> {
              try {
                // Copy the shared temp file to a timestamped file so that a subsequent camera
                // capture for the other slot does not overwrite this one.
                File tempFile = CacheManager.getCameraTempFile(activity.getCacheDir());
                File destFile =
                    new File(activity.getCacheDir(), "IMG_" + System.currentTimeMillis() + ".jpg");
                Uri destUri =
                    ImageFileSaver.saveUriToFile(
                        activity.getContentResolver(), Uri.fromFile(tempFile), destFile);

                if (destUri == null) {
                  activity.runOnUiThread(
                      () ->
                          Toast.makeText(
                                  activity, R.string.error_message_general, Toast.LENGTH_SHORT)
                              .show());
                  return;
                }

                Bitmap bitmap = BitmapExtractor.fromUri(activity.getContentResolver(), destUri);
                if (bitmap == null) {
                  activity.runOnUiThread(
                      () ->
                          Toast.makeText(
                                  activity, R.string.error_message_general, Toast.LENGTH_SHORT)
                              .show());
                  return;
                }

                setUri(destUri);
                getHolder()
                    .updateFromBitmap(
                        bitmap,
                        dimensions.maxSide(),
                        dimensions.maxSideForPreview(),
                        MainHelper.getImageName(activity, destUri));
              } catch (Exception ignored) {
              }

              activity.runOnUiThread(
                  () -> ImageRestoreHelper.restoreImageViews(sessionState, binding));
            })
        .start();
  }

  // ── private: gallery ──────────────────────────────────────────────────────

  private void onGalleryResult(Uri receivedUri) {
    if (receivedUri == null) {
      Toast.makeText(activity, R.string.error_message_general, Toast.LENGTH_SHORT).show();
      return;
    }

    String originalName = MainHelper.getImageName(activity, receivedUri);
    String prefix = isLeft() ? "1_" : "2_";
    File localFile = new File(activity.getCacheDir(), prefix + originalName);
    Uri localUri =
        ImageFileSaver.saveUriToFile(activity.getContentResolver(), receivedUri, localFile);

    if (localUri == null) {
      Toast.makeText(activity, R.string.error_message_general, Toast.LENGTH_SHORT).show();
      return;
    }

    setUri(localUri);

    ImageInfoHolder holder = getHolder();
    holder.updateFromBitmap(
        BitmapExtractor.fromUri(activity.getContentResolver(), localUri),
        dimensions.maxSide(),
        dimensions.maxSideForPreview(),
        originalName);
    (isLeft() ? binding.homeImageLeft : binding.homeImageRight)
        .setImageBitmap(holder.getBitmapSmall());
    (isLeft() ? binding.mainTextViewNameImageLeft : binding.mainTextViewNameImageRight)
        .setText(holder.getImageName());
  }

  // ── private: share ────────────────────────────────────────────────────────

  private void shareCurrentImage() {
    try {
      Uri imageUri = isLeft() ? sessionState.getLeftImageUri() : sessionState.getRightImageUri();

      if (imageUri == null) {
        Toast.makeText(
                activity, activity.getString(R.string.error_msg_missing_images), Toast.LENGTH_SHORT)
            .show();
        return;
      }

      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("image/*");
      shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
      activity.startActivity(
          Intent.createChooser(shareIntent, activity.getString(R.string.share_image)));
    } catch (Exception ignored) {
      Toast.makeText(
              activity, activity.getString(R.string.error_message_general), Toast.LENGTH_SHORT)
          .show();
    }
  }

  // ── private utilities ─────────────────────────────────────────────────────

  private boolean isLeft() {
    return "left".equals(slot);
  }

  private ImageInfoHolder getHolder() {
    return isLeft()
        ? sessionState.getFirstImageInfoHolder()
        : sessionState.getSecondImageInfoHolder();
  }

  private void setUri(Uri uri) {
    if (isLeft()) sessionState.setLeftImageUri(uri);
    else sessionState.setRightImageUri(uri);
  }
}
