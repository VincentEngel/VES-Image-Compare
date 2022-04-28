package com.vincentengelsoftware.androidimagecompare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.vincentengelsoftware.androidimagecompare.helper.MainHelper;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableUri;

public class MainActivity extends AppCompatActivity {
    public static final String KEY_URI_IMAGE_FIRST = "key.uri.image.first";
    public static final String KEY_URI_IMAGE_SECOND = "key.uri.image.second";

    protected UtilMutableUri uri_image_first = new UtilMutableUri();
    protected UtilMutableUri uri_image_second = new UtilMutableUri();

    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpActivityButtons();

        addLoadImageLogic(
                findViewById(R.id.home_image_first),
                uri_image_first
        );

        addLoadImageLogic(
                findViewById(R.id.home_image_second),
                uri_image_second
        );
    }

    @Override
    public void onBackPressed() {

        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            this.finishAndRemoveTask();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }

    private void setUpActivityButtons()
    {
        addButtonChangeActivityLogic(
                findViewById(R.id.button_side_by_side),
                SideBySideActivity.class
        );

        addButtonChangeActivityLogic(
                findViewById(R.id.button_overlay_tap),
                OverlayTapActivity.class
        );

        addButtonChangeActivityLogic(
                findViewById(R.id.button_overlay_slide),
                OverlaySlideActivity.class
        );

        addButtonChangeActivityLogic(
                findViewById(R.id.button_overlay_transparent),
                OverlayTransparentActivity.class
        );

        findViewById(R.id.home_button_info).setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
            startActivity(intent);
        });

        MainHelper.passClickToUnderlyingView(
                findViewById(R.id.frame_layout_image_right),
                findViewById(R.id.home_button_swap_images)
        );

        MainHelper.addSwapImageLogic(
                findViewById(R.id.home_button_swap_images),
                uri_image_first,
                uri_image_second,
                findViewById(R.id.home_image_first),
                findViewById(R.id.home_image_second)
        );
    }

    private void addButtonChangeActivityLogic(Button btn, Class<?> targetActivity)
    {
        btn.setOnClickListener(view -> {
            if (uri_image_first.uri == null || uri_image_second.uri == null) {
                return;
            }

            Intent intent = new Intent(getApplicationContext(), targetActivity);
            intent.putExtra(KEY_URI_IMAGE_FIRST, uri_image_first.uri.toString());
            intent.putExtra(KEY_URI_IMAGE_SECOND, uri_image_second.uri.toString());
            startActivity(intent);
        });
    }

    private void addLoadImageLogic(ImageView imageView, UtilMutableUri mutableUri) {
        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        return;
                    }

                    mutableUri.uri = uri;
                    imageView.setImageURI(uri);
                });

        imageView.setOnClickListener(view -> {
            // Pass in the mime type you'd like to allow the user to select as the input
            mGetContent.launch("image/*");
        });
    }

    // This callback is called only when there is a saved instance that is previously saved by using
// onSaveInstanceState(). We restore some state in onCreate(), while we can optionally restore
// other state here, possibly usable after onStart() has completed.
// The savedInstanceState Bundle is same as the one used in onCreate().
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.getString(KEY_URI_IMAGE_FIRST) != null) {
            ImageView imageView = findViewById(R.id.home_image_first);
            uri_image_first.uri = Uri.parse(savedInstanceState.getString(KEY_URI_IMAGE_FIRST));
            imageView.setImageURI(uri_image_first.uri);
        }

        if (savedInstanceState.getString(KEY_URI_IMAGE_SECOND) != null) {
            ImageView imageView = findViewById(R.id.home_image_second);
            uri_image_second.uri = Uri.parse(savedInstanceState.getString(KEY_URI_IMAGE_SECOND));
            imageView.setImageURI(uri_image_second.uri);
        }
    }


    // invoked when the activity may be temporarily destroyed, save the instance state here
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (uri_image_first.uri != null) {
            outState.putString(KEY_URI_IMAGE_FIRST, uri_image_first.uri.toString());
        }

        if (uri_image_second.uri != null) {
            outState.putString(KEY_URI_IMAGE_SECOND, uri_image_second.uri.toString());
        }

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }
}