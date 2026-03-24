package com.vincentengelsoftware.androidimagecompare.ui.settings;

import android.os.Bundle;
import android.text.Html;
import androidx.appcompat.app.AppCompatActivity;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityPrivacyPolicyBinding;
import com.vincentengelsoftware.androidimagecompare.ui.util.TextViewModifier;

/** Displays the app's privacy policy as formatted HTML and makes any embedded links clickable. */
public class PrivacyPolicyActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ActivityPrivacyPolicyBinding binding =
        ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    binding.privacyPolicyText.setText(
        Html.fromHtml(getString(R.string.privacy_policy), Html.FROM_HTML_MODE_LEGACY));
    TextViewModifier.makeLinkClickable(binding.privacyPolicyText);
  }
}
