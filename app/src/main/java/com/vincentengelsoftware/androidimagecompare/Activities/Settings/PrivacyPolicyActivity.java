package com.vincentengelsoftware.androidimagecompare.Activities.Settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.helper.TextViewModifier;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        TextView textViewPrivacyPolicy = findViewById(R.id.privacy_policy_text);
        textViewPrivacyPolicy.setText(Html.fromHtml(
                getString(R.string.privacy_policy),
                Html.FROM_HTML_MODE_LEGACY
        ));
        TextViewModifier.makeLinkClickable(textViewPrivacyPolicy);
    }
}