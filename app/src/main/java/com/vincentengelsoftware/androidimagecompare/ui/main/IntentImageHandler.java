package com.vincentengelsoftware.androidimagecompare.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityMainBinding;
import com.vincentengelsoftware.androidimagecompare.domain.model.ImageInfoHolder;
import com.vincentengelsoftware.androidimagecompare.domain.model.ImageSessionState;
import com.vincentengelsoftware.androidimagecompare.util.BitmapExtractor;
import com.vincentengelsoftware.androidimagecompare.util.Dimensions;
import com.vincentengelsoftware.androidimagecompare.util.ImageFileSaver;
import com.vincentengelsoftware.androidimagecompare.util.UriExtractor;
import java.io.File;
import java.util.ArrayList;

/**
 * Handles incoming {@link Intent#ACTION_SEND} and {@link Intent#ACTION_SEND_MULTIPLE} share intents
 * that deliver images to this app.
 *
 * <p>The first shared image always fills the left slot; the second fills the right slot. Sharing
 * more than two images shows a toast and discards the extras.
 */
public class IntentImageHandler {

  private IntentImageHandler() {}

  /**
   * Entry point called from both {@code Activity.onCreate} and {@code Activity.onNewIntent}.
   * Silently ignores intents that are not image-share intents.
   */
  public static void handleIntent(
      Intent intent,
      AppCompatActivity activity,
      ImageSessionState sessionState,
      ActivityMainBinding binding,
      Dimensions dimensions) {
    try {
      String action = intent.getAction();
      String type = intent.getType();

      if (Intent.ACTION_SEND.equals(action) && type != null && type.startsWith("image/")) {
        handleSendImage(intent, activity, sessionState, binding, dimensions);
      } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)
          && type != null
          && type.startsWith("image/")) {
        handleSendMultipleImages(intent, activity, sessionState, binding, dimensions);
      }
    } catch (Exception ignored) {
    }
  }

  // ── private ───────────────────────────────────────────────────────────────

  private static void handleSendImage(
      Intent intent,
      AppCompatActivity activity,
      ImageSessionState sessionState,
      ActivityMainBinding binding,
      Dimensions dimensions) {
    Uri imageUri = UriExtractor.getOutOfParcelableExtra(intent);
    if (imageUri == null) return;

    boolean isFirst = sessionState.getFirstImageInfoHolder().getBitmap() == null;
    ImageInfoHolder holder =
        isFirst ? sessionState.getFirstImageInfoHolder() : sessionState.getSecondImageInfoHolder();

    String originalName = MainHelper.getImageName(activity, imageUri);
    String fileName = (isFirst ? "1_" : "2_") + originalName;
    File localFile = new File(activity.getCacheDir(), fileName);
    Uri localUri = ImageFileSaver.saveUriToFile(activity.getContentResolver(), imageUri, localFile);

    if (localUri == null) return;

    if (isFirst) sessionState.setLeftImageUri(localUri);
    else sessionState.setRightImageUri(localUri);

    MainHelper.updateImageFromIntent(
        holder,
        BitmapExtractor.fromUri(activity.getContentResolver(), localUri),
        dimensions.maxSide(),
        dimensions.maxSideForPreview(),
        originalName,
        isFirst ? binding.homeImageLeft : binding.homeImageRight,
        isFirst ? binding.mainTextViewNameImageLeft : binding.mainTextViewNameImageRight);
  }

  private static void handleSendMultipleImages(
      Intent intent,
      AppCompatActivity activity,
      ImageSessionState sessionState,
      ActivityMainBinding binding,
      Dimensions dimensions) {
    ArrayList<Uri> imageUris = UriExtractor.getOutOfParcelableArrayListExtra(intent);
    if (imageUris == null) return;

    if (!imageUris.isEmpty() && imageUris.get(0) != null) {
      Uri src = imageUris.get(0);
      String name = MainHelper.getImageName(activity, src);
      File dest = new File(activity.getCacheDir(), "1_" + name);
      Uri localUri = ImageFileSaver.saveUriToFile(activity.getContentResolver(), src, dest);
      if (localUri != null) {
        sessionState.setLeftImageUri(localUri);
        MainHelper.updateImageFromIntent(
            sessionState.getFirstImageInfoHolder(),
            BitmapExtractor.fromUri(activity.getContentResolver(), localUri),
            dimensions.maxSide(),
            dimensions.maxSideForPreview(),
            name,
            binding.homeImageLeft,
            binding.mainTextViewNameImageLeft);
      }
    }

    if (imageUris.size() > 1 && imageUris.get(1) != null) {
      Uri src = imageUris.get(1);
      String name = MainHelper.getImageName(activity, src);
      File dest = new File(activity.getCacheDir(), "2_" + name);
      Uri localUri = ImageFileSaver.saveUriToFile(activity.getContentResolver(), src, dest);
      if (localUri != null) {
        sessionState.setRightImageUri(localUri);
        MainHelper.updateImageFromIntent(
            sessionState.getSecondImageInfoHolder(),
            BitmapExtractor.fromUri(activity.getContentResolver(), localUri),
            dimensions.maxSide(),
            dimensions.maxSideForPreview(),
            name,
            binding.homeImageRight,
            binding.mainTextViewNameImageRight);
      }
    }

    if (imageUris.size() > 2) {
      Toast.makeText(
              activity.getApplicationContext(),
              R.string.error_message_intent_more_than_two_images,
              Toast.LENGTH_LONG)
          .show();
    }
  }
}
