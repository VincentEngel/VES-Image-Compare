package com.vincentengelsoftware.androidimagecompare.ui.compare.shared;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.vincentengelsoftware.androidimagecompare.constants.Status;
import com.vincentengelsoftware.androidimagecompare.ui.widget.VesImageInterface;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class for wiring tap-to-switch behaviour between two stacked image views.
 *
 * <p>Non-instantiable – all methods are static.
 */
public final class TapHelper {

  private TapHelper() {
    /* utility class */
  }

  /**
   * Registers a click listener on {@code imageViewListener} that brings {@code imageViewTarget} to
   * the front (or makes it visible) and optionally copies zoom/pan state when sync is active.
   *
   * @param imageViewListener the image that receives the tap
   * @param imageViewTarget the image to reveal after the tap
   * @param sync shared flag; when {@code true} zoom/pan state is copied
   * @param textViewImageName label updated to show the target image's name
   * @param targetImageName the display name of the target image
   * @param tapHideMode the tap hide mode, determining the visibility behaviour on tap
   */
  public static void setOnClickListener(
      VesImageInterface imageViewListener,
      VesImageInterface imageViewTarget,
      AtomicBoolean sync,
      TextView textViewImageName,
      String targetImageName,
      int tapHideMode) {
    imageViewListener.setOnClickListener(
        view -> {
          if (sync.get()) {
            imageViewTarget.applyScaleAndCenter(imageViewListener);
          }

          if (tapHideMode == Status.TAP_HIDE_MODE_INVISIBLE) {
            imageViewListener.setVisibility(View.INVISIBLE);
            imageViewTarget.setVisibility(View.VISIBLE);
          } else {
            ViewGroup parentView = imageViewListener.getParentViewGroup();
            parentView.removeView((View) imageViewTarget);
            parentView.addView((View) imageViewTarget);
          }

          textViewImageName.setText(targetImageName);
        });
  }
}
