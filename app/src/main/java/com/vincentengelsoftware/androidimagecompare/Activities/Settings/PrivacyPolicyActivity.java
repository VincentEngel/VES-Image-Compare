package com.vincentengelsoftware.androidimagecompare.Activities.Settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityPrivacyPolicyBinding;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.TextViewModifier;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityPrivacyPolicyBinding binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Status.activityIsOpening = false;

        binding.privacyPolicyText.setText(Html.fromHtml(
                getString(R.string.privacy_policy),
                Html.FROM_HTML_MODE_LEGACY
        ));
        TextViewModifier.makeLinkClickable(binding.privacyPolicyText);
    }
}