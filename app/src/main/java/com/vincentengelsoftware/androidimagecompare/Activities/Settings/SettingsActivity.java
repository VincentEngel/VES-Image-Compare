package com.vincentengelsoftware.androidimagecompare.Activities.Settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.vincentengelsoftware.androidimagecompare.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        try {
            PackageInfo pinfo = getPackageInfo();
            String version = "v" + pinfo.versionName;
            TextView versionText = findViewById(R.id.settings_version);
            versionText.setText(version);
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("deprecation")
    private PackageInfo getPackageInfo() throws PackageManager.NameNotFoundException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return getPackageManager().getPackageInfo(getPackageName(), PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA));
        }

        return getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
    }
}