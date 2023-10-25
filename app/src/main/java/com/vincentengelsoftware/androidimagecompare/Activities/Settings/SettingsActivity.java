package com.vincentengelsoftware.androidimagecompare.Activities.Settings;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.Theme;
import com.vincentengelsoftware.androidimagecompare.services.KeyValueStorage;
import com.vincentengelsoftware.androidimagecompare.services.Settings.UserSettings;

public class SettingsActivity extends AppCompatActivity {
    private UserSettings userSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.userSettings = UserSettings.getInstance(new KeyValueStorage(getApplicationContext()));
        Theme.updateTheme(userSettings.getTheme());
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        Status.activityIsOpening = false;

        this.setUp();

        this.applyCurrentSettings();
    }

    private void setUp()
    {
        this.setUpThemeToggleButton(findViewById(R.id.home_theme), findViewById(R.id.settings_next_theme));

        EditText maxZoom = findViewById(R.id.settings_max_zoom);

        Button saveButton = findViewById(R.id.settings_save);
        saveButton.setOnClickListener(view -> {
            int maxZoomValue = Integer.parseInt(maxZoom.getText().toString());
            if (maxZoomValue < 1) {
                maxZoomValue = 1;
                Toast.makeText(getApplicationContext(), "Max Zoom needs to be greater than 0", Toast.LENGTH_LONG).show();
            }
            this.userSettings.setMaxZoom(maxZoomValue);

            this.applyCurrentSettings();

            Toast.makeText(getApplicationContext(), "Successfully saved", Toast.LENGTH_LONG).show();
        });

        SwitchMaterial resetZoomOnLinking = findViewById(R.id.settings_switch_reset_zoom_on_linking);
        resetZoomOnLinking.setOnClickListener(view -> {
            this.userSettings.setResetImageOnLinking(resetZoomOnLinking.isChecked());
        });

        SwitchMaterial looseMirroring = findViewById(R.id.settings_switch_loose_mirroring);
        looseMirroring.setOnClickListener(view -> {
            this.userSettings.setLooseMirroring(looseMirroring.isChecked());
        });

        Button resetButton = findViewById(R.id.settings_reset);
        resetButton.setOnClickListener(view -> {
            this.userSettings.resetAllSettings();

            this.applyCurrentSettings();

            Toast.makeText(getApplicationContext(), "Successfully reset all settings", Toast.LENGTH_LONG).show();
            recreate();
        });
    }

    private void applyCurrentSettings()
    {
        EditText maxZoom = findViewById(R.id.settings_max_zoom);
        maxZoom.setText(String.valueOf(this.userSettings.getMaxZoom()));

        Button themeButton = findViewById(R.id.home_theme);
        Theme.updateButtonText(themeButton, this.userSettings.getTheme());

        SwitchMaterial resetZoomOnLinking = findViewById(R.id.settings_switch_reset_zoom_on_linking);
        resetZoomOnLinking.setChecked(this.userSettings.getResetImageOnLink());

        SwitchMaterial looseMirroring = findViewById(R.id.settings_switch_loose_mirroring);
        looseMirroring.setChecked(this.userSettings.getLooseMirroring());
    }

    private void setUpThemeToggleButton(Button buttonTheme, ImageButton nextTheme)
    {
        nextTheme.setOnClickListener(view -> {
            this.userSettings.setTheme((this.userSettings.getTheme() + 1) % 3);
            Theme.updateButtonText(buttonTheme, this.userSettings.getTheme());
            Theme.updateTheme(this.userSettings.getTheme());
        });
    }
}