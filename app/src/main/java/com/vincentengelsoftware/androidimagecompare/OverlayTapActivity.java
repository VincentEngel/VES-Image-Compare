package com.vincentengelsoftware.androidimagecompare;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.helper.TapHelper;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.viewClasses.VesImageInterface;

public class OverlayTapActivity extends AppCompatActivity {
    public static UtilMutableBoolean sync = new UtilMutableBoolean();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Status.activityIsOpening = false;

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        setContentView(R.layout.activity_overlay_tap);

        VesImageInterface image_first = findViewById(R.id.overlay_tap_image_view_one);
        Images.first.updateVesImageViewWithAdjustedImage(image_first);

        VesImageInterface image_second = findViewById(R.id.overlay_tap_image_view_two);
        Images.second.updateVesImageViewWithAdjustedImage(image_second);

        image_second.setVisibility(View.INVISIBLE);

        TapHelper.setOnClickListener(image_first, image_second);
        TapHelper.setOnClickListener(image_second, image_first);

        SyncZoom.setUpSyncZoom(
                image_first,
                image_second,
                findViewById(R.id.overlay_tap_button_zoom_sync),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off),
                OverlayTapActivity.sync
        );
    }
}