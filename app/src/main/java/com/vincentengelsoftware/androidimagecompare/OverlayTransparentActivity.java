package com.vincentengelsoftware.androidimagecompare;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.helper.TransparentHelper;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.viewClasses.VesImageInterface;

public class OverlayTransparentActivity extends AppCompatActivity {
    public static UtilMutableBoolean sync = new UtilMutableBoolean();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Status.activityIsOpening) {
            sync.value = Status.SYNCED_ZOOM;
        }

        Status.activityIsOpening = false;

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        setContentView(R.layout.activity_overlay_transparent);

        VesImageInterface base = findViewById(R.id.overlay_transparent_image_view_base);
        Images.first.updateVesImageViewWithAdjustedImage(base);

        VesImageInterface image_transparent = findViewById(R.id.overlay_transparent_image_view_transparent);

        Images.second.updateVesImageViewWithAdjustedImage(image_transparent);

        image_transparent.bringToFront();


        ImageButton hideShow = findViewById(R.id.overlay_transparent_button_hide_front_image);

        SeekBar seekBar = findViewById(R.id.overlay_slide_seek_bar);

        TransparentHelper.makeTargetTransparent(seekBar, image_transparent, hideShow);

        seekBar.setProgress(50);

        hideShow.setOnClickListener(view -> {
            if (image_transparent.getVisibility() == View.VISIBLE) {
                hideShow.setImageResource(R.drawable.ic_visibility_off);
                image_transparent.setVisibility(View.GONE);
            } else if (seekBar.getProgress() <= 2) {
                seekBar.setProgress(3);
            } else {
                hideShow.setImageResource(R.drawable.ic_visibility);
                image_transparent.setVisibility(View.VISIBLE);
            }
        });

        SyncZoom.setLinkedTargets(
                base,
                image_transparent,
                OverlayTransparentActivity.sync
        );
        SyncZoom.setUpSyncZoomToggleButton(
                base,
                image_transparent,
                findViewById(R.id.overlay_transparent_button_zoom_sync),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off),
                OverlayTransparentActivity.sync
        );
    }
}
