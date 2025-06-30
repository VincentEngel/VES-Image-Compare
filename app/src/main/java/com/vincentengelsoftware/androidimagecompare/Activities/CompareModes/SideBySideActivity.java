package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.Activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivitySideBySideBinding;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;

import java.util.concurrent.atomic.AtomicBoolean;

public class SideBySideActivity extends AppCompatActivity {
    public static AtomicBoolean sync = new AtomicBoolean(true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Status.activityIsOpening) {
            sync.set(getIntent().getBooleanExtra(IntentExtras.SYNCED_ZOOM, true));
        }

        Status.activityIsOpening = false;

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        ActivitySideBySideBinding binding = ActivitySideBySideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SyncZoom.setLinkedTargets(
                binding.sideBySideImageLeft,
                binding.sideBySideImageRight,
                SideBySideActivity.sync,
                new AtomicBoolean(false)
        );
        SyncZoom.setUpSyncZoomToggleButton(
                binding.sideBySideImageLeft,
                binding.sideBySideImageRight,
                binding.toggleButton,
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off),
                SideBySideActivity.sync,
                null
        );

        try {
            Images.first.updateVesImageViewWithAdjustedImage(binding.sideBySideImageLeft);
            binding.sideBySideImageNameFirst.setText(Images.first.getImageName());

            Images.second.updateVesImageViewWithAdjustedImage(binding.sideBySideImageRight);
            binding.sideBySideImageNameSecond.setText(Images.second.getImageName());
        } catch (Exception e) {
            this.finish();
        }

        if (getIntent().getBooleanExtra(IntentExtras.SHOW_EXTENSIONS, false)) {
            binding.sideBySideExtensions.setVisibility(View.VISIBLE);
        } else {
            binding.sideBySideExtensions.setVisibility(View.GONE);
        }
    }
}