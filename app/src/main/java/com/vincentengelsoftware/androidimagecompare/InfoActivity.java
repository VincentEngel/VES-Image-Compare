package com.vincentengelsoftware.androidimagecompare;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView textViewGithub = findViewById(R.id.info_text_view_link_github_project);
        textViewGithub.setMovementMethod(LinkMovementMethod.getInstance());

        TextView textViewLibrary = findViewById(R.id.info_text_view_link_library);
        textViewLibrary.setMovementMethod(LinkMovementMethod.getInstance());

        TextView textViewColorTheme = findViewById(R.id.info_text_view_link_color_theme);
        textViewColorTheme.setMovementMethod(LinkMovementMethod.getInstance());

        TextView textViewAppIcon = findViewById(R.id.info_text_view_link_app_icon);
        textViewAppIcon.setMovementMethod(LinkMovementMethod.getInstance());
    }
}