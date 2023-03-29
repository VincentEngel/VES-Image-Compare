package com.vincentengelsoftware.androidimagecompare;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.vincentengelsoftware.androidimagecompare.globals.Images;

public class MetaDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meta_data);

        Images.first.updateImageViewPreviewImage(findViewById(R.id.meta_data_image_first));
        TextView imageNameFirst = findViewById(R.id.meta_data_image_name_first);
        imageNameFirst.setText(Images.first.getImageName());

        Images.second.updateImageViewPreviewImage(findViewById(R.id.meta_data_image_second));
        TextView imageNameSecond = findViewById(R.id.meta_data_image_name_second);
        imageNameSecond.setText(Images.second.getImageName());

        Thread t = new Thread(() -> {
            runOnUiThread(() -> {
                ProgressBar spinner = findViewById(R.id.meta_data_spinner);
                spinner.setVisibility(View.VISIBLE);
            });

            // TODO load MetaData and present it

            runOnUiThread(() -> {
                ProgressBar spinner = findViewById(R.id.meta_data_spinner);
                spinner.setVisibility(View.GONE);
            });
        });

        t.start();
    }
}