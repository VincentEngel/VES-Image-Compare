package com.vincentengelsoftware.androidimagecompare.domain.model;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import com.vincentengelsoftware.androidimagecompare.constants.ImageResizeOptions;
import com.vincentengelsoftware.androidimagecompare.util.BitmapTransformer;
import java.io.File;

public class ImageInfoHolder {
  private ImageSource source = new ImageSource(null, null, 0, 0);
  private final BitmapTransformSettings transformSettings = new BitmapTransformSettings();
  private final PreviewBitmap previewBitmap = new PreviewBitmap();
  private AdjustImageState stateSaver = AdjustImageState.empty();

  private static final int DEGREES_PER_ROTATION_STEP = 90;

  /**
   * Initialises this holder from a freshly decoded bitmap. All cached bitmaps and the save-state
   * snapshot are cleared so the image is treated as new.
   */
  public void updateFromBitmap(
      Bitmap bitmap, int maxSideSize, int maxSideSizeForSmallBitmap, String imageName) {
    source = new ImageSource(bitmap, imageName, maxSideSize, maxSideSizeForSmallBitmap);
    transformSettings.reset();
    previewBitmap.invalidate();
    stateSaver = AdjustImageState.empty();
  }

  /** Copies all state from another holder into this one (used by the swap-images feature). */
  public void updateFromImageHolder(ImageInfoHolder other) {
    source =
        new ImageSource(
            other.source.bitmap(),
            other.source.imageName(),
            other.source.maxSideSize(),
            other.source.maxSideSizeForSmallBitmap());
    transformSettings.copyFrom(other.transformSettings);
    previewBitmap.updateFrom(other.previewBitmap);
    stateSaver = AdjustImageState.empty();
  }

  public void setResizeOption(int resizeOption) {
    transformSettings.setResizeOption(resizeOption);
    stateSaver = AdjustImageState.empty();
  }

  public void setCustomSize(int height, int width) {
    transformSettings.setCustomSize(height, width);
    stateSaver = AdjustImageState.empty();
  }

  public Bitmap getBitmap() {
    return source.bitmap();
  }

  public String getImageName() {
    return source.imageName();
  }

  /** Returns the small preview bitmap, computing it lazily if necessary. */
  public Bitmap getBitmapSmall() {
    return previewBitmap.getSmall(source, transformSettings);
  }

  /** Clears the cached resized bitmap so it will be recomputed on next access. */
  public void resetBitmapResized() {
    stateSaver = AdjustImageState.empty();
  }

  public Bitmap getAdjustedBitmap() {
    // Step 1: scale the source bitmap to the configured target size.
    Bitmap scaled = scaleSource(source, transformSettings);

    // Step 2: apply horizontal mirror if toggled to the already-small bitmap.
    Bitmap mirrored =
        transformSettings.isMirrored() ? BitmapTransformer.mirrorBitmap(scaled) : scaled;

    // Step 3: apply rotation.
    return BitmapTransformer.rotateBitmap(
        mirrored, DEGREES_PER_ROTATION_STEP * transformSettings.getCurrentRotation());
  }

  public void rotatePreviewImage() {
    transformSettings.rotate();
  }

  public void mirrorPreviewImage() {
    transformSettings.toggleMirror();
  }

  public void updateImageViewPreviewImage(ImageView imageView) {
    imageView.setImageBitmap(getBitmapSmall());
  }

  /**
   * Returns {@code true} when the compare-output file must be regenerated because the image, its
   * rotation, or its resize settings have changed since the last save, or because the file does not
   * exist yet.
   */
  public boolean requiresRecalculation(File compareFile) {
    return stateSaver.requiresRecalculation(compareFile, transformSettings);
  }

  /**
   * Records the current image state so that subsequent calls to {@link #requiresRecalculation} can
   * detect whether anything has changed.
   */
  public void markSaved() {
    stateSaver = AdjustImageState.of(transformSettings);
  }

  /**
   * Saves the current rotation, mirror state, and dirty-tracking snapshot into a Bundle so they
   * survive activity recreation (device rotation) and process death (killed in background). Pair
   * with {@link #restoreTransformState(Bundle)}.
   *
   * <p>Resize settings are managed by {@code UserSettings} and restored separately via {@code
   * ApplyUserSettings.apply}.
   *
   * <ul>
   *   <li>{@code rotation} — current 90°-step counter (0–3).
   *   <li>{@code mirrored} — whether the image is currently horizontally flipped.
   *   <li>{@code savedRotation / savedResizeOption / savedCustomHeight / savedCustomWidth /
   *       savedMirrored} — {@link AdjustImageState} snapshot used to decide whether compare-output
   *       files need re-encoding after restore.
   * </ul>
   */
  public Bundle saveTransformState() {
    Bundle b = new Bundle();
    b.putInt("rotation", transformSettings.getCurrentRotation());
    b.putBoolean("mirrored", transformSettings.isMirrored());
    b.putInt("savedRotation", stateSaver.savedRotation());
    b.putInt("savedResizeOption", stateSaver.savedResizeOption());
    b.putInt("savedCustomHeight", stateSaver.savedCustomHeight());
    b.putInt("savedCustomWidth", stateSaver.savedCustomWidth());
    b.putInt("savedMirrored", stateSaver.savedMirrored());
    return b;
  }

  /**
   * Restores rotation, mirror state, and dirty-tracking state after a bitmap has been re-loaded via
   * {@link #updateFromBitmap} following a process death (killed in background).
   *
   * <p>Must be called <em>after</em> {@link #updateFromBitmap} (which resets rotation and mirror to
   * their defaults) so the correct transforms are re-applied to both the {@link
   * BitmapTransformSettings} and the cached preview bitmap, and the {@link AdjustImageState} is
   * consistent with any previously encoded compare files still on disk.
   *
   * <p><b>Device rotation</b> does not need this method — {@link ImageSessionState} is a static
   * singleton and keeps the full transform state in memory across configuration changes.
   *
   * @param b Bundle previously returned by {@link #saveTransformState()}, or {@code null} (in which
   *     case this method is a no-op).
   */
  public void restoreTransformState(Bundle b) {
    if (b == null || source.bitmap() == null) return;

    int rotation = b.getInt("rotation", 0);

    // Re-apply each 90° step to keep the rotation counter in sync.
    // PreviewBitmap will recompute the preview from scaledBase when next requested.
    for (int i = 0; i < rotation; i++) {
      transformSettings.rotate();
    }

    // Re-apply mirroring flag if it was active before recreation.
    if (b.getBoolean("mirrored", false)) {
      transformSettings.toggleMirror();
    }

    // Restore the dirty-tracking snapshot so compare files are not needlessly re-encoded.
    stateSaver =
        new AdjustImageState(
            b.getInt("savedRotation", -1),
            b.getInt("savedResizeOption", -1),
            b.getInt("savedCustomHeight", -1),
            b.getInt("savedCustomWidth", -1),
            b.getInt("savedMirrored", -1));
  }

  /**
   * Scales {@code source} according to the resize option in {@code settings}. Returns the original
   * bitmap reference when no scaling is requested.
   */
  private static Bitmap scaleSource(ImageSource source, BitmapTransformSettings settings) {
    return switch (settings.getResizeOption()) {
      case ImageResizeOptions.RESIZE_OPTION_CUSTOM ->
          BitmapTransformer.resizeBitmap(
              source.bitmap(), settings.getCustomWidth(), settings.getCustomHeight());
      case ImageResizeOptions.RESIZE_OPTION_AUTOMATIC ->
          BitmapTransformer.createScaledBitmapToMaxLength(
              source.bitmap(), source.maxSideSize(), source.maxSideSize());
      default -> source.bitmap(); // RESIZE_OPTION_ORIGINAL — keep at full resolution.
    };
  }
}
