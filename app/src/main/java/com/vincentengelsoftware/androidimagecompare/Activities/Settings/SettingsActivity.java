package com.vincentengelsoftware.androidimagecompare.Activities.Settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

        setUpThemeToggleButton(findViewById(R.id.home_theme));

        EditText maxZoom = findViewById(R.id.settings_max_zoom);
        maxZoom.setText(String.valueOf(this.userSettings.getMaxZoom()));

        Button saveButton = findViewById(R.id.settings_save);
        saveButton.setOnClickListener(view -> {
            this.userSettings.setMaxZoom(Integer.parseInt(maxZoom.getText().toString()));

            Toast.makeText(getApplicationContext(), "Successfully saved", Toast.LENGTH_LONG).show();
        });
    }

    private void setUpThemeToggleButton(Button button)
    {
        Theme.updateButtonText(button, this.userSettings.getTheme());

        button.setOnClickListener(view -> {
            this.userSettings.setTheme((this.userSettings.getTheme() + 1) % 3);
            Theme.updateButtonText(button, this.userSettings.getTheme());
            Theme.updateTheme(this.userSettings.getTheme());
        });
    }
}