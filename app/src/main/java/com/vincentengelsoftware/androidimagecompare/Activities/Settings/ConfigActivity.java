package com.vincentengelsoftware.androidimagecompare.Activities.Settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.globals.Status;

public class ConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configs);
        Status.activityIsOpening = false;

        this.setVersionText();
    }

    private void setVersionText()
    {
        String version = getString(R.string.unknown);
        try {
            PackageInfo pinfo = getPackageInfo();
            version = "v" + pinfo.versionName;
        } catch (Exception ignored) {
        }

        TextView versionText = findViewById(R.id.settings_version);
        versionText.setText(version);

        Button privacyPolicyButton = findViewById(R.id.configs_action_privacy_policy);
        privacyPolicyButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });

        Button aboutButton = findViewById(R.id.configs_action_app_info);
        aboutButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        });

        Button settingsButton = findViewById(R.id.configs_action_settings);
        settingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        Button openPlaystore = findViewById(R.id.configs_open_playstore);
        openPlaystore.setOnClickListener(view -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" +getPackageName())));
            } catch (ActivityNotFoundException e1) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" +getPackageName())));
                } catch (ActivityNotFoundException ignored) {
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    private PackageInfo getPackageInfo() throws PackageManager.NameNotFoundException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return getPackageManager().getPackageInfo(getPackageName(), PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA));
        }

        return getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
    }
}