package com.vincentengelsoftware.androidimagecompare.ui.main;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import com.vincentengelsoftware.androidimagecompare.data.cache.CacheManager;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityMainBinding;
import com.vincentengelsoftware.androidimagecompare.domain.model.ImageInfoHolder;
import com.vincentengelsoftware.androidimagecompare.domain.model.ImageSessionState;
import com.vincentengelsoftware.androidimagecompare.util.BitmapExtractor;
import com.vincentengelsoftware.androidimagecompare.util.Dimensions;
import java.io.File;

/**
 * Restores the left/right image slots from persisted URIs and Bundle transform state after a
 * configuration change or process death.
 *
 * <p>The two entry points mirror the two restore phases in {@code Activity.onCreate}:
 *
 * <ol>
 *   <li>{@link #restoreImages} — re-decodes bitmaps from disk when the in-memory holders are empty
 *       (process was killed).
 *   <li>{@link #restoreImageViews} — re-applies already-loaded bitmaps to the preview {@link
 *       android.widget.ImageView}s after every configuration change.
 * </ol>
 */
public class ImageRestoreHelper {

  private ImageRestoreHelper() {}

  /**
   * Loads bitmaps from the stored URIs when the in-memory holders are empty, then updates the
   * preview views for every holder that has a bitmap.
   */
  public static void restoreImages(
      AppCompatActivity activity,
      ImageSessionState sessionState,
      ActivityMainBinding binding,
      Dimensions dimensions) {
    ContentResolver cr = activity.getContentResolver();
    File cacheDir = activity.getCacheDir();

    ImageInfoHolder first = sessionState.getFirstImageInfoHolder();
    ImageInfoHolder second = sessionState.getSecondImageInfoHolder();

    if (first.getBitmap() == null && sessionState.getLeftImageUri() != null) {
      loadHolder(
          cr,
          sessionState.getLeftImageUri(),
          first,
          cacheDir,
          CacheManager.COMPARE_FILE_ONE,
          activity,
          dimensions);
    }
    if (second.getBitmap() == null && sessionState.getRightImageUri() != null) {
      loadHolder(
          cr,
          sessionState.getRightImageUri(),
          second,
          cacheDir,
          CacheManager.COMPARE_FILE_TWO,
          activity,
          dimensions);
    }

    updateViews(sessionState, binding);
  }

  /**
   * Re-applies already-loaded bitmaps to the preview image views. Call this after every
   * configuration change when bitmaps are already in memory.
   */
  public static void restoreImageViews(
      ImageSessionState sessionState, ActivityMainBinding binding) {
    updateViews(sessionState, binding);
  }

  /**
   * Strips the {@code "1_"} / {@code "2_"} slot prefix that is prepended when a file is copied into
   * the cache, so that only the original filename is shown to the user.
   */
  public static String stripSlotPrefix(String name) {
    if (name != null && name.length() > 2 && (name.startsWith("1_") || name.startsWith("2_"))) {
      return name.substring(2);
    }
    return name;
  }

  private static void loadHolder(
      ContentResolver cr,
      Uri uri,
      ImageInfoHolder holder,
      File cacheDir,
      String compareFileName,
      AppCompatActivity activity,
      Dimensions dimensions) {
    try {
      Bitmap bitmap = BitmapExtractor.fromUri(cr, uri);
      if (bitmap == null) return;

      holder.updateFromBitmap(
          bitmap,
          dimensions.maxSide(),
          dimensions.maxSideForPreview(),
          stripSlotPrefix(MainHelper.getImageName(activity, uri)));

      if (new File(cacheDir, compareFileName).exists()) {
        holder.markSaved();
      }
    } catch (Exception ignored) {
    }
  }

  private static void updateViews(ImageSessionState sessionState, ActivityMainBinding binding) {
    ImageInfoHolder first = sessionState.getFirstImageInfoHolder();
    ImageInfoHolder second = sessionState.getSecondImageInfoHolder();

    if (first.getBitmap() != null) {
      binding.homeImageLeft.setImageBitmap(first.getBitmapSmall());
      binding.mainTextViewNameImageLeft.setText(first.getImageName());
    }
    if (second.getBitmap() != null) {
      binding.homeImageRight.setImageBitmap(second.getBitmapSmall());
      binding.mainTextViewNameImageRight.setText(second.getImageName());
    }
  }
}
