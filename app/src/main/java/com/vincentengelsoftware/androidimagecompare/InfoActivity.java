package com.vincentengelsoftware.androidimagecompare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;

import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.TextViewModifier;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Status.activityIsOpening = false;
        setContentView(R.layout.activity_info);

        TextViewModifier.makeLinkClickable(findViewById(R.id.info_text_view_link_github_project));
        TextViewModifier.makeLinkClickable(findViewById(R.id.info_text_view_link_library));
        TextViewModifier.makeLinkClickable(findViewById(R.id.info_text_view_link_color_theme));
        TextViewModifier.makeLinkClickable(findViewById(R.id.info_text_view_link_google_material_icons));
        TextViewModifier.makeLinkClickable(findViewById(R.id.info_text_view_link_ditto_photo_comparer));

        TextView textViewPrivacyPolicy = findViewById(R.id.info_text_view_privacy_policy);
        textViewPrivacyPolicy.setText(Html.fromHtml(
                getString(R.string.privacy_policy),
                Html.FROM_HTML_MODE_LEGACY
        ));
        TextViewModifier.makeLinkClickable(textViewPrivacyPolicy);

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

        String version = "Unbekannt";
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
            version = "v" + pinfo.versionName;
        } catch (Exception ignored) {
        }

        TextView versionText = findViewById(R.id.info_version);
        versionText.setText(version);
    }
}