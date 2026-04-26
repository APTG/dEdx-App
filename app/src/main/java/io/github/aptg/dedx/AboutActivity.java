package io.github.aptg.dedx;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        TextView aboutText = findViewById(R.id.about_text);
        aboutText.setText(HtmlCompat.fromHtml(getString(R.string.about), HtmlCompat.FROM_HTML_MODE_LEGACY));
        aboutText.setMovementMethod(LinkMovementMethod.getInstance());

        String libVersion = new DedxAPI().dedxGetVersion();
        String appVersion = BuildConfig.VERSION_NAME;

        TextView versionText = findViewById(R.id.version_text);
        versionText.setText("App version:     " + appVersion + "\nlibdedx version: " + libVersion);
    }
}
