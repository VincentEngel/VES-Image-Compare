package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.vincentengelsoftware.androidimagecompare.Activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityOverlayCutBinding;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapExtractor;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapHelper;
import com.vincentengelsoftware.androidimagecompare.helper.Calculator;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;

import java.util.concurrent.atomic.AtomicBoolean;

public class OverlayCutActivity extends AppCompatActivity {
    private static final String KEY_SYNC_IMAGE_INTERACTIONS = "key_sync_image_interactions";

    /** Survives configuration changes; holds the adjusted bitmap and seekbar positions. */
    private OverlayCutViewModel viewModel;

    /** Whether both image views pan/zoom together. */
    private final AtomicBoolean sync = new AtomicBoolean(true);

    /** Handler bound to the main thread – used for all deferred UI updates. */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** Background thread processing the current bitmap-crop request. */
    private Thread currentThread;

    /** Next queued bitmap-crop request, promoted once currentThread finishes. */
    private Thread nextThread;

    /** Tint applied to the active seekbar thumb. */
    private int colorActive;

    /** Tint applied to inactive seekbar thumbs. */
    private int colorInactive;

    /** The seekbar the user touched most recently (i.e. the current one). */
    private SeekBar currentSeekBar;

    /** The seekbar that was active just before currentSeekBar. */
    private SeekBar recentSeekBar;

    /** Decoded base (background) image. */
    private Bitmap bitmapBaseView;

    /** Decoded source (front) image – never mutated after load. */
    private Bitmap bitmapSource;

    /**
     * Working copy of the front image.  Reset by the Reset button; updated by
     * the Check button after the user is happy with the current crop.
     * Persisted across rotation via {@link OverlayCutViewModel}.
     */
    private Bitmap bitmapAdjusted;

    private ActivityOverlayCutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain ViewModel – survives rotation automatically.
        viewModel = new ViewModelProvider(this).get(OverlayCutViewModel.class);

        // Restore sync state across configuration changes.
        if (savedInstanceState != null) {
            sync.set(savedInstanceState.getBoolean(KEY_SYNC_IMAGE_INTERACTIONS, true));
        } else {
            sync.set(getIntent().getBooleanExtra(IntentExtras.SYNC_IMAGE_INTERACTIONS, true));
        }

        FullScreenHelper.setFullScreenFlags(getWindow());

        binding = ActivityOverlayCutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!initImages()) {
            // URIs are invalid; nothing to display.
            finish();
            return;
        }

        // Restore the adjusted bitmap that survived rotation (null on first launch).
        if (viewModel.bitmapAdjusted != null) {
            bitmapAdjusted = viewModel.bitmapAdjusted;
        }

        initColors();
        initSeekBars();
        initImageViews();
        initExtensionButtons();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SYNC_IMAGE_INTERACTIONS, sync.get());

        // Snapshot seekbar positions into the ViewModel before the activity is recreated.
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
        interruptBitmapThreads();
        binding = null;
    }

    /**
     * Decodes both images from the intent URIs.
     *
     * @return {@code true} if both bitmaps were loaded successfully.
     */
    private boolean initImages() {
        String uriOne = getIntent().getStringExtra(IntentExtras.IMAGE_URI_ONE);
        String uriTwo = getIntent().getStringExtra(IntentExtras.IMAGE_URI_TWO);

        bitmapBaseView = BitmapExtractor.fromUriString(getContentResolver(), uriOne);
        Bitmap bitmapTwo = BitmapExtractor.fromUriString(getContentResolver(), uriTwo);

        if (bitmapBaseView == null || bitmapTwo == null) {
            return false;
        }

        bitmapSource = bitmapTwo;
        bitmapAdjusted = bitmapTwo;

        return true;
    }

    private void initColors() {
        colorActive = getResources().getColor(R.color.orange, null);
        colorInactive = getResources().getColor(android.R.color.darker_gray, null);
    }

    private void initSeekBars() {
        // The left/right seekbars are rotated 90 ° in the layout, so their
        // visual length matches the screen height rather than width.
        int seekBarLength = getResources().getDisplayMetrics().heightPixels
                - Calculator.DpToPx2(48, getResources());

        ViewGroup.LayoutParams paramsLeft = binding.fullSliderSeekbarLeft.getLayoutParams();
        paramsLeft.width = seekBarLength;

        ViewGroup.LayoutParams paramsRight = binding.fullSliderSeekbarRight.getLayoutParams();
        paramsRight.width = seekBarLength;

        addSeekBarListener(binding.fullSliderSeekbarTop);
        addSeekBarListener(binding.fullSliderSeekbarLeft);
        addSeekBarListener(binding.fullSliderSeekbarRight);
        addSeekBarListener(binding.fullSliderSeekbarBottom);

        if (viewModel.hasSeekBarState) {
            // Restore seekbar positions from before the rotation.
            binding.fullSliderSeekbarTop.setProgress(viewModel.seekBarTopProgress);
            binding.fullSliderSeekbarLeft.setProgress(viewModel.seekBarLeftProgress);
            binding.fullSliderSeekbarRight.setProgress(viewModel.seekBarRightProgress);
            binding.fullSliderSeekbarBottom.setProgress(viewModel.seekBarBottomProgress);
        } else {
            // First launch defaults: left slider mostly right, right slider mostly left.
            binding.fullSliderSeekbarLeft.setProgress(90);
            binding.fullSliderSeekbarRight.setProgress(10);
        }
    }

    private void initImageViews() {
        binding.fullSlideImageViewBase.setBitmapImage(bitmapBaseView);
        // bitmapAdjusted equals bitmapSource on first launch; after rotation it
        // holds the last committed crop so the correct state is shown immediately.
        binding.fullSlideImageViewFront.setBitmapImage(bitmapAdjusted);

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
            // Discard edits and restore the original source bitmap.
            bitmapAdjusted = bitmapSource;
            viewModel.bitmapAdjusted = bitmapSource;

            // Nudge the active seekbar so updateImage() is triggered.
            if (currentSeekBar != null) {
                int progress = currentSeekBar.getProgress();
                currentSeekBar.setProgress(progress > 1 ? progress - 1 : 1);
            }
        });

        binding.overlayCutBtnCheck.setOnClickListener(view -> {
            // Commit the current visible crop as the new base for further edits.
            Bitmap current = binding.fullSlideImageViewFront.getCurrentBitmap();
            if (current != null) {
                bitmapAdjusted = current.copy(Bitmap.Config.ARGB_8888, false);
                viewModel.bitmapAdjusted = bitmapAdjusted;
            }
        });
    }

    private void addSeekBarListener(@NonNull SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBar.getThumb().setTint(colorActive);

                if (currentSeekBar == null) {
                    currentSeekBar = seekBar;
                    return;
                }

                if (seekBar.getId() != currentSeekBar.getId()) {
                    // Dim the previously active seekbar thumb.
                    if (recentSeekBar != null && seekBar.getId() != recentSeekBar.getId()) {
                        recentSeekBar.getThumb().setTint(colorInactive);
                    }
                    recentSeekBar = currentSeekBar;
                    currentSeekBar = seekBar;
                }

                if (bitmapAdjusted != null) {
                    updateImage(bitmapAdjusted.copy(Bitmap.Config.ARGB_8888, true));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    /**
     * Reads the current seekbar states and enqueues a bitmap-crop operation on
     * a background thread.  The result is posted back to the main thread.
     *
     * @param bitmapSource mutable copy of the bitmap to crop.
     */
    private void updateImage(@NonNull Bitmap bitmapSource) {
        if (currentSeekBar == null || recentSeekBar == null) {
            return;
        }

        final boolean topActive     = isSeekBarActive(binding.fullSliderSeekbarTop);
        final int     topProgress   = topActive    ? binding.fullSliderSeekbarTop.getProgress()    : 0;
        final boolean leftActive    = isSeekBarActive(binding.fullSliderSeekbarLeft);
        final int     leftProgress  = leftActive   ? binding.fullSliderSeekbarLeft.getProgress()   : 0;
        final boolean rightActive   = isSeekBarActive(binding.fullSliderSeekbarRight);
        final int     rightProgress = rightActive  ? binding.fullSliderSeekbarRight.getProgress()  : 0;
        final boolean bottomActive  = isSeekBarActive(binding.fullSliderSeekbarBottom);
        final int     bottomProgress= bottomActive ? binding.fullSliderSeekbarBottom.getProgress() : 0;

        enqueueBitmapCrop(new Thread(() -> {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            Bitmap result = BitmapHelper.cutBitmapAny(
                    bitmapSource,
                    topActive,
                    topProgress,
                    leftActive,
                    leftProgress,
                    rightActive,
                    rightProgress,
                    bottomActive,
                    bottomProgress
            );

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            mainHandler.post(() -> {
                if (binding == null) {
                    return;
                }
                binding.fullSlideImageViewFront.setBitmapImage(result);
            });

            currentThread = null;
            processNextBitmapThread();
        }));
    }

    /**
     * Returns {@code true} when {@code seekBar} is one of the two currently
     * active seekbars (current or recent).
     */
    private boolean isSeekBarActive(@NonNull SeekBar seekBar) {
        int id = seekBar.getId();
        return (currentSeekBar != null && id == currentSeekBar.getId())
                || (recentSeekBar != null && id == recentSeekBar.getId());
    }

    /** Starts {@code thread} immediately if no crop is running, otherwise queues it. */
    private synchronized void enqueueBitmapCrop(@NonNull Thread thread) {
        if (currentThread == null) {
            currentThread = thread;
            currentThread.start();
        } else {
            nextThread = thread;
        }
    }

    /** Promotes the queued thread (if any) once the current one has finished. */
    private synchronized void processNextBitmapThread() {
        if (nextThread != null) {
            currentThread = nextThread;
            nextThread = null;
            currentThread.start();
        }
    }

    /** Interrupts any in-flight bitmap threads; called from {@link #onDestroy()}. */
    private synchronized void interruptBitmapThreads() {
        if (nextThread != null) {
            nextThread.interrupt();
            nextThread = null;
        }
        if (currentThread != null) {
            currentThread.interrupt();
            currentThread = null;
        }
    }
}

