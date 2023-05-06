package com.vincentengelsoftware.androidimagecompare.Activities;

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

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.TextViewModifier;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Status.activityIsOpening = false;
        setContentView(R.layout.activity_info);

        TextViewModifier.makeLinkClickable(findViewById(R.id.info_text_view_link_github_project));
        TextViewModifier.makeLinkClickable(findViewById(R.id.info_text_view_link_library_subsampling_scale_image_view));
        TextViewModifier.makeLinkClickable(findViewById(R.id.info_text_view_link_library_metadata_extractor));
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
            PackageInfo pinfo = getPackageInfo();
            version = "v" + pinfo.versionName;
        } catch (Exception ignored) {
        }

        TextView versionText = findViewById(R.id.info_version);
        versionText.setText(version);
    }

    @SuppressWarnings("deprecation")
    private PackageInfo getPackageInfo() throws PackageManager.NameNotFoundException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return getPackageManager().getPackageInfo(getPackageName(), PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA));
        }

        return getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
    }
}