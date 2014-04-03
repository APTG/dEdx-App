package dk.au.aptg.dEdx;
 
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
 
public class MainActivity extends TabActivity {
	DedxAPI dEdx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        dEdx = new DedxAPI(getApplicationContext());
         
        TabHost tabHost = getTabHost();
         
        // Tab for dEdx
        TabSpec dEdxtab = tabHost.newTabSpec("dE/dx");
        // setting Title and Icon for the Tab
        dEdxtab.setIndicator(makeDEdxTab());
        Intent dEdxIntent = new Intent(this, DedxActivity.class);
        dEdxtab.setContent(dEdxIntent);
         
        // Tab for inverse dEdx
        TabSpec inversetab = tabHost.newTabSpec("Inverse");        
        inversetab.setIndicator(makeInvTab());
        Intent inverseIntent = new Intent(this, InverseActivity.class);
        inversetab.setContent(inverseIntent);
         
        // Adding all TabSpec to TabHost
        tabHost.addTab(dEdxtab);
        tabHost.addTab(inversetab);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	startActivity(new Intent(this, AboutActivity.class));
    	return super.onOptionsItemSelected(item);
    }
    
	@Override
	protected void onStop() {
		super.onStop();
		dEdx.dedxExit();
	}
    
    private TextView makeDEdxTab() {
    	TextView tabView = new TextView(this);
    	tabView.setBackgroundDrawable(getResources().getDrawable(R.drawable.dedx_tab));
    	return tabView;
    }
    private TextView makeInvTab() {
    	TextView tabView = new TextView(this);
    	tabView.setBackgroundDrawable(getResources().getDrawable(R.drawable.inverse_tab));
    	return tabView;
    }
}