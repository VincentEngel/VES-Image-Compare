package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.Activities.IntentExtras;
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
    public static AtomicBoolean sync = new AtomicBoolean(true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Status.activityIsOpening) {
            sync.set(getIntent().getBooleanExtra(IntentExtras.SYNC_IMAGE_INTERACTIONS, true));
            Status.activityIsOpening = false;
        }

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        ActivityOverlayTapBinding binding = ActivityOverlayTapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String uriOne = getIntent().getStringExtra(IntentExtras.IMAGE_URI_ONE);
        String uriTwo = getIntent().getStringExtra(IntentExtras.IMAGE_URI_TWO);
        String nameOne = getIntent().getStringExtra(IntentExtras.IMAGE_NAME_ONE);
        String nameTwo = getIntent().getStringExtra(IntentExtras.IMAGE_NAME_TWO);

        Bitmap bitmapFirst = BitmapExtractor.fromUriString(getContentResolver(), uriOne);
        Bitmap bitmapSecond = BitmapExtractor.fromUriString(getContentResolver(), uriTwo);

        try {
            binding.overlayTapImageViewOne.setBitmapImage(bitmapFirst);
        } catch (Exception e) {
            this.finish();
        }

        binding.overlayTapImageName.setText(nameOne);
        binding.overlayTapImageViewTwo.setBitmapImage(bitmapSecond);

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
                nameTwo
        );

        TapHelper.setOnClickListener(
                binding.overlayTapImageViewTwo,
                binding.overlayTapImageViewOne,
                OverlayTapActivity.sync,
                binding.overlayTapImageName,
                nameOne
        );

        SyncZoom.setUpSyncZoomToggleButton(
                binding.overlayTapImageViewOne,
                binding.overlayTapImageViewTwo,
                binding.overlayTapButtonZoomSync,
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off),
                OverlayTapActivity.sync
        );

        if (getIntent().getBooleanExtra(IntentExtras.SHOW_EXTENSIONS, false)) {
            binding.overlayTapExtensions.setVisibility(View.VISIBLE);
        } else {
            binding.overlayTapExtensions.setVisibility(View.GONE);
        }
    }
}