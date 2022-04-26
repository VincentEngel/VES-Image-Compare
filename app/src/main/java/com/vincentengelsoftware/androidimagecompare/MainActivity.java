package com.vincentengelsoftware.androidimagecompare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static final String KEY_URI_IMAGE_FIRST = "key.uri.image.first";
    public static final String KEY_URI_IMAGE_SECOND = "key.uri.image.second";

    protected Uri uri_image_first;
    protected Uri uri_image_second;

    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpActivityButtons();

        addLoadImageLogic(
                findViewById(R.id.home_image_first),
                KEY_URI_IMAGE_FIRST
        );

        addLoadImageLogic(
                findViewById(R.id.home_image_second),
                KEY_URI_IMAGE_SECOND
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

        findViewById(R.id.frame_layout_image_right).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    int[] imageButtonLocation = new int[2];
                    ImageButton imageButton = findViewById(R.id.home_button_swap_images);
                    imageButton.getLocationOnScreen(imageButtonLocation);

                    int[] viewLocation = new int[2];
                    v.getLocationOnScreen(viewLocation);


                    imageButtonLocation[0] = imageButtonLocation[0] - viewLocation[0];
                    imageButtonLocation[1] = imageButtonLocation[1] - viewLocation[1];

                    if (
                            x >= imageButtonLocation[0]
                                    && x <= (imageButtonLocation[0] + imageButton.getWidth())
                                    && y >= imageButtonLocation[1]
                                    && y <= (imageButtonLocation[1] + imageButton.getHeight())
                    ) {
                        findViewById(R.id.home_button_swap_images).callOnClick();
                    }
                }
                return true;
            }
        });

        findViewById(R.id.home_button_swap_images).setOnClickListener(view -> {
            Uri temp = uri_image_first;
            uri_image_first = uri_image_second;
            uri_image_second = temp;

            ImageView image_first = findViewById(R.id.home_image_first);
            image_first.setImageURI(uri_image_first);

            ImageView image_second = findViewById(R.id.home_image_second);
            image_second.setImageURI(uri_image_second);
        });
    }

    private void addButtonChangeActivityLogic(Button btn, Class<?> targetActivity)
    {
        btn.setOnClickListener(view -> {
            if (uri_image_first == null || uri_image_second == null) {
                return;
            }

            Intent intent = new Intent(getApplicationContext(), targetActivity);
            intent.putExtra(KEY_URI_IMAGE_FIRST, uri_image_first.toString());
            intent.putExtra(KEY_URI_IMAGE_SECOND, uri_image_second.toString());
            startActivity(intent);
        });
    }

    private void addLoadImageLogic(ImageView imageView, String image) {
        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        return;
                    }

                    // TODO this is ugly
                    if (image.equals(KEY_URI_IMAGE_FIRST)) {
                        uri_image_first = uri;
                    } else {
                        uri_image_second = uri;
                    }
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
            uri_image_first = Uri.parse(savedInstanceState.getString(KEY_URI_IMAGE_FIRST));
            imageView.setImageURI(uri_image_first);
        }

        if (savedInstanceState.getString(KEY_URI_IMAGE_SECOND) != null) {
            ImageView imageView = findViewById(R.id.home_image_second);
            uri_image_second = Uri.parse(savedInstanceState.getString(KEY_URI_IMAGE_SECOND));
            imageView.setImageURI(uri_image_second);
        }
    }


    // invoked when the activity may be temporarily destroyed, save the instance state here
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (uri_image_first != null) {
            outState.putString(KEY_URI_IMAGE_FIRST, uri_image_first.toString());
        }

        if (uri_image_second != null) {
            outState.putString(KEY_URI_IMAGE_SECOND, uri_image_second.toString());
        }

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }
}