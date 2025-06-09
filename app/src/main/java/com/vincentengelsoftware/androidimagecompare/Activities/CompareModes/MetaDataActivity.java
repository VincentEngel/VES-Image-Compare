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
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityMetaDataBinding;
import com.vincentengelsoftware.androidimagecompare.services.MetaData.Preparer;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class MetaDataActivity extends AppCompatActivity {

    private ActivityMetaDataBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Status.activityIsOpening = false;

        binding = ActivityMetaDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        try {
            Images.first.updateImageViewPreviewImage(binding.metaDataImageFirst);
            binding.metaDataImageNameFirst.setText(Images.first.getImageName());

            Images.second.updateImageViewPreviewImage(binding.metaDataImageSecond);
            binding.metaDataImageNameSecond.setText(Images.second.getImageName());
        } catch (Exception e) {
            this.finish();
        }

        Thread t = new Thread(() -> {
            runOnUiThread(() -> binding.metaDataSpinner.setVisibility(View.VISIBLE));

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

                if (groupNames.length == 0) {
                    runOnUiThread(() -> {
                        binding.MetaDataTable.removeAllViews();
                        binding.metaDataSpinner.setVisibility(View.GONE);
                    });
                    return;
                }

                for (String groupName : groupNames) {
                    runOnUiThread(() -> {
                        // Add group name rows dynamically using binding.MetaDataTable
                    });

                    valueNames = Preparer.getSortedKeys(metaDataFirst.get(groupName).keySet());

                    for (String valueName : valueNames) {
                        String valueFirst = metaDataFirst.get(groupName).get(valueName);
                        String valueSecond = metaDataSecond.get(groupName).get(valueName);

                        runOnUiThread(() -> {
                            // Add value rows dynamically using binding.MetaDataTable
                        });
                    }
                }
            } catch (Exception ignored) {
            }

            runOnUiThread(() -> binding.metaDataSpinner.setVisibility(View.GONE));
        });

        t.start();
    }
}