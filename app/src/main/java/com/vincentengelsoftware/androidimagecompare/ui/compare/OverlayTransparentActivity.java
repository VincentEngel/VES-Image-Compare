package com.vincentengelsoftware.androidimagecompare.ui.compare;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.constants.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.constants.Settings;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityOverlayTransparentBinding;
import com.vincentengelsoftware.androidimagecompare.ui.animation.ControlsBarHost;
import com.vincentengelsoftware.androidimagecompare.ui.animation.ControlsBarManager;
import com.vincentengelsoftware.androidimagecompare.ui.util.FullScreenHelper;

/**
 * Displays two images stacked on top of each other and lets the user adjust the opacity of the
 * front image via a seekbar, or hide it entirely with a toggle button.
 *
 * <p>The Activity owns only UI concerns: view binding, seekbar/button wiring, and controls-bar
 * animation. The sync-zoom flag is delegated to {@link OverlayTransparentViewModel}. Images are
 * loaded directly from their content URIs on every (re-)creation – no bitmap is retained in memory
 * across configuration changes.
 */
public class OverlayTransparentActivity extends AppCompatActivity implements ControlsBarHost {

  private static final String KEY_SYNC_IMAGE_INTERACTIONS = "key_sync_image_interactions";

  /** Survives configuration changes; owns the sync flag. */
  private OverlayTransparentViewModel viewModel;

  /** Encapsulates the animated show/hide behaviour of the controls bar. */
  private ControlsBarManager controlsBarManager;

  private ActivityOverlayTransparentBinding binding;

  // ── Lifecycle ──────────────────────────────────────────────────────────────

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    viewModel = new ViewModelProvider(this).get(OverlayTransparentViewModel.class);

    // On first launch read the sync state from the Intent;
    // on process-death restoration read it from savedInstanceState.
    // (ViewModel already retains it across ordinary configuration changes.)
    if (savedInstanceState != null) {
      viewModel.getSync().set(savedInstanceState.getBoolean(KEY_SYNC_IMAGE_INTERACTIONS, true));
    } else {
      viewModel
          .getSync()
          .set(getIntent().getBooleanExtra(IntentExtras.SYNC_IMAGE_INTERACTIONS, true));
    }

    FullScreenHelper.apply(getWindow(), Settings.SHOW_NAVIGATION_BAR);

    binding = ActivityOverlayTransparentBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    controlsBarManager =
        new ControlsBarManager(binding.overlayTransparentExtensions, getResources());

    if (!initImages()) {
      // URIs are invalid or images could not be decoded; nothing to show.
      finish();
      return;
    }

    initControls();
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(KEY_SYNC_IMAGE_INTERACTIONS, viewModel.getSync().get());
  }

  @Override
  protected void onResume() {
    super.onResume();
    // Always snap the controls bar into view on resume so that it is
    // visible after a configuration change (e.g. rotation), even when
    // Android's view-state restoration had set the bar to INVISIBLE.
    showControlsBarInstant();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    controlsBarManager.destroy();
    binding = null;
  }

  // ── ControlsBarHost ────────────────────────────────────────────────────────

  @Override
  public void showControlsBar() {
    controlsBarManager.showAnimated();
  }

  @Override
  public void showControlsBarInstant() {
    controlsBarManager.showInstant();
  }

  @Override
  public void scheduleControlsBarHide() {
    controlsBarManager.scheduleHide();
  }

  // ── Initialisation ─────────────────────────────────────────────────────────

  /**
   * Reads the two image URIs from the Intent and applies them to the image views.
   *
   * @return {@code false} if either URI string is absent; the caller should finish().
   */
  private boolean initImages() {
    String uriStringOne = getIntent().getStringExtra(IntentExtras.IMAGE_URI_ONE);
    String uriStringTwo = getIntent().getStringExtra(IntentExtras.IMAGE_URI_TWO);

    if (uriStringOne == null || uriStringOne.isEmpty()
        || uriStringTwo == null || uriStringTwo.isEmpty()) {
      return false;
    }

    binding.overlayTransparentImageViewBase.addFadeListener(this);
    binding.overlayTransparentImageViewBase.setImageURI(Uri.parse(uriStringOne));

    binding.overlayTransparentImageViewTransparent.addFadeListener(this);
    binding.overlayTransparentImageViewTransparent.setImageURI(Uri.parse(uriStringTwo));
    binding.overlayTransparentImageViewTransparent.bringToFront();

    return true;
  }

  private void initControls() {
    TransparentHelper.makeTargetTransparent(
        binding.overlaySlideSeekBar,
        binding.overlayTransparentImageViewTransparent,
        binding.overlayTransparentButtonHideFrontImage,
        this);

    binding.overlaySlideSeekBar.setProgress(50);

    binding.overlayTransparentButtonHideFrontImage.setOnClickListener(
        view -> {
          showControlsBarInstant();
          if (binding.overlayTransparentImageViewTransparent.getVisibility() == View.VISIBLE) {
            binding.overlayTransparentButtonHideFrontImage.setImageResource(
                R.drawable.ic_visibility_off);
            binding.overlayTransparentImageViewTransparent.setVisibility(View.GONE);
          } else if (binding.overlaySlideSeekBar.getProgress()
              <= TransparentHelper.HIDE_THRESHOLD) {
            binding.overlaySlideSeekBar.setProgress(TransparentHelper.HIDE_THRESHOLD + 1);
          } else {
            binding.overlayTransparentButtonHideFrontImage.setImageResource(
                R.drawable.ic_visibility);
            binding.overlayTransparentImageViewTransparent.setVisibility(View.VISIBLE);
          }
          scheduleControlsBarHide();
        });

    SyncZoom.setLinkedTargets(
        binding.overlayTransparentImageViewBase,
        binding.overlayTransparentImageViewTransparent,
        viewModel.getSync());
    SyncZoom.setUpSyncZoomToggleButton(
        binding.overlayTransparentImageViewBase,
        binding.overlayTransparentImageViewTransparent,
        binding.overlayTransparentButtonZoomSync,
        ContextCompat.getDrawable(this, R.drawable.ic_link),
        ContextCompat.getDrawable(this, R.drawable.ic_link_off),
        viewModel.getSync());

    if (getIntent().getBooleanExtra(IntentExtras.HAS_HARDWARE_KEY, false)) {
      ViewGroup.MarginLayoutParams layoutParams =
          (ViewGroup.MarginLayoutParams) binding.overlayTransparentExtensions.getLayoutParams();
      layoutParams.setMargins(0, 0, 0, 0);
      binding.overlayTransparentExtensions.setLayoutParams(layoutParams);
    }
  }
}
