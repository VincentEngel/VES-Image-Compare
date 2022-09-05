package com.vincentengelsoftware.androidimagecompare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.InfoHelper;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Status.activityIsOpening = false;
        setContentView(R.layout.activity_info);

        InfoHelper.makeLinkClickable(findViewById(R.id.info_text_view_link_github_project));
        InfoHelper.makeLinkClickable(findViewById(R.id.info_text_view_link_library));
        InfoHelper.makeLinkClickable(findViewById(R.id.info_text_view_link_color_theme));
        InfoHelper.makeLinkClickable(findViewById(R.id.info_text_view_link_app_icon));
        InfoHelper.makeLinkClickable(findViewById(R.id.info_text_view_link_ditto_photo_comparer));

        TextView textViewPrivacyPolicy = findViewById(R.id.info_text_view_privacy_policy);
        textViewPrivacyPolicy.setText(Html.fromHtml(
                getString(R.string.privacy_policy),
                Html.FROM_HTML_MODE_LEGACY
        ));
        InfoHelper.makeLinkClickable(textViewPrivacyPolicy);

        Button playStore = findViewById(R.id.info_btn_open_playstore);
        playStore.setOnClickListener(view -> {
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
}