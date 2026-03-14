package com.vincentengelsoftware.androidimagecompare.ui.compare;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.constants.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityOverlayCutBinding;
import com.vincentengelsoftware.androidimagecompare.domain.model.CropParams;
import com.vincentengelsoftware.androidimagecompare.ui.util.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.util.BitmapExtractor;
import com.vincentengelsoftware.androidimagecompare.util.Calculator;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Displays two images stacked on top of each other and lets the user draw a
 * diagonal cut-line by dragging any two of the four edge seekbars.
 *
 * <p>The Activity owns only UI concerns: view binding, seekbar colour
 * feedback, and sync-zoom setup.  All bitmap processing and state
 * management is delegated to {@link OverlayCutViewModel}.</p>
 */
public class OverlayCutActivity extends AppCompatActivity {

    private static final String KEY_SYNC_IMAGE_INTERACTIONS = "key_sync_image_interactions";

    /** Survives configuration changes; owns bitmaps, crop logic and seekbar state. */
    private OverlayCutViewModel viewModel;

    /** Whether both image views pan/zoom together. */
    private final AtomicBoolean sync = new AtomicBoolean(true);

    /**
     * Tracks the two most-recently touched seekbars.
     * Exactly two seekbars define the diagonal cut line.
     */
    private final SeekBarTracker seekBarTracker = new SeekBarTracker();

    /** Tint applied to the currently active seekbar thumb. */
    private int colorActive;

    /** Tint applied to inactive seekbar thumbs. */
    private int colorInactive;

    private ActivityOverlayCutBinding binding;

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(OverlayCutViewModel.class);

        if (savedInstanceState != null) {
            sync.set(savedInstanceState.getBoolean(KEY_SYNC_IMAGE_INTERACTIONS, true));
        } else {
            sync.set(getIntent().getBooleanExtra(IntentExtras.SYNC_IMAGE_INTERACTIONS, true));
        }

        FullScreenHelper.setFullScreenFlags(getWindow());

        binding = ActivityOverlayCutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!initImages()) {
            // URIs are invalid or images could not be decoded; nothing to show.
            finish();
            return;
        }

        initColors();
        initSeekBars();
        initImageViews();
        initExtensionButtons();
        observeViewModel();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SYNC_IMAGE_INTERACTIONS, sync.get());

        if (binding != null) {
            viewModel.saveSeekBarState(
                    binding.fullSliderSeekbarTop.getProgress(),
                    binding.fullSliderSeekbarLeft.getProgress(),
                    binding.fullSliderSeekbarRight.getProgress(),
                    binding.fullSliderSeekbarBottom.getProgress()
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.cancelPendingCrop();
        binding = null;
    }

    // ── ViewModel observation ──────────────────────────────────────────────────

    /**
     * Starts observing the ViewModel's live output.  Must be called after the
     * binding is ready so that {@link android.widget.ImageView#setImageBitmap}
     * is always invoked on the main thread.
     */
    private void observeViewModel() {
        viewModel.getFrontBitmap().observe(this, bitmap -> {
            if (binding != null) {
                binding.fullSlideImageViewFront.setBitmapImage(bitmap);
            }
        });
    }

    // ── Initialisation helpers ─────────────────────────────────────────────────

    /**
     * Loads the two images from the intent URIs (first launch) or reuses the
     * bitmaps already retained by the ViewModel (after a configuration change).
     *
     * @return {@code true} if both bitmaps are available and ready to display.
     */
    private boolean initImages() {
        if (viewModel.areBitmapsLoaded()) {
            // Bitmaps survived a configuration change – nothing to reload.
            return true;
        }

        String uriOne = getIntent().getStringExtra(IntentExtras.IMAGE_URI_ONE);
        String uriTwo = getIntent().getStringExtra(IntentExtras.IMAGE_URI_TWO);

        Bitmap base  = BitmapExtractor.fromUriString(getContentResolver(), uriOne);
        Bitmap front = BitmapExtractor.fromUriString(getContentResolver(), uriTwo);

        if (base == null || front == null) {
            return false;
        }

        viewModel.bitmapBase     = base;
        viewModel.bitmapSource   = front;
        viewModel.bitmapAdjusted = front;
        return true;
    }

    private void initColors() {
        colorActive   = getResources().getColor(R.color.orange, null);
        colorInactive = getResources().getColor(android.R.color.darker_gray, null);
    }

    private void initSeekBars() {
        // The left/right seekbars are rotated 90° in XML, so their width attribute
        // maps to the visible vertical length.  We size them to match the screen height.
        int seekBarLength = getResources().getDisplayMetrics().heightPixels
                - Calculator.dpToPx(48, getResources());

        binding.fullSliderSeekbarLeft.getLayoutParams().width  = seekBarLength;
        binding.fullSliderSeekbarRight.getLayoutParams().width = seekBarLength;

        addSeekBarListener(binding.fullSliderSeekbarTop);
        addSeekBarListener(binding.fullSliderSeekbarLeft);
        addSeekBarListener(binding.fullSliderSeekbarRight);
        addSeekBarListener(binding.fullSliderSeekbarBottom);

        if (viewModel.hasSeekBarState()) {
            // Restore positions from before the configuration change.
            binding.fullSliderSeekbarTop.setProgress(viewModel.getSeekBarTopProgress());
            binding.fullSliderSeekbarLeft.setProgress(viewModel.getSeekBarLeftProgress());
            binding.fullSliderSeekbarRight.setProgress(viewModel.getSeekBarRightProgress());
            binding.fullSliderSeekbarBottom.setProgress(viewModel.getSeekBarBottomProgress());
        } else {
            // First-launch defaults: left slider far right, right slider far left.
            binding.fullSliderSeekbarLeft.setProgress(90);
            binding.fullSliderSeekbarRight.setProgress(10);
        }
    }

    private void initImageViews() {
        binding.fullSlideImageViewBase.setBitmapImage(viewModel.bitmapBase);
        // Show the last committed crop (equals bitmapSource on first launch).
        binding.fullSlideImageViewFront.setBitmapImage(viewModel.bitmapAdjusted);

        SyncZoom.setLinkedTargets(
                binding.fullSlideImageViewFront,
                binding.fullSlideImageViewBase,
                sync
        );
    }

    private void initExtensionButtons() {
        if (!getIntent().getBooleanExtra(IntentExtras.SHOW_EXTENSIONS, false)) {
            binding.overlayCutBtnReset.setVisibility(View.INVISIBLE);
            binding.overlayCutBtnCheck.setVisibility(View.INVISIBLE);
            return;
        }

        binding.overlayCutBtnReset.setOnClickListener(view -> {
            // Discard any committed edits and restore the original source image.
            viewModel.bitmapAdjusted = viewModel.bitmapSource;

            // Re-apply the active crop to the restored baseline (if two seekbars
            // are already selected) so the user sees a consistent result immediately.
            if (seekBarTracker.canCrop() && viewModel.bitmapAdjusted != null) {
                viewModel.submitCrop(
                        viewModel.bitmapAdjusted.copy(Bitmap.Config.ARGB_8888, true),
                        buildCropParams()
                );
            } else if (viewModel.bitmapAdjusted != null) {
                binding.fullSlideImageViewFront.setBitmapImage(viewModel.bitmapAdjusted);
            }
        });

        binding.overlayCutBtnCheck.setOnClickListener(view -> {
            // Commit the currently visible crop as the new baseline for further edits.
            Bitmap current = binding.fullSlideImageViewFront.getCurrentBitmap();
            if (current != null) {
                viewModel.bitmapAdjusted = current.copy(Bitmap.Config.ARGB_8888, false);
            }
        });
    }

    // ── Seekbar listener ───────────────────────────────────────────────────────

    private void addSeekBarListener(@NonNull SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Mark this seekbar's thumb as active.
                seekBar.getThumb().setTint(colorActive);

                // Inform the tracker; it returns whichever seekbar is now evicted
                // from the active pair (needs its thumb dimmed), or null if none.
                SeekBar evicted = seekBarTracker.onSeekBarChanged(seekBar);
                if (evicted != null) {
                    evicted.getThumb().setTint(colorInactive);
                }

                // Two seekbars selected → submit a crop job.
                if (seekBarTracker.canCrop() && viewModel.bitmapAdjusted != null) {
                    viewModel.submitCrop(
                            viewModel.bitmapAdjusted.copy(Bitmap.Config.ARGB_8888, true),
                            buildCropParams()
                    );
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar)  {}
        });
    }

    /**
     * Reads the current seekbar positions and active-flags from {@link #seekBarTracker}
     * to build an immutable {@link CropParams} snapshot.
     */
    @NonNull
    private CropParams buildCropParams() {
        return new CropParams(
                seekBarTracker.isActive(binding.fullSliderSeekbarTop),
                binding.fullSliderSeekbarTop.getProgress(),
                seekBarTracker.isActive(binding.fullSliderSeekbarLeft),
                binding.fullSliderSeekbarLeft.getProgress(),
                seekBarTracker.isActive(binding.fullSliderSeekbarRight),
                binding.fullSliderSeekbarRight.getProgress(),
                seekBarTracker.isActive(binding.fullSliderSeekbarBottom),
                binding.fullSliderSeekbarBottom.getProgress()
        );
    }

    // ── SeekBarTracker ─────────────────────────────────────────────────────────

    /**
     * Tracks the two most-recently touched seekbars (current and recent).
     * Exactly this pair defines the active diagonal crop line.
     */
    private static final class SeekBarTracker {

        @Nullable private SeekBar current;
        @Nullable private SeekBar recent;

        /**
         * Called whenever a seekbar value changes.
         *
         * @param seekBar the seekbar that changed
         * @return the seekbar that was just evicted from the active pair and
         *         therefore needs its thumb dimmed, or {@code null} if nothing
         *         changed in the active pair
         */
        @Nullable
        SeekBar onSeekBarChanged(@NonNull SeekBar seekBar) {
            if (current == null) {
                current = seekBar;
                return null;
            }
            if (seekBar.getId() == current.getId()) {
                // Same seekbar moved again – no pair change.
                return null;
            }
            // A new seekbar is becoming active; the old recent gets evicted.
            SeekBar evicted = (recent != null && seekBar.getId() != recent.getId())
                    ? recent : null;
            recent  = current;
            current = seekBar;
            return evicted;
        }

        /**
         * Returns {@code true} when both {@link #current} and {@link #recent} are
         * set, i.e. a crop geometry can be computed.
         */
        boolean canCrop() {
            return current != null && recent != null;
        }

        /**
         * Returns {@code true} when {@code seekBar} is one of the two currently
         * active seekbars (current or recent).
         */
        boolean isActive(@NonNull SeekBar seekBar) {
            int id = seekBar.getId();
            return (current != null && id == current.getId())
                    || (recent != null && id == recent.getId());
        }
    }
}
