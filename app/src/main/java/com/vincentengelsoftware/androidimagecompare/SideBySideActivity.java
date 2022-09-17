package com.vincentengelsoftware.androidimagecompare;

import android.os.Bundle;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.viewClasses.VesImageInterface;

public class SideBySideActivity extends AppCompatActivity {
    public static boolean sync = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Status.activityIsOpening = false;

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        setContentView(R.layout.activity_side_by_side);

        VesImageInterface first = findViewById(R.id.side_by_side_image_left);
        VesImageInterface second = findViewById(R.id.side_by_side_image_right);
        first.setLinkedTarget(second);
        second.setLinkedTarget(first);

        ToggleButton sync = findViewById(R.id.toggleButton);
        sync.setChecked(SideBySideActivity.sync);
        if (SideBySideActivity.sync) {
            sync.setBackgroundDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link));
        } else {
            sync.setBackgroundDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off));
        }

        sync.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                first.resetScaleAndCenter();
                second.resetScaleAndCenter();
                sync.setBackgroundDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link));
            } else {
                sync.setBackgroundDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off));
            }
            SideBySideActivity.sync = !SideBySideActivity.sync;
        });

        Images.first.updateVesImageViewWithAdjustedImage(findViewById(R.id.side_by_side_image_left));

        Images.second.updateVesImageViewWithAdjustedImage(findViewById(R.id.side_by_side_image_right));
    }
}