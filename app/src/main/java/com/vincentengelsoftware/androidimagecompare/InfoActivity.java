package com.vincentengelsoftware.androidimagecompare;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.vincentengelsoftware.androidimagecompare.globals.Status;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Status.activityIsOpening = false;
        setContentView(R.layout.activity_info);

        TextView textViewGithub = findViewById(R.id.info_text_view_link_github_project);
        textViewGithub.setMovementMethod(LinkMovementMethod.getInstance());

        TextView textViewLibrary = findViewById(R.id.info_text_view_link_library);
        textViewLibrary.setMovementMethod(LinkMovementMethod.getInstance());

        TextView textViewColorTheme = findViewById(R.id.info_text_view_link_color_theme);
        textViewColorTheme.setMovementMethod(LinkMovementMethod.getInstance());

        TextView textViewAppIcon = findViewById(R.id.info_text_view_link_app_icon);
        textViewAppIcon.setMovementMethod(LinkMovementMethod.getInstance());

        TextView textViewDittoAppLink = findViewById(R.id.info_text_view_link_ditto_photo_comparer);
        textViewDittoAppLink.setMovementMethod(LinkMovementMethod.getInstance());

        TextView textViewPrivacyPolicy = findViewById(R.id.info_text_view_privacy_policy);
        textViewPrivacyPolicy.setText(Html.fromHtml(
                getString(R.string.privacy_policy),
                Html.FROM_HTML_MODE_LEGACY
        ));
        textViewPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
    }
}