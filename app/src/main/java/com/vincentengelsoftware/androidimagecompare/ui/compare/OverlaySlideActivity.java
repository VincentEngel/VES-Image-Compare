package com.vincentengelsoftware.androidimagecompare.ui.compare;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.constants.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityOverlaySlideBinding;
import com.vincentengelsoftware.androidimagecompare.ui.animation.ControlsBarHost;
import com.vincentengelsoftware.androidimagecompare.ui.animation.ControlsBarManager;
import com.vincentengelsoftware.androidimagecompare.ui.util.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.ui.widget.ImageScaleCenter;
import com.vincentengelsoftware.androidimagecompare.util.BitmapExtractor;

/**
 * Displays two images stacked on top of each other and lets the user slide a
 * vertical divider left or right to reveal either image.
 *
 * <p>The Activity owns only UI concerns: view binding, seekbar feedback, controls
 * bar animation, and hide/show button state. All bitmap processing and persistent
 * state are delegated to {@link OverlaySlideViewModel}.</p>
 */
public class OverlaySlideActivity extends AppCompatActivity implements ControlsBarHost {

    private static final String KEY_SYNC         = "key_sync_image_interactions";
    private static final String KEY_LEFT_TO_RIGHT = "key_left_to_right";

    /** Survives configuration changes; owns bitmaps, crop logic, and direction state. */
    private OverlaySlideViewModel viewModel;

    /** Encapsulates the animated show/hide behaviour of the controls bar. */
    private ControlsBarManager controlsBarManager;

    private ActivityOverlaySlideBinding binding;

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(OverlaySlideViewModel.class);

        // Restore sync and direction from savedInstanceState on rotation,
        // or seed from the launching Intent on first launch.
        if (savedInstanceState != null) {
            viewModel.getSync().set(
                    savedInstanceState.getBoolean(KEY_SYNC, true));
            viewModel.getLeftToRight().set(
                    savedInstanceState.getBoolean(KEY_LEFT_TO_RIGHT, true));
        } else {
            viewModel.getSync().set(
                    getIntent().getBooleanExtra(IntentExtras.SYNC_IMAGE_INTERACTIONS, true));
        }

        FullScreenHelper.setFullScreenFlags(getWindow());

        binding = ActivityOverlaySlideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        controlsBarManager = new ControlsBarManager(binding.overlaySlideExtensions, getResources());

        if (!initImages()) {
            finish();
            return;
        }

        // Observer must be registered before initControls() so the LiveData
        // delivers the first crop result as soon as it is computed.
        observeViewModel();
        initControls();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SYNC, viewModel.getSync().get());
        outState.putBoolean(KEY_LEFT_TO_RIGHT, viewModel.getLeftToRight().get());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure the controls bar is visible after any configuration change,
        // even if view-state restoration had left it INVISIBLE.
        showControlsBarInstant();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.cancelPendingCrop();
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

    // ── ViewModel observation ──────────────────────────────────────────────────

    /**
     * Observes the ViewModel's crop output. Each new bitmap is applied to the
     * front image view while preserving the current zoom/pan state.
     */
    private void observeViewModel() {
        viewModel.getFrontBitmap().observe(this, bitmap -> {
            if (binding == null) return;

            // Preserve zoom state across bitmap swaps.
            ImageScaleCenter center = binding.overlaySlideImageViewFront.getImageScaleCenter();
            binding.overlaySlideImageViewFront.setBitmapImage(bitmap);
            binding.overlaySlideImageViewFront.setImageScaleCenter(center);

            binding.overlayTransparentButtonHideFrontImage.setImageResource(R.drawable.ic_visibility);
            binding.overlaySlideImageViewFront.setVisibility(View.VISIBLE);

            // Reset the auto-hide timer after each rendered frame.
            controlsBarManager.scheduleHide();
        });
    }

    // ── Initialisation ─────────────────────────────────────────────────────────

    /**
     * Loads (or reuses from ViewModel) the two images and wires up zoom synchronisation.
     *
     * @return {@code false} if the images could not be decoded; the caller should finish()
     */
    private boolean initImages() {
        binding.overlaySlideImageViewBase.addFadeListener(this);
        binding.overlaySlideImageViewFront.addFadeListener(this);

        if (viewModel.areBitmapsLoaded()) {
            // Bitmaps survived a configuration change — re-bind to the recreated views.
            binding.overlaySlideImageViewBase.setBitmapImage(viewModel.bitmapBase);
        } else {
            String uriOne = getIntent().getStringExtra(IntentExtras.IMAGE_URI_ONE);
            String uriTwo = getIntent().getStringExtra(IntentExtras.IMAGE_URI_TWO);

            Bitmap base   = BitmapExtractor.fromUriString(getContentResolver(), uriOne);
            Bitmap source = BitmapExtractor.fromUriString(getContentResolver(), uriTwo);

            if (base == null || source == null) return false;

            viewModel.initBitmaps(base, source);

            try {
                binding.overlaySlideImageViewBase.setBitmapImage(base);
            } catch (Exception e) {
                return false;
            }
        }

        SyncZoom.setLinkedTargets(
                binding.overlaySlideImageViewFront,
                binding.overlaySlideImageViewBase,
                viewModel.getSync()
        );

        return true;
    }

    private void initControls() {
        updateSlideDirectionIcon();

        // Register the listener BEFORE setting the initial progress so that
        // setProgress(50) triggers onProgressChanged and fires the first crop.
        binding.overlaySlideSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                showControlsBarInstant();
                onSeekBarProgressChanged(progress);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar)  {}
        });

        // Trigger the initial crop at the midpoint.
        binding.overlaySlideSeekBar.setProgress(50);

        binding.overlayTransparentButtonHideFrontImage.setOnClickListener(view -> {
            showControlsBarInstant();
            if (binding.overlaySlideImageViewFront.getVisibility() == View.VISIBLE) {
                binding.overlayTransparentButtonHideFrontImage
                        .setImageResource(R.drawable.ic_visibility_off);
                binding.overlaySlideImageViewFront.setVisibility(View.GONE);
            } else if (viewModel.getLeftToRight().get()
                    && binding.overlaySlideSeekBar.getProgress() <= 1) {
                // Nudge seekbar to re-trigger the crop and restore the image.
                binding.overlaySlideSeekBar.setProgress(2);
            } else if (!viewModel.getLeftToRight().get()
                    && binding.overlaySlideSeekBar.getProgress() >= 99) {
                binding.overlaySlideSeekBar.setProgress(98);
            } else {
                binding.overlayTransparentButtonHideFrontImage
                        .setImageResource(R.drawable.ic_visibility);
                binding.overlaySlideImageViewFront.setVisibility(View.VISIBLE);
            }
            scheduleControlsBarHide();
        });

        SlideHelper.setSwapSlideDirectionOnClick(
                binding.overlaySlideButtonSwapSeekbar,
                binding.overlaySlideSeekBar,
                viewModel.getLeftToRight()
        );

        if (getIntent().getBooleanExtra(IntentExtras.HAS_HARDWARE_KEY, false)) {
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) binding.overlaySlideExtensions.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, 0);
            binding.overlaySlideExtensions.setLayoutParams(layoutParams);
        }
    }

    // ── Seekbar logic ──────────────────────────────────────────────────────────

    /**
     * Hides the front image at the edges of the seekbar range, or submits
     * a crop job to the ViewModel for any value in between.
     */
    private void onSeekBarProgressChanged(int progress) {
        boolean ltr = viewModel.getLeftToRight().get();

        if (ltr && progress <= 1) {
            binding.overlayTransparentButtonHideFrontImage
                    .setImageResource(R.drawable.ic_visibility_off);
            binding.overlaySlideImageViewFront.setVisibility(View.GONE);
            return;
        }

        if (!ltr && progress >= 99) {
            binding.overlayTransparentButtonHideFrontImage
                    .setImageResource(R.drawable.ic_visibility_off);
            binding.overlaySlideImageViewFront.setVisibility(View.GONE);
            return;
        }

        viewModel.submitCrop(progress);
    }

    private void updateSlideDirectionIcon() {
        binding.overlaySlideButtonSwapSeekbar.setImageResource(
                viewModel.getLeftToRight().get()
                        ? R.drawable.ic_slide_ltr
                        : R.drawable.ic_slide_rtl);
    }
}
