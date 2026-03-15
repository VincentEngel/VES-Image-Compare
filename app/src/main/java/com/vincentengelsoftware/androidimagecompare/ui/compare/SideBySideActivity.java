package com.vincentengelsoftware.androidimagecompare.ui.compare;

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
import com.vincentengelsoftware.androidimagecompare.constants.Settings;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivitySideBySideBinding;
import com.vincentengelsoftware.androidimagecompare.ui.util.FullScreenHelper;

/**
 * Displays two images side-by-side with optional synchronised zoom/pan.
 *
 * <p>The Activity owns only UI concerns: view binding and controls wiring. The sync state is
 * delegated to {@link SideBySideViewModel}. Images are loaded directly from their content URIs on
 * every (re-)creation – no bitmap is retained in memory across configuration changes.
 */
public class SideBySideActivity extends AppCompatActivity {

  private static final String KEY_SYNC_IMAGE_INTERACTIONS = "key_sync_image_interactions";

  /** Survives configuration changes; owns the sync flag. */
  private SideBySideViewModel viewModel;

  private ActivitySideBySideBinding binding;

  // ── Lifecycle ──────────────────────────────────────────────────────────────

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    viewModel = new ViewModelProvider(this).get(SideBySideViewModel.class);

    // On first launch, read the sync state from the Intent;
    // on configuration changes, restore it from savedInstanceState.
    if (savedInstanceState != null) {
      viewModel.getSync().set(savedInstanceState.getBoolean(KEY_SYNC_IMAGE_INTERACTIONS, true));
    } else {
      viewModel
          .getSync()
          .set(getIntent().getBooleanExtra(IntentExtras.SYNC_IMAGE_INTERACTIONS, true));
    }

    FullScreenHelper.apply(getWindow(), Settings.SHOW_NAVIGATION_BAR);

    binding = ActivitySideBySideBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    if (!initImages()) {
      // URIs are missing or invalid; nothing to show.
      finish();
      return;
    }

    initImageViews();
    toggleExtensionsVisibility();
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

  // ── Initialisation helpers ─────────────────────────────────────────────────

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

    binding.sideBySideImageTopLeft.setImageURI(Uri.parse(uriStringOne));
    binding.sideBySideImageNameTopLeft.setText(getIntent().getStringExtra(IntentExtras.IMAGE_NAME_ONE));

    binding.sideBySideImageBottomRight.setImageURI(Uri.parse(uriStringTwo));
    binding.sideBySideImageNameBottomRight.setText(getIntent().getStringExtra(IntentExtras.IMAGE_NAME_TWO));

    return true;
  }

  /**
   * Wires synchronised zoom/pan between the two image views and binds the toggle button that
   * enables/disables the link.
   */
  private void initImageViews() {
    SyncZoom.setLinkedTargets(
        binding.sideBySideImageTopLeft, binding.sideBySideImageBottomRight, viewModel.getSync());

    SyncZoom.setUpSyncZoomToggleButton(
        binding.sideBySideImageTopLeft,
        binding.sideBySideImageBottomRight,
        binding.toggleButton,
        ContextCompat.getDrawable(this, R.drawable.ic_link),
        ContextCompat.getDrawable(this, R.drawable.ic_link_off),
        viewModel.getSync());
  }

  /** Shows or hides the extensions bar based on the Intent extra. */
  private void toggleExtensionsVisibility() {
    boolean showExtensions = getIntent().getBooleanExtra(IntentExtras.SHOW_EXTENSIONS, false);
    binding.sideBySideExtensions.setVisibility(showExtensions ? View.VISIBLE : View.GONE);
  }
}
