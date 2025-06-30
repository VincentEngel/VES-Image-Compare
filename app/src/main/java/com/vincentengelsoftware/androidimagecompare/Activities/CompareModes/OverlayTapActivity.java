package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.Activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityOverlayTapBinding;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Settings;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.helper.TapHelper;

import java.util.concurrent.atomic.AtomicBoolean;

public class OverlayTapActivity extends AppCompatActivity {
    public static AtomicBoolean sync = new AtomicBoolean(true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Status.activityIsOpening) {
            sync.set(getIntent().getBooleanExtra(IntentExtras.SYNCED_ZOOM, true));
        }
        Status.activityIsOpening = false;

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        ActivityOverlayTapBinding binding = ActivityOverlayTapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            Images.first.updateVesImageViewWithAdjustedImage(binding.overlayTapImageViewOne);
        } catch (Exception e) {
            this.finish();
        }

        binding.overlayTapImageName.setText(Images.first.getImageName());
        Images.second.updateVesImageViewWithAdjustedImage(binding.overlayTapImageViewTwo);

        if (Settings.TAP_HIDE_MODE == Status.TAP_HIDE_MODE_INVISIBLE) {
            binding.overlayTapImageViewTwo.setVisibility(View.INVISIBLE);
        } else {
            binding.overlayTapImageViewOne.bringToFront();
        }

        TapHelper.setOnClickListener(
                binding.overlayTapImageViewOne,
                binding.overlayTapImageViewTwo,
                OverlayTapActivity.sync,
                binding.overlayTapImageName,
                Images.second
        );

        TapHelper.setOnClickListener(
                binding.overlayTapImageViewTwo,
                binding.overlayTapImageViewOne,
                OverlayTapActivity.sync,
                binding.overlayTapImageName,
                Images.first
        );

        SyncZoom.setUpSyncZoomToggleButton(
                binding.overlayTapImageViewOne,
                binding.overlayTapImageViewTwo,
                binding.overlayTapButtonZoomSync,
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off),
                OverlayTapActivity.sync,
                null
        );

        if (getIntent().getBooleanExtra(IntentExtras.SHOW_EXTENSIONS, false)) {
            binding.overlayTapExtensions.setVisibility(View.VISIBLE);
        } else {
            binding.overlayTapExtensions.setVisibility(View.GONE);
        }
    }
}