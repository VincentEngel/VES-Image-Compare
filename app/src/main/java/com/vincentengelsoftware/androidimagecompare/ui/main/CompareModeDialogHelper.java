package com.vincentengelsoftware.androidimagecompare.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.widget.Button;
import com.vincentengelsoftware.androidimagecompare.databinding.DialogCompareModeSelectionBinding;
import com.vincentengelsoftware.androidimagecompare.ui.compare.OverlayCutActivity;
import com.vincentengelsoftware.androidimagecompare.ui.compare.OverlaySlideActivity;
import com.vincentengelsoftware.androidimagecompare.ui.compare.OverlayTapActivity;
import com.vincentengelsoftware.androidimagecompare.ui.compare.OverlayTouchActivity;
import com.vincentengelsoftware.androidimagecompare.ui.compare.OverlayTransparentActivity;
import com.vincentengelsoftware.androidimagecompare.ui.compare.SideBySideActivity;

/**
 * Builds and shows the compare-mode selection dialog.
 *
 * <p>Use the single static entry point {@link #show(Activity, OnCompareModeSelectedListener)}.
 */
public class CompareModeDialogHelper {

  /** Callback invoked when the user selects a compare mode from the dialog. */
  public interface OnCompareModeSelectedListener {
    /**
     * Called with the {@link android.app.Activity} class of the chosen compare mode.
     *
     * @param targetActivity the compare-mode activity class selected by the user
     */
    void onSelected(Class<?> targetActivity);
  }

  /**
   * Inflates, configures, and shows the compare-mode selection dialog.
   *
   * @param activity the host activity
   * @param listener called once the user taps a mode button; the dialog is dismissed first
   */
  public static void show(Activity activity, OnCompareModeSelectedListener listener) {
    DialogCompareModeSelectionBinding dialogBinding =
        DialogCompareModeSelectionBinding.inflate(activity.getLayoutInflater());

    Dialog dialog = new Dialog(activity);
    dialog.setContentView(dialogBinding.getRoot());

    addButton(
        dialogBinding.selectCompareModeDialogBtnSideBySide,
        SideBySideActivity.class,
        dialog,
        listener);

    addButton(
        dialogBinding.selectCompareModeDialogBtnOverlaySlide,
        OverlaySlideActivity.class,
        dialog,
        listener);

    addButton(
        dialogBinding.selectCompareModeDialogBtnTransparent,
        OverlayTransparentActivity.class,
        dialog,
        listener);

    addButton(
        dialogBinding.selectCompareModeDialogBtnOverlayTap,
        OverlayTapActivity.class,
        dialog,
        listener);

    addButton(
        dialogBinding.selectCompareModeDialogBtnOverlayCut,
        OverlayCutActivity.class,
        dialog,
        listener);

    addButton(
        dialogBinding.selectCompareModeDialogBtnOverlayTouch,
        OverlayTouchActivity.class,
        dialog,
        listener);

    dialog.show();
  }

  private static void addButton(
      Button button,
      Class<?> targetActivity,
      Dialog dialog,
      OnCompareModeSelectedListener listener) {
    button.setOnClickListener(
        view -> {
          dialog.dismiss();
          listener.onSelected(targetActivity);
        });
  }
}
