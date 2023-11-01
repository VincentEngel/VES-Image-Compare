package com.vincentengelsoftware.androidimagecompare.Activities.Settings;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
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
        this.setUpThemeToggleButton(findViewById(R.id.home_theme));

        EditText maxZoom = findViewById(R.id.settings_max_zoom);

        Button saveButton = findViewById(R.id.settings_save);
        saveButton.setOnClickListener(view -> {
            int maxZoomValue = Integer.parseInt(maxZoom.getText().toString());
            if (maxZoomValue < 1) {
                maxZoomValue = 1;
                Toast.makeText(getApplicationContext(), getString(R.string.error_msg_invalid_input_number_gt_zero), Toast.LENGTH_LONG).show();
            }
            this.userSettings.setMaxZoom(maxZoomValue);

            this.applyCurrentSettings();

            Toast.makeText(getApplicationContext(), getString(R.string.settings_save_success), Toast.LENGTH_LONG).show();
        });

        SwitchMaterial resetZoomOnLinking = findViewById(R.id.settings_switch_reset_zoom_on_linking);
        resetZoomOnLinking.setOnClickListener(view -> {
            this.userSettings.setResetImageOnLinking(resetZoomOnLinking.isChecked());
        });

        TextView mirroringDescription = findViewById(R.id.settings_mirroring_explanation);
        RadioButton naturalMirroring = findViewById(R.id.settings_mirroring_natural);
        naturalMirroring.setOnClickListener(view -> {
            this.userSettings.setMirroringType(Status.NATURAL_MIRRORING);
            mirroringDescription.setText(R.string.settings_mirroring_natural_description);
        });
        RadioButton strictMirroring = findViewById(R.id.settings_mirroring_strict);
        strictMirroring.setOnClickListener(view -> {
            this.userSettings.setMirroringType(Status.STRICT_MIRRORING);
            mirroringDescription.setText(R.string.settings_mirroring_strict_description);
        });
        RadioButton looseMirroring = findViewById(R.id.settings_mirroring_loose);
        looseMirroring.setOnClickListener(view -> {
            this.userSettings.setMirroringType(Status.LOOSE_MIRRORING);
            mirroringDescription.setText(R.string.settings_mirroring_loose_description);
        });

        TextView tapHideModeDescription = findViewById(R.id.settings_tap_hide_mode_description);
        RadioButton tapHideModeInvisible = findViewById(R.id.settings_tap_hide_mode_btn_invisible);
        tapHideModeInvisible.setOnClickListener(view -> {
            this.userSettings.setTypHideMode(Status.TAP_HIDE_MODE_INVISIBLE);
            tapHideModeDescription.setText(R.string.settings_tap_hide_mode_description_invisible);
        });
        RadioButton tapHideModeBackground = findViewById(R.id.settings_tap_hide_mode_btn_background);
        tapHideModeBackground.setOnClickListener(view -> {
            this.userSettings.setTypHideMode(Status.TAP_HIDE_MODE_BACKGROUND);
            tapHideModeDescription.setText(R.string.settings_tap_hide_mode_description_background);
        });

        Button resetButton = findViewById(R.id.settings_reset);
        resetButton.setOnClickListener(view -> {
            this.userSettings.resetAllSettings();

            this.applyCurrentSettings();

            Toast.makeText(getApplicationContext(), getString(R.string.settings_reset_success), Toast.LENGTH_LONG).show();
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

        this.applyMirroringSettings();
        this.applyTapModeSettings();
    }

    private void applyMirroringSettings()
    {
        RadioButton naturalMirroring = findViewById(R.id.settings_mirroring_natural);
        RadioButton strictMirroring = findViewById(R.id.settings_mirroring_strict);
        RadioButton looseMirroring = findViewById(R.id.settings_mirroring_loose);

        TextView mirroringDescription = findViewById(R.id.settings_mirroring_explanation);
        if (this.userSettings.getMirroringType() == Status.NATURAL_MIRRORING) {
            naturalMirroring.setChecked(true);
            strictMirroring.setChecked(false);
            looseMirroring.setChecked(false);
            mirroringDescription.setText(R.string.settings_mirroring_natural_description);
        }
        if (this.userSettings.getMirroringType() == Status.STRICT_MIRRORING) {
            naturalMirroring.setChecked(false);
            strictMirroring.setChecked(true);
            looseMirroring.setChecked(false);
            mirroringDescription.setText(R.string.settings_mirroring_strict_description);
        }
        if (this.userSettings.getMirroringType() == Status.LOOSE_MIRRORING) {
            naturalMirroring.setChecked(false);
            strictMirroring.setChecked(false);
            looseMirroring.setChecked(true);
            mirroringDescription.setText(R.string.settings_mirroring_loose_description);
        }
    }

    private void applyTapModeSettings()
    {
        TextView tapHideModeDescription = findViewById(R.id.settings_tap_hide_mode_description);
        RadioButton tapHideModeInvisible = findViewById(R.id.settings_tap_hide_mode_btn_invisible);
        RadioButton tapHideModeBackground = findViewById(R.id.settings_tap_hide_mode_btn_background);


        if (this.userSettings.getTypHideMode() == Status.TAP_HIDE_MODE_INVISIBLE) {
            tapHideModeInvisible.setChecked(true);
            tapHideModeBackground.setChecked(false);
            tapHideModeDescription.setText(R.string.settings_tap_hide_mode_description_invisible);
        }

        if (this.userSettings.getTypHideMode() == Status.TAP_HIDE_MODE_BACKGROUND) {
            tapHideModeInvisible.setChecked(false);
            tapHideModeBackground.setChecked(true);
            tapHideModeDescription.setText(R.string.settings_tap_hide_mode_description_background);
        }
    }

    private void setUpThemeToggleButton(Button buttonTheme)
    {
        buttonTheme.setOnClickListener(view -> {
            this.userSettings.setTheme((this.userSettings.getTheme() + 1) % 3);
            Theme.updateButtonText(buttonTheme, this.userSettings.getTheme());
            Theme.updateTheme(this.userSettings.getTheme());
        });
    }
}