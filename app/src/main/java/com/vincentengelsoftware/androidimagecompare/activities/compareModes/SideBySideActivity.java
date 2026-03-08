package com.vincentengelsoftware.androidimagecompare.activities.compareModes;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivitySideBySideBinding;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapExtractor;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SideBySideActivity extends AppCompatActivity {

    private static final String KEY_SYNC_IMAGE_INTERACTIONS = "key_sync_image_interactions";

    /** Shared sync state, retained across config changes via savedInstanceState. */
    private final AtomicBoolean syncImageInteractions = new AtomicBoolean(true);

    private ActivitySideBySideBinding binding;
    private ExecutorService imageLoadExecutor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FullScreenHelper.setFullScreenFlags(getWindow());

        binding = ActivitySideBySideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Restore or initialise the sync-zoom state.
        // On first launch the value comes from the launching Intent;
        // on configuration changes it is preserved via savedInstanceState.
        if (savedInstanceState != null) {
            syncImageInteractions.set(savedInstanceState.getBoolean(KEY_SYNC_IMAGE_INTERACTIONS, true));
        } else {
            syncImageInteractions.set(getIntent().getBooleanExtra(IntentExtras.SYNC_IMAGE_INTERACTIONS, true));
        }

        SyncZoom.setLinkedTargets(
                binding.sideBySideImageTopLeft,
                binding.sideBySideImageBottomRight,
                syncImageInteractions
        );

        SyncZoom.setUpSyncZoomToggleButton(
                binding.sideBySideImageTopLeft,
                binding.sideBySideImageBottomRight,
                binding.toggleButton,
                ContextCompat.getDrawable(this, R.drawable.ic_link),
                ContextCompat.getDrawable(this, R.drawable.ic_link_off),
                syncImageInteractions
        );

        toggleExtensionsVisibility();
        loadImagesAsync();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SYNC_IMAGE_INTERACTIONS, syncImageInteractions.get());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageLoadExecutor != null) {
            imageLoadExecutor.shutdownNow();
        }
        binding = null;
    }

    /** Shows or hides the extensions bar based on the Intent extra. */
    private void toggleExtensionsVisibility() {
        boolean showExtensions = getIntent().getBooleanExtra(IntentExtras.SHOW_EXTENSIONS, false);
        binding.sideBySideExtensions.setVisibility(showExtensions ? View.VISIBLE : View.GONE);
    }

    /**
     * Decodes the two bitmaps on a background thread and delivers them to the
     * UI thread once ready.  Calls {@link #finish()} if either image cannot be
     * loaded so the user is never shown a broken screen.
     */
    private void loadImagesAsync() {
        final String uriOne = getIntent().getStringExtra(IntentExtras.IMAGE_URI_ONE);
        final String uriTwo = getIntent().getStringExtra(IntentExtras.IMAGE_URI_TWO);
        final String nameOne = getIntent().getStringExtra(IntentExtras.IMAGE_NAME_ONE);
        final String nameTwo = getIntent().getStringExtra(IntentExtras.IMAGE_NAME_TWO);

        imageLoadExecutor = Executors.newSingleThreadExecutor();
        imageLoadExecutor.execute(() -> {
            final var bitmapOne = BitmapExtractor.fromUriString(getContentResolver(), uriOne);
            final var bitmapTwo = BitmapExtractor.fromUriString(getContentResolver(), uriTwo);

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || binding == null) {
                    return;
                }

                try {
                    binding.sideBySideImageTopLeft.setBitmapImage(bitmapOne);
                    binding.sideBySideImageNameTopLeft.setText(nameOne);

                    binding.sideBySideImageBottomRight.setBitmapImage(bitmapTwo);
                    binding.sideBySideImageNameBottomRight.setText(nameTwo);
                } catch (Exception e) {
                    finish();
                }
            });
        });
    }
}