package com.vincentengelsoftware.androidimagecompare.ui.compare.overlayTap;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.constants.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.constants.Status;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityOverlayTapBinding;
import com.vincentengelsoftware.androidimagecompare.ui.compare.shared.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.ui.compare.shared.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.ui.compare.shared.TapHelper;

/**
 * Displays two images stacked on top of each other; tapping the front image swaps which image is on
 * top.
 *
 * <p>The Activity owns only UI concerns: view binding and controls wiring. The sync state is
 * delegated to {@link ViewModel}. Images are loaded directly from their content URIs on every
 * (re-)creation – no bitmap is retained in memory across configuration changes.
 */
public class OverlayTapActivity extends AppCompatActivity {

  private static final String KEY_SYNC_IMAGE_INTERACTIONS = "key_sync_image_interactions";

  /** Survives configuration changes; owns the sync flag. */
  private ViewModel viewModel;

  private ActivityOverlayTapBinding binding;

  // ── Lifecycle ──────────────────────────────────────────────────────────────

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    viewModel = new ViewModelProvider(this).get(ViewModel.class);

    // On first launch, read the sync state from the Intent;
    // on configuration changes, restore it from savedInstanceState.
    if (savedInstanceState != null) {
      viewModel.getSync().set(savedInstanceState.getBoolean(KEY_SYNC_IMAGE_INTERACTIONS, true));
    } else {
      viewModel
          .getSync()
          .set(getIntent().getBooleanExtra(IntentExtras.SYNC_IMAGE_INTERACTIONS, true));
    }

    FullScreenHelper.apply(
        getWindow(), getIntent().getBooleanExtra(IntentExtras.SHOW_NAVIGATION_BAR, true));

    binding = ActivityOverlayTapBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    int maxZoom = IntentExtras.getMaxZoom(getIntent());
    float minZoom = IntentExtras.getMinZoom(getIntent());
    binding.overlayTapImageViewOne.initZoomLimits(maxZoom, minZoom);
    binding.overlayTapImageViewTwo.initZoomLimits(maxZoom, minZoom);

    int tapHideMode =
        getIntent().getIntExtra(IntentExtras.TAP_HIDE_MODE, Status.TAP_HIDE_MODE_INVISIBLE);

    if (!initImages(tapHideMode)) {
      // URIs are missing or invalid; nothing to show.
      finish();
      return;
    }

    initControls(tapHideMode);
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(KEY_SYNC_IMAGE_INTERACTIONS, viewModel.getSync().get());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  // ── Initialisation ─────────────────────────────────────────────────────────

  /**
   * Reads the two image URIs from the Intent and applies them to the image views.
   *
   * @return {@code false} if either URI string is absent; the caller should finish().
   */
  private boolean initImages(int tapHideMode) {
    String uriStringOne = getIntent().getStringExtra(IntentExtras.IMAGE_URI_ONE);
    String uriStringTwo = getIntent().getStringExtra(IntentExtras.IMAGE_URI_TWO);

    if (uriStringOne == null
        || uriStringOne.isEmpty()
        || uriStringTwo == null
        || uriStringTwo.isEmpty()) {
      return false;
    }

    binding.overlayTapImageViewOne.setImageURI(Uri.parse(uriStringOne));
    binding.overlayTapImageViewTwo.setImageURI(Uri.parse(uriStringTwo));

    if (tapHideMode == Status.TAP_HIDE_MODE_INVISIBLE) {
      binding.overlayTapImageViewTwo.setVisibility(View.INVISIBLE);
    } else {
      binding.overlayTapImageViewOne.bringToFront();
    }

    return true;
  }

  private void initControls(int tapHideMode) {
    String nameOne = getIntent().getStringExtra(IntentExtras.IMAGE_NAME_ONE);
    String nameTwo = getIntent().getStringExtra(IntentExtras.IMAGE_NAME_TWO);

    binding.overlayTapImageName.setText(nameOne);

    TapHelper.setOnClickListener(
        binding.overlayTapImageViewOne,
        binding.overlayTapImageViewTwo,
        viewModel.getSync(),
        binding.overlayTapImageName,
        nameTwo,
        tapHideMode);

    TapHelper.setOnClickListener(
        binding.overlayTapImageViewTwo,
        binding.overlayTapImageViewOne,
        viewModel.getSync(),
        binding.overlayTapImageName,
        nameOne,
        tapHideMode);

    SyncZoom.setUpSyncZoomToggleButton(
        binding.overlayTapImageViewOne,
        binding.overlayTapImageViewTwo,
        binding.overlayTapButtonZoomSync,
        ContextCompat.getDrawable(this, R.drawable.ic_link),
        ContextCompat.getDrawable(this, R.drawable.ic_link_off),
        viewModel.getSync(),
        getIntent().getBooleanExtra(IntentExtras.RESET_IMAGE_ON_LINKING, true));

    binding.overlayTapExtensions.setVisibility(
        getIntent().getBooleanExtra(IntentExtras.SHOW_EXTENSIONS, false)
            ? View.VISIBLE
            : View.GONE);
  }
}
