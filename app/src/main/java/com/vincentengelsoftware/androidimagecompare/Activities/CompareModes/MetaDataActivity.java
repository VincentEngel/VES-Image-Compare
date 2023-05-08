package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.vincentengelsoftware.androidimagecompare.Activities.MainActivity;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.services.MetaData.Preparer;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class MetaDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Status.activityIsOpening = false;
        setContentView(R.layout.activity_meta_data);
        FullScreenHelper.setFullScreenFlags(this.getWindow());

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

            try {
                ArrayList<HashMap<String, HashMap<String, String>>> metData = Preparer.load(
                        this.getContentResolver(),
                        MainActivity.leftImageUri,
                        MainActivity.rightImageUri
                );

                HashMap<String, HashMap<String, String>> metaDataFirst = metData.get(0);
                HashMap<String, HashMap<String, String>> metaDataSecond = metData.get(1);

                String[] groupNames = Preparer.getSortedKeys(metaDataFirst.keySet());
                String[] valueNames;

                for (String groupName : groupNames) {
                    runOnUiThread(() -> {
                        TableLayout metaDataTable = findViewById(R.id.MetaDataTable);
                        TableRow titleRow = new TableRow(this);
                        TextView titleText = new TextView(this);
                        titleText.setTextAppearance(R.style.meta_data_title_text);

                        titleText.setText(groupName);
                        titleRow.addView(titleText);
                        metaDataTable.addView(titleRow);
                    });

                    valueNames = Preparer.getSortedKeys(metaDataFirst.get(groupName).keySet());

                    for (String valueName : valueNames) {
                        String valueFirst = metaDataFirst.get(groupName).get(valueName);
                        String valueSecond = metaDataSecond.get(groupName).get(valueName);

                        runOnUiThread(() -> {
                            TableLayout metaDataTable = findViewById(R.id.MetaDataTable);
                            TableRow subtitleRow = new TableRow(this);
                            TextView subtitleText = new TextView(this);
                            subtitleText.setTextAppearance(R.style.meta_data_subtitle_text);

                            subtitleText.setText(valueName);
                            subtitleRow.addView(subtitleText);
                            metaDataTable.addView(subtitleRow);

                            TableRow valueRow = new TableRow(this);
                            TextView valueTextFirst = new TextView(this);
                            TextView valueTextSecond = new TextView(this);

                            valueTextFirst.setText(valueFirst);
                            valueTextFirst.setTextAppearance(R.style.meta_data_value_text);
                            valueTextSecond.setText(valueSecond);
                            valueTextSecond.setTextAppearance(R.style.meta_data_value_text);

                            valueRow.addView(valueTextFirst);
                            valueRow.addView(valueTextSecond);
                            valueRow.setBackgroundResource(R.drawable.meta_data_table_row_value_border_bottom);
                            metaDataTable.addView(valueRow);
                        });
                    }
                }
            } catch (Exception ignored) {
            }

            runOnUiThread(() -> {
                ProgressBar spinner = findViewById(R.id.meta_data_spinner);
                spinner.setVisibility(View.GONE);
            });
        });

        t.start();
    }
}