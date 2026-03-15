package com.vincentengelsoftware.androidimagecompare.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewConfiguration;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.constants.Settings;
import com.vincentengelsoftware.androidimagecompare.constants.Status;
import com.vincentengelsoftware.androidimagecompare.data.cache.CacheManager;
import com.vincentengelsoftware.androidimagecompare.data.preferences.ApplyUserSettings;
import com.vincentengelsoftware.androidimagecompare.data.preferences.KeyValueStorage;
import com.vincentengelsoftware.androidimagecompare.data.preferences.UserSettings;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityMainBinding;
import com.vincentengelsoftware.androidimagecompare.domain.model.ImageSessionState;
import com.vincentengelsoftware.androidimagecompare.ui.compare.CompareModeNames;
import com.vincentengelsoftware.androidimagecompare.ui.compare.OverlayCutActivity;
import com.vincentengelsoftware.androidimagecompare.ui.compare.OverlaySlideActivity;
import com.vincentengelsoftware.androidimagecompare.ui.compare.OverlayTapActivity;
import com.vincentengelsoftware.androidimagecompare.ui.compare.OverlayTransparentActivity;
import com.vincentengelsoftware.androidimagecompare.ui.compare.SideBySideActivity;
import com.vincentengelsoftware.androidimagecompare.ui.settings.ConfigActivity;
import com.vincentengelsoftware.androidimagecompare.ui.util.AskForReview;
import com.vincentengelsoftware.androidimagecompare.util.DimensionsInitializer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Entry-point activity for the image comparison app.
 *
 * <p>Responsibilities are intentionally kept to a minimum:
 *
 * <ul>
 *   <li>Android lifecycle hooks ({@code onCreate}, {@code onResume}, …).
 *   <li>View-binding setup and toolbar / button wiring ({@link #setUpActions}).
 *   <li>Delegation to focused single-responsibility helpers for every non-trivial task.
 * </ul>
 *
 * <p>Non-Activity concerns are handled by:
 *
 * <ul>
 *   <li>{@link ImageSessionState} — left/right URI + ImageInfoHolder singleton state
 *   <li>{@link DimensionsInitializer} — one-time screen-size calculation
 *   <li>{@link CacheManager} — cache file names + cleanup on destroy
 *   <li>{@link ImageRestoreHelper} — bitmap reload + image-view update after recreation
 *   <li>{@link IntentImageHandler} — incoming share-intent parsing
 *   <li>{@link ResizeImageDialogHelper} — resize options dialog
 *   <li>{@link CompareModeDialogHelper} — compare-mode selection dialog
 *   <li>{@link CompareActivityLauncher} — parallel image encoding + activity launch
 *   <li>{@link ImageSlotPickerHelper} — camera / gallery / share per image slot
 * </ul>
 */
public class MainActivity extends AppCompatActivity {

  // Shared flag: prevents duplicate launches while one is already in progress.
  private final AtomicBoolean openingActivity = new AtomicBoolean(false);

  // Core dependencies
  private ImageSessionState sessionState;
  private UserSettings userSettings;

  // Delegates created in onCreate / setUpActions
  private CompareActivityLauncher compareLauncher;
  private ImageSlotPickerHelper leftPicker;
  private ImageSlotPickerHelper rightPicker;

  protected ActivityMainBinding binding;

  // ── lifecycle ─────────────────────────────────────────────────────────────

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    KeyValueStorage keyValueStorage = new KeyValueStorage(getApplicationContext());
    this.userSettings = UserSettings.getInstance(keyValueStorage);
    this.sessionState = ImageSessionState.getInstance();

    Settings.init(userSettings);
    ApplyUserSettings.apply(
        this.userSettings,
        sessionState.getFirstImageInfoHolder(),
        sessionState.getSecondImageInfoHolder());

    super.onCreate(savedInstanceState);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    Status.HAS_HARDWARE_KEY = ViewConfiguration.get(this).hasPermanentMenuKey();
    DimensionsInitializer.init(this);

    compareLauncher = new CompareActivityLauncher(openingActivity);

    if (savedInstanceState != null) {
      restoreUrisFromBundle(savedInstanceState);

      boolean firstWasNull = sessionState.getFirstImageInfoHolder().getBitmap() == null;
      boolean secondWasNull = sessionState.getSecondImageInfoHolder().getBitmap() == null;

      ImageRestoreHelper.restoreImages(this, sessionState, binding);

      // Re-apply saved rotation / dirty-tracking state for holders that had to reload
      // their bitmap from disk (process was killed while the app was in the background).
      if (firstWasNull && sessionState.getFirstImageInfoHolder().getBitmap() != null) {
        sessionState
            .getFirstImageInfoHolder()
            .restoreTransformState(savedInstanceState.getBundle("leftImageTransformState"));
        sessionState.getFirstImageInfoHolder().updateImageViewPreviewImage(binding.homeImageLeft);
      }
      if (secondWasNull && sessionState.getSecondImageInfoHolder().getBitmap() != null) {
        sessionState
            .getSecondImageInfoHolder()
            .restoreTransformState(savedInstanceState.getBundle("rightImageTransformState"));
        sessionState.getSecondImageInfoHolder().updateImageViewPreviewImage(binding.homeImageRight);
      }
    }

    ImageRestoreHelper.restoreImageViews(sessionState, binding);

    // setUpActions must be called after super.onCreate so that registerForActivityResult
    // inside ImageSlotPickerHelper is still within the allowed window.
    setUpActions();

    if (Status.handleIntentOnCreate) {
      Status.handleIntentOnCreate = false;
      IntentImageHandler.handleIntent(getIntent(), this, sessionState, binding);
    }

    AskForReview.askForReviewWhenNecessary(getApplicationContext(), keyValueStorage);
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);

    if (sessionState.getLeftImageUri() != null) {
      outState.putString(ImageSessionState.LEFT_URI_KEY, sessionState.getLeftImageUri().toString());
    }
    if (sessionState.getRightImageUri() != null) {
      outState.putString(
          ImageSessionState.RIGHT_URI_KEY, sessionState.getRightImageUri().toString());
    }
    if (sessionState.getFirstImageInfoHolder().getBitmap() != null) {
      outState.putBundle(
          "leftImageTransformState", sessionState.getFirstImageInfoHolder().saveTransformState());
    }
    if (sessionState.getSecondImageInfoHolder().getBitmap() != null) {
      outState.putBundle(
          "rightImageTransformState", sessionState.getSecondImageInfoHolder().saveTransformState());
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    compareLauncher.shutdown();
    // Skip cache cleanup when the activity is being recreated due to a configuration change
    // (e.g. screen rotation). At that point the camera may still be open and writing to
    // camera_capture_temp.jpg; deleting it would cause onCameraResult to fail after rotation.
    if (!isChangingConfigurations()) {
      CacheManager.cleanup(getCacheDir(), sessionState);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    Settings.init(userSettings);

    binding.mainButtonLastCompare.setText(
        CompareModeNames.getUserCompareModeNameFromInternalName(
            getBaseContext(), this.userSettings.getLastCompareMode()));

    binding.homeButtonExtensions.setImageDrawable(
        ContextCompat.getDrawable(
            getBaseContext(),
            this.userSettings.isShowExtensions()
                ? R.drawable.ic_extension_on
                : R.drawable.ic_extension_off));

    binding.homeButtonLinkZoom.setImageDrawable(
        ContextCompat.getDrawable(
            getBaseContext(),
            this.userSettings.isSyncImageInteractions()
                ? R.drawable.ic_link
                : R.drawable.ic_link_off));
  }

  @Override
  public void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    IntentImageHandler.handleIntent(intent, this, sessionState, binding);
  }

  // ── action wiring ─────────────────────────────────────────────────────────

  private void setUpActions() {
    // Resize buttons
    binding.mainBtnResizeImageLeft.setOnClickListener(
        view ->
            ResizeImageDialogHelper.show(
                this,
                sessionState.getFirstImageInfoHolder(),
                this.userSettings.getLeftImageResizeSettings()));

    binding.mainBtnResizeImageRight.setOnClickListener(
        view ->
            ResizeImageDialogHelper.show(
                this,
                sessionState.getSecondImageInfoHolder(),
                this.userSettings.getRightImageResizeSettings()));

    // Last-used compare mode button
    binding.mainButtonLastCompare.setText(
        CompareModeNames.getUserCompareModeNameFromInternalName(
            getBaseContext(), this.userSettings.getLastCompareMode()));
    binding.mainButtonLastCompare.setOnClickListener(
        view -> {
          String mode =
              CompareModeNames.getInternalCompareModeNameFromUserCompareModeName(
                  getBaseContext(), binding.mainButtonLastCompare.getText().toString());
          switch (mode) {
            case CompareModeNames.SIDE_BY_SIDE -> openCompareActivity(SideBySideActivity.class);
            case CompareModeNames.OVERLAY_SLIDE -> openCompareActivity(OverlaySlideActivity.class);
            case CompareModeNames.OVERLAY_TAP -> openCompareActivity(OverlayTapActivity.class);
            case CompareModeNames.OVERLAY_TRANSPARENT ->
                openCompareActivity(OverlayTransparentActivity.class);
            case CompareModeNames.OVERLAY_CUT -> openCompareActivity(OverlayCutActivity.class);
            default -> openCompareDialog();
          }
        });

    // Compare-mode picker dialog button
    binding.mainButtonCompare.setOnClickListener(view -> openCompareDialog());

    // Settings / info screen
    binding.homeButtonInfo.setOnClickListener(
        view -> {
          if (openingActivity.get()) return;
          startActivity(new Intent(getApplicationContext(), ConfigActivity.class));
        });

    // Swap images
    MainHelper.addSwapImageLogic(
        binding.homeButtonSwapImages,
        binding.homeImageLeft,
        binding.homeImageRight,
        binding.mainTextViewNameImageLeft,
        binding.mainTextViewNameImageRight,
        openingActivity,
        ImageSessionState.getInstance());
    binding.homeButtonSwapImages.setOnLongClickListener(
        view -> {
          Toast.makeText(this, R.string.swap_images, Toast.LENGTH_SHORT).show();
          return true;
        });

    // Show-extensions toggle
    binding.homeButtonExtensions.setOnClickListener(
        view -> {
          this.userSettings.setShowExtensions(!this.userSettings.isShowExtensions());
          binding.homeButtonExtensions.setImageDrawable(
              ContextCompat.getDrawable(
                  getBaseContext(),
                  this.userSettings.isShowExtensions()
                      ? R.drawable.ic_extension_on
                      : R.drawable.ic_extension_off));
        });
    binding.homeButtonExtensions.setOnLongClickListener(
        view -> {
          Toast.makeText(this, R.string.show_extensions_in_compare_modes, Toast.LENGTH_SHORT)
              .show();
          return true;
        });

    // Linked-zoom toggle
    binding.homeButtonLinkZoom.setOnClickListener(
        view -> {
          this.userSettings.setSyncImageInteractions(!this.userSettings.isSyncImageInteractions());
          binding.homeButtonLinkZoom.setImageDrawable(
              ContextCompat.getDrawable(
                  getBaseContext(),
                  this.userSettings.isSyncImageInteractions()
                      ? R.drawable.ic_link
                      : R.drawable.ic_link_off));
        });
    binding.homeButtonLinkZoom.setOnLongClickListener(
        view -> {
          Toast.makeText(this, R.string.globally_enable_or_disable_linked_zoom, Toast.LENGTH_SHORT)
              .show();
          return true;
        });

    // Rotate buttons
    MainHelper.addRotateImageLogic(
        binding.homeButtonRotateImageLeft,
        sessionState.getFirstImageInfoHolder(),
        binding.homeImageLeft,
        openingActivity);
    binding.homeButtonRotateImageLeft.setOnLongClickListener(
        view -> {
          Toast.makeText(this, R.string.rotate_image_left, Toast.LENGTH_SHORT).show();
          return true;
        });

    MainHelper.addRotateImageLogic(
        binding.homeButtonRotateImageRight,
        sessionState.getSecondImageInfoHolder(),
        binding.homeImageRight,
        openingActivity);
    binding.homeButtonRotateImageRight.setOnLongClickListener(
        view -> {
          Toast.makeText(this, R.string.rotate_image_right, Toast.LENGTH_SHORT).show();
          return true;
        });

    // Mirror buttons
    MainHelper.addMirrorImageLogic(
        binding.homeButtonMirrorImageLeft,
        sessionState.getFirstImageInfoHolder(),
        binding.homeImageLeft,
        openingActivity);
    binding.homeButtonMirrorImageLeft.setOnLongClickListener(
        view -> {
          Toast.makeText(this, R.string.mirror_image_left, Toast.LENGTH_SHORT).show();
          return true;
        });

    MainHelper.addMirrorImageLogic(
        binding.homeButtonMirrorImageRight,
        sessionState.getSecondImageInfoHolder(),
        binding.homeImageRight,
        openingActivity);
    binding.homeButtonMirrorImageRight.setOnLongClickListener(
        view -> {
          Toast.makeText(this, R.string.mirror_image_right, Toast.LENGTH_SHORT).show();
          return true;
        });

    // Image-slot pickers: camera / gallery / share per slot.
    // Stored as fields to ensure the instances (and their registered launcher callbacks)
    // are not garbage-collected while the activity is alive.
    leftPicker = ImageSlotPickerHelper.create(this, "left", sessionState, binding, openingActivity);
    rightPicker =
        ImageSlotPickerHelper.create(this, "right", sessionState, binding, openingActivity);

    binding.homeImageLeft.setOnClickListener(leftPicker.buildClickListener());
    binding.homeImageRight.setOnClickListener(rightPicker.buildClickListener());
  }

  // ── private helpers ───────────────────────────────────────────────────────

  private void openCompareDialog() {
    CompareModeDialogHelper.show(this, this::openCompareActivity);
  }

  private void openCompareActivity(Class<?> targetActivity) {
    compareLauncher.launch(this, targetActivity, userSettings, sessionState, binding);
  }

  private void restoreUrisFromBundle(Bundle savedInstanceState) {
    String savedLeft = savedInstanceState.getString(ImageSessionState.LEFT_URI_KEY);
    String savedRight = savedInstanceState.getString(ImageSessionState.RIGHT_URI_KEY);
    if (savedLeft != null) sessionState.setLeftImageUri(Uri.parse(savedLeft));
    if (savedRight != null) sessionState.setRightImageUri(Uri.parse(savedRight));
  }
}
