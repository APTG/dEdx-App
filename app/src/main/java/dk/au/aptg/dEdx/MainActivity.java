package dk.au.aptg.dEdx;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    DedxAPI dEdx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            dEdx = new DedxAPI(getApplicationContext());
        } catch (RuntimeException e) {
            Log.e("MainActivity", "Failed to initialize libdedx", e);
            Toast.makeText(this, "Failed to initialize: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new DedxPagerAdapter(this));
        viewPager.setUserInputEnabled(false);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                tab.setText(position == 0 ? "dE/dx" : "Inverse")
        ).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dEdx.dedxExit();
    }

    static class DedxPagerAdapter extends FragmentStateAdapter {
        DedxPagerAdapter(FragmentActivity fa) { super(fa); }

        @Override
        public int getItemCount() { return 2; }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? new DedxFragment() : new InverseFragment();
        }
    }
}
