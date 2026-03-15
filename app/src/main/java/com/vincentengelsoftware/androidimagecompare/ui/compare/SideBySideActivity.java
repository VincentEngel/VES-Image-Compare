package com.vincentengelsoftware.androidimagecompare.ui.compare;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.constants.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivitySideBySideBinding;
import com.vincentengelsoftware.androidimagecompare.ui.util.FullScreenHelper;

/**
 * Displays two images side-by-side with optional synchronised zoom/pan.
 *
 * <p>The Activity owns only UI concerns: view binding, controls wiring, and LiveData observation.
 * All bitmap loading and state management is delegated to {@link SideBySideViewModel}.
 */
public class SideBySideActivity extends AppCompatActivity {

  private static final String KEY_SYNC_IMAGE_INTERACTIONS = "key_sync_image_interactions";

  /** Survives configuration changes; owns bitmaps and the sync flag. */
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

    FullScreenHelper.setFullScreenFlags(getWindow());

    binding = ActivitySideBySideBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    initImageViews();
    toggleExtensionsVisibility();
    loadImages();
    observeViewModel();
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

  // ── ViewModel observation ──────────────────────────────────────────────────

  /**
   * Starts observing the ViewModel's LiveData streams. Must be called after the binding is ready so
   * that view updates are always on the main thread.
   */
  private void observeViewModel() {
    viewModel
        .getImages()
        .observe(
            this,
            imagePair -> {
              if (binding == null) return;
              binding.sideBySideImageTopLeft.setBitmapImage(imagePair.imageOne());
              binding.sideBySideImageNameTopLeft.setText(
                  getIntent().getStringExtra(IntentExtras.IMAGE_NAME_ONE));
              binding.sideBySideImageBottomRight.setBitmapImage(imagePair.imageTwo());
              binding.sideBySideImageNameBottomRight.setText(
                  getIntent().getStringExtra(IntentExtras.IMAGE_NAME_TWO));
            });

    viewModel
        .isLoadFailed()
        .observe(
            this,
            failed -> {
              if (Boolean.TRUE.equals(failed)) finish();
            });
  }

  // ── Initialisation helpers ─────────────────────────────────────────────────

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

  /**
   * Asks the ViewModel to decode both bitmaps in the background. This is a no-op after a
   * configuration change (bitmaps are already retained).
   */
  private void loadImages() {
    String uriOne = getIntent().getStringExtra(IntentExtras.IMAGE_URI_ONE);
    String uriTwo = getIntent().getStringExtra(IntentExtras.IMAGE_URI_TWO);
    if (uriOne == null || uriTwo == null) {
      finish();
      return;
    }
    viewModel.loadImages(getContentResolver(), uriOne, uriTwo);
  }
}
