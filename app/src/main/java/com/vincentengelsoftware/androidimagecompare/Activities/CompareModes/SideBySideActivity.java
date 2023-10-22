package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.Activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.ImageView.VesImageInterface;

public class SideBySideActivity extends AppCompatActivity {
    public static UtilMutableBoolean sync = new UtilMutableBoolean();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Status.activityIsOpening) {
            sync.value = getIntent().getBooleanExtra(IntentExtras.SYNCED_ZOOM, true);
        }

        Status.activityIsOpening = false;

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        setContentView(R.layout.activity_side_by_side);

        VesImageInterface first = findViewById(R.id.side_by_side_image_left);
        VesImageInterface second = findViewById(R.id.side_by_side_image_right);

        SyncZoom.setLinkedTargets(
                first,
                second,
                SideBySideActivity.sync
        );
        SyncZoom.setUpSyncZoomToggleButton(
                first,
                second,
                findViewById(R.id.toggleButton),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off),
                SideBySideActivity.sync,
                null
        );

        Images.first.updateVesImageViewWithAdjustedImage(findViewById(R.id.side_by_side_image_left));
        TextView imageFirst = findViewById(R.id.side_by_side_image_name_first);
        imageFirst.setText(Images.first.getImageName());

        Images.second.updateVesImageViewWithAdjustedImage(findViewById(R.id.side_by_side_image_right));
        TextView imageSecond = findViewById(R.id.side_by_side_image_name_second);
        imageSecond.setText(Images.second.getImageName());

        LinearLayout extensions = findViewById(R.id.side_by_side_extensions);
        if (getIntent().getBooleanExtra(IntentExtras.SHOW_EXTENSIONS, false)) {
            extensions.setVisibility(View.VISIBLE);
        } else {
            extensions.setVisibility(View.GONE);
        }
    }
}