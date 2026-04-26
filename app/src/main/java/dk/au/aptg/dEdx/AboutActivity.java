package dk.au.aptg.dEdx;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        String libVersion = new DedxAPI().dedxGetVersion();
        String appVersion = BuildConfig.VERSION_NAME;

        TextView versionText = findViewById(R.id.version_text);
        versionText.setText("App version:    " + appVersion + "\nlibdedx version: " + libVersion);
    }
}
