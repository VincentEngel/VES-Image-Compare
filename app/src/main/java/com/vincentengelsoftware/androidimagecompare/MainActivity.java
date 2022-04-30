package com.vincentengelsoftware.androidimagecompare;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.vincentengelsoftware.androidimagecompare.helper.MainHelper;
import com.vincentengelsoftware.androidimagecompare.util.ImageHolder;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final String KEY_URI_IMAGE_FIRST = "key.uri.image.first";
    public static final String KEY_URI_IMAGE_SECOND = "key.uri.image.second";

    protected ImageHolder image_holder_first = new ImageHolder();
    protected ImageHolder image_holder_second = new ImageHolder();

    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpActivityButtons();

        addLoadImageLogic(
                findViewById(R.id.home_image_first),
                image_holder_first
        );

        addLoadImageLogic(
                findViewById(R.id.home_image_second),
                image_holder_second
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
                image_holder_first,
                image_holder_second,
                findViewById(R.id.home_image_first),
                findViewById(R.id.home_image_second)
        );
    }

    private void addButtonChangeActivityLogic(Button btn, Class<?> targetActivity)
    {
        btn.setOnClickListener(view -> {
            if (image_holder_first.uri == null || image_holder_second.uri == null) {
                return;
            }

            Intent intent = new Intent(getApplicationContext(), targetActivity);
            intent.putExtra(KEY_URI_IMAGE_FIRST, image_holder_first.uri.toString());
            intent.putExtra(KEY_URI_IMAGE_SECOND, image_holder_second.uri.toString());
            startActivity(intent);
        });
    }

    private void addLoadImageLogic(ImageView imageView, ImageHolder imageHolder) {
        ActivityResultLauncher<String> mGetContentGallery = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        return;
                    }

                    Point size = new Point();
                    getWindowManager().getDefaultDisplay().getSize(size);
                    imageHolder.updateFromUri(uri, this.getContentResolver(), size);
                    imageView.setImageBitmap(imageHolder.bitmapSmall);
                });


        File temp = null;

        try {
            temp = File.createTempFile("camera_image", null, this.getCacheDir());
        } catch (Exception ignored) {
        }

        Uri contentUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", temp);

        ActivityResultLauncher<Uri> mGetContentCamera = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    Point size = new Point();
                    getWindowManager().getDefaultDisplay().getSize(size);
                    imageHolder.updateFromUri(contentUri, this.getContentResolver(), size);
                    imageView.setImageBitmap(imageHolder.bitmapSmall);
                }
        );

        imageView.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            final CharSequence[] optionsMenu = {"Take Photo", "Choose from Gallery", "Exit" };


            builder.setItems(optionsMenu, (dialogInterface, i) -> {
                if (optionsMenu[i].equals("Take Photo")) {
                    if (MainHelper.checkPermission(MainActivity.this)) {
                        mGetContentCamera.launch(contentUri);
                    } else {
                        MainHelper.requestPermission(MainActivity.this);
                    }
                } else if (optionsMenu[i].equals("Choose from Gallery")) {
                    mGetContentGallery.launch("image/*");
                }

                dialogInterface.dismiss();
            });

            builder.create().show();
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
            image_holder_first.uri = Uri.parse(savedInstanceState.getString(KEY_URI_IMAGE_FIRST));
            imageView.setImageBitmap(image_holder_first.bitmapSmall);
        }

        if (savedInstanceState.getString(KEY_URI_IMAGE_SECOND) != null) {
            ImageView imageView = findViewById(R.id.home_image_second);
            image_holder_second.uri = Uri.parse(savedInstanceState.getString(KEY_URI_IMAGE_SECOND));
            imageView.setImageBitmap(image_holder_second.bitmapSmall);
        }
    }


    // invoked when the activity may be temporarily destroyed, save the instance state here
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (image_holder_first.uri != null) {
            outState.putString(KEY_URI_IMAGE_FIRST, image_holder_first.uri.toString());
        }

        if (image_holder_second.uri != null) {
            outState.putString(KEY_URI_IMAGE_SECOND, image_holder_second.uri.toString());
        }

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }
}