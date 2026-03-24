package com.vincentengelsoftware.androidimagecompare.ui.compare.differences;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.constants.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.constants.Status;
import com.vincentengelsoftware.androidimagecompare.data.preferences.KeyValueStorage;
import com.vincentengelsoftware.androidimagecompare.data.preferences.UserSettings;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityDifferencesBinding;
import com.vincentengelsoftware.androidimagecompare.ui.compare.shared.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.ui.compare.shared.SyncZoom;

/**
 * Displays two images side-by-side after automatically detecting and highlighting the most
 * significant visual differences between them.
 *
 * <p>All heavy work (I/O, BFS, drawing) is delegated to {@link ViewModel}, which survives
 * configuration changes. On rotation the ViewModel immediately re-delivers its cached {@link
 * ViewModel.ProcessingState#DONE} state via LiveData and the Activity applies the already-annotated
 * bitmaps — zero re-processing.
 *
 * <p>The Activity owns only UI concerns: view binding, spinner visibility, and controls wiring.
 */
public class DifferencesActivity extends AppCompatActivity {

  private ViewModel viewModel;
  private ActivityDifferencesBinding binding;

  // ── Lifecycle ──────────────────────────────────────────────────────────────

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    viewModel = new ViewModelProvider(this).get(ViewModel.class);

    FullScreenHelper.apply(
        getWindow(), getIntent().getBooleanExtra(IntentExtras.SHOW_NAVIGATION_BAR, true));

    binding = ActivityDifferencesBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    int maxZoom = IntentExtras.getMaxZoom(getIntent());
    float minZoom = IntentExtras.getMinZoom(getIntent());
    binding.differencesImageTop.initZoomLimits(maxZoom, minZoom);
    binding.differencesImageBottom.initZoomLimits(maxZoom, minZoom);

    if (!initProcessing()) {
      finish();
      return;
    }

    toggleExtensionsVisibility();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  // ── Initialisation ─────────────────────────────────────────────────────────

  /**
   * Reads the image URIs, wires the LiveData observer, and — on first launch only — starts the
   * processing pipeline in the ViewModel. On rotation the observer immediately fires with the
   * already-cached state; no re-processing occurs.
   *
   * @return {@code false} if either URI is absent; the caller should finish().
   */
  private boolean initProcessing() {
    String uriStringOne = getIntent().getStringExtra(IntentExtras.IMAGE_URI_ONE);
    String uriStringTwo = getIntent().getStringExtra(IntentExtras.IMAGE_URI_TWO);

    if (uriStringOne == null
        || uriStringOne.isEmpty()
        || uriStringTwo == null
        || uriStringTwo.isEmpty()) {
      return false;
    }

    binding.differencesImageNameTop.setText(
        getIntent().getStringExtra(IntentExtras.IMAGE_NAME_ONE));
    binding.differencesImageNameBottom.setText(
        getIntent().getStringExtra(IntentExtras.IMAGE_NAME_TWO));

    // Observe state — lifecycle-aware: old Activity's observer is removed automatically on
    // rotation before the new one is added, so there is never a double-delivery.
    viewModel
        .getProcessingState()
        .observe(
            this,
            state -> {
              switch (state) {
                case PROCESSING -> binding.differencesProgress.setVisibility(View.VISIBLE);
                case DONE -> {
                  binding.differencesProgress.setVisibility(View.GONE);
                  binding.differencesImageTop.setBitmapImage(viewModel.getAnnotatedOne());
                  binding.differencesImageBottom.setBitmapImage(viewModel.getAnnotatedTwo());
                  initImageViews();
                }
                case ERROR -> {
                  binding.differencesProgress.setVisibility(View.GONE);
                  Toast.makeText(this, R.string.error_message_general, Toast.LENGTH_SHORT).show();
                  finish();
                }
              }
            });

    // Start processing exactly once. isProcessingStarted() returns true on subsequent
    // calls (rotation), so this block is skipped and the observer above handles display.
    if (!viewModel.isProcessingStarted()) {
      UserSettings userSettings =
          UserSettings.getInstance(new KeyValueStorage(getApplicationContext()));

      // Initialise sync from Intent only on the very first launch.
      viewModel
          .getSync()
          .set(getIntent().getBooleanExtra(IntentExtras.SYNC_IMAGE_INTERACTIONS, true));

      viewModel.startProcessing(
          Uri.parse(uriStringOne),
          Uri.parse(uriStringTwo),
          userSettings.getDifferencesMaxCount(),
          ViewModel.resolveCircleColor(userSettings.getDifferencesCircleColor()));
    }

    return true;
  }

  /**
   * Wires synchronised zoom/pan between the two image views and binds the toggle button. Called
   * after the bitmaps are applied to the views.
   */
  private void initImageViews() {
    SyncZoom.setLinkedTargets(
        binding.differencesImageTop,
        binding.differencesImageBottom,
        viewModel.getSync(),
        getIntent().getIntExtra(IntentExtras.MIRRORING_TYPE, Status.NATURAL_MIRRORING));

    SyncZoom.setUpSyncZoomToggleButton(
        binding.differencesImageTop,
        binding.differencesImageBottom,
        binding.toggleButton,
        ContextCompat.getDrawable(this, R.drawable.ic_link),
        ContextCompat.getDrawable(this, R.drawable.ic_link_off),
        viewModel.getSync(),
        getIntent().getBooleanExtra(IntentExtras.RESET_IMAGE_ON_LINKING, true));
  }

  /** Shows or hides the extensions bar based on the Intent extra. */
  private void toggleExtensionsVisibility() {
    boolean showExtensions = getIntent().getBooleanExtra(IntentExtras.SHOW_EXTENSIONS, false);
    binding.differencesExtensions.setVisibility(showExtensions ? View.VISIBLE : View.GONE);
  }
}
