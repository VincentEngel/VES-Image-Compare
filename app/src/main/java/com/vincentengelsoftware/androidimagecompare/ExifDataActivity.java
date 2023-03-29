package com.vincentengelsoftware.androidimagecompare;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.vincentengelsoftware.androidimagecompare.helper.MetaDataExtractor;

public class ExifDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exif_data);

        String[] a = MetaDataExtractor.getMetaData(this.getContentResolver(), MainActivity.leftImageUri);
    }
}