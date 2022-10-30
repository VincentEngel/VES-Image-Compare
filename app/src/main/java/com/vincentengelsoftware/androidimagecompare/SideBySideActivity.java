package com.vincentengelsoftware.androidimagecompare;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.viewClasses.VesImageInterface;

public class SideBySideActivity extends AppCompatActivity {
    public static UtilMutableBoolean sync = new UtilMutableBoolean();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Status.activityIsOpening = false;

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        setContentView(R.layout.activity_side_by_side);

        VesImageInterface first = findViewById(R.id.side_by_side_image_left);
        VesImageInterface second = findViewById(R.id.side_by_side_image_right);

        SyncZoom.setUpSyncZoom(
                first,
                second,
                findViewById(R.id.toggleButton),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off),
                SideBySideActivity.sync
        );

        Images.first.updateVesImageViewWithAdjustedImage(findViewById(R.id.side_by_side_image_left));

        Images.second.updateVesImageViewWithAdjustedImage(findViewById(R.id.side_by_side_image_right));
    }
}