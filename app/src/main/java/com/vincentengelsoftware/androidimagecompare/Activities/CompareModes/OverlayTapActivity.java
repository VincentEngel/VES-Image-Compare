package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.os.Bundle;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.Activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Settings;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.helper.TapHelper;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.ImageView.VesImageInterface;

public class OverlayTapActivity extends AppCompatActivity {
    public static UtilMutableBoolean sync = new UtilMutableBoolean(true);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Status.activityIsOpening) {
            sync.value = getIntent().getBooleanExtra(IntentExtras.SYNCED_ZOOM, true);
        }
        Status.activityIsOpening = false;

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        setContentView(R.layout.activity_overlay_tap);

        VesImageInterface image_first = findViewById(R.id.overlay_tap_image_view_one);
        try {
            Images.first.updateVesImageViewWithAdjustedImage(image_first);
        } catch (Exception e) {
            this.finish();
        }

        TextView textView = findViewById(R.id.overlay_tap_image_name);
        textView.setText(Images.first.getImageName());

        VesImageInterface image_second = findViewById(R.id.overlay_tap_image_view_two);
        Images.second.updateVesImageViewWithAdjustedImage(image_second);

        if (Settings.TAP_HIDE_MODE == Status.TAP_HIDE_MODE_INVISIBLE) {
            image_second.setVisibility(View.INVISIBLE);
        } else {
            image_first.bringToFront();
        }


        TapHelper.setOnClickListener(image_first, image_second, OverlayTapActivity.sync, textView, Images.second);
        TapHelper.setOnClickListener(image_second, image_first, OverlayTapActivity.sync, textView, Images.first);

        SyncZoom.setUpSyncZoomToggleButton(
                image_first,
                image_second,
                findViewById(R.id.overlay_tap_button_zoom_sync),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off),
                OverlayTapActivity.sync,
                null
        );

        TableRow extensions = findViewById(R.id.overlay_tap_extensions);
        if (getIntent().getBooleanExtra(IntentExtras.SHOW_EXTENSIONS, false)) {
            extensions.setVisibility(View.VISIBLE);
        } else {
            extensions.setVisibility(View.GONE);
        }
    }
}