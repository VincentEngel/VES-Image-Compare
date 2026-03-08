package com.vincentengelsoftware.androidimagecompare.activities.compareModes;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityOverlayTapBinding;
import com.vincentengelsoftware.androidimagecompare.globals.Settings;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapExtractor;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.helper.TapHelper;

import java.util.concurrent.atomic.AtomicBoolean;

public class OverlayTapActivity extends AppCompatActivity {

    private static final String KEY_SYNC_IMAGE_INTERACTIONS = "key_sync_image_interactions";

    /** Sync state retained across config changes via savedInstanceState. */
    private final AtomicBoolean sync = new AtomicBoolean(true);

    private ActivityOverlayTapBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // On first launch read the sync state from the Intent;
        // on configuration changes restore it from savedInstanceState.
        if (savedInstanceState != null) {
            sync.set(savedInstanceState.getBoolean(KEY_SYNC_IMAGE_INTERACTIONS, true));
        } else {
            sync.set(getIntent().getBooleanExtra(IntentExtras.SYNC_IMAGE_INTERACTIONS, true));
        }

        FullScreenHelper.setFullScreenFlags(getWindow());

        binding = ActivityOverlayTapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initImages();
        initControls();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SYNC_IMAGE_INTERACTIONS, sync.get());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void initImages() {
        String uriOne = getIntent().getStringExtra(IntentExtras.IMAGE_URI_ONE);
        String uriTwo = getIntent().getStringExtra(IntentExtras.IMAGE_URI_TWO);

        Bitmap bitmapFirst  = BitmapExtractor.fromUriString(getContentResolver(), uriOne);
        Bitmap bitmapSecond = BitmapExtractor.fromUriString(getContentResolver(), uriTwo);

        try {
            binding.overlayTapImageViewOne.setBitmapImage(bitmapFirst);
        } catch (Exception e) {
            finish();
            return;
        }

        binding.overlayTapImageViewTwo.setBitmapImage(bitmapSecond);

        if (Settings.TAP_HIDE_MODE == Status.TAP_HIDE_MODE_INVISIBLE) {
            binding.overlayTapImageViewTwo.setVisibility(View.INVISIBLE);
        } else {
            binding.overlayTapImageViewOne.bringToFront();
        }
    }

    private void initControls() {
        String nameOne = getIntent().getStringExtra(IntentExtras.IMAGE_NAME_ONE);
        String nameTwo = getIntent().getStringExtra(IntentExtras.IMAGE_NAME_TWO);

        binding.overlayTapImageName.setText(nameOne);

        TapHelper.setOnClickListener(
                binding.overlayTapImageViewOne,
                binding.overlayTapImageViewTwo,
                sync,
                binding.overlayTapImageName,
                nameTwo
        );

        TapHelper.setOnClickListener(
                binding.overlayTapImageViewTwo,
                binding.overlayTapImageViewOne,
                sync,
                binding.overlayTapImageName,
                nameOne
        );

        SyncZoom.setUpSyncZoomToggleButton(
                binding.overlayTapImageViewOne,
                binding.overlayTapImageViewTwo,
                binding.overlayTapButtonZoomSync,
                ContextCompat.getDrawable(this, R.drawable.ic_link),
                ContextCompat.getDrawable(this, R.drawable.ic_link_off),
                sync
        );

        if (getIntent().getBooleanExtra(IntentExtras.SHOW_EXTENSIONS, false)) {
            binding.overlayTapExtensions.setVisibility(View.VISIBLE);
        } else {
            binding.overlayTapExtensions.setVisibility(View.GONE);
        }
    }
}