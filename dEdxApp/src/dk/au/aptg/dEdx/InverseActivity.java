package dk.au.aptg.dEdx;

import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class InverseActivity extends Activity {
	DedxAPI dEdx;
	Spinner programSpinner;
	DedxIdxNameAdapter programAdaptor;
	Spinner ionSpinner;
	DedxIdxNameAdapter ionAdaptor;
	Spinner materialSpinner;
	DedxIdxNameAdapter materialAdaptor;
	EditText atomValue;
	TextView textEnergy_i;
	TextView textCSDA;
	EditText textDensity;
	double result;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inverse_layout);

		dEdx = new DedxAPI();

		List<DedxIdxName> programList = dEdx.dedxGetProgramList();   
		programSpinner = (Spinner) findViewById(R.id.program_spinner_i);
		programAdaptor = new DedxIdxNameAdapter(this, programList);
		programSpinner.setAdapter(programAdaptor);
		programSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				updateSpinner(programAdaptor.getSelectedIdx(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				return;
			}
		});
		programSpinner.setSelection(1);
		programSpinner.setPrompt("PROGRAM");

		ionSpinner = (Spinner) findViewById(R.id.ion_spinner_i);
		ionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				updateAtom(ionAdaptor.getSelectedIdx(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				return;
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void updateDensity() {
		textDensity = (EditText) findViewById(R.id.density_value_i);
		
		int err = dEdx.dedxLoadConfig(getProgramIdx(), getMaterialIdx(), getIonIdx());

		if( err != 0) {
			printErr(err);
		} else {
			float density = dEdx.dedxGetDensity();
			textDensity.setText(String.format(Locale.US, "%e", density));
		}
	}

	private void updateAtom(int ion) {
		atomValue = (EditText) findViewById(R.id.atom_value_i);
		atomValue.setText(Integer.toString(dEdx.getAtomNum(ion)));
	}

	private void updateSpinner(int program) {
		List<DedxIdxName> ionList = dEdx.dedxGetIons(program);
		ionAdaptor = new DedxIdxNameAdapter(this, ionList);
		ionAdaptor.notifyDataSetChanged();
		ionSpinner = (Spinner) findViewById(R.id.ion_spinner_i);
		ionSpinner.setAdapter(ionAdaptor);
		ionSpinner.setPrompt("ION");

		List<DedxIdxName> materialList = dEdx.dedxGetMaterials(program);
		materialAdaptor = new DedxIdxNameAdapter(this, materialList);
		materialAdaptor.notifyDataSetChanged();
		materialSpinner = (Spinner) findViewById(R.id.material_spinner_i);
		materialSpinner.setAdapter(materialAdaptor);
		materialSpinner.setPrompt("TARGET");

		int index = 1;
		for(int i = 0; i < materialList.size(); i++) {
			if(materialList.get(i).getName().equals("WATER")) {
				index = i;
			}				
		}
		materialSpinner.setSelection(index);
		materialSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				updateDensity();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				return;
			}
		});
	}

	private int getProgramIdx() {
		return programAdaptor.getSelectedIdx(programSpinner.getSelectedItemPosition());
	}
	private int getIonIdx() {
		return ionAdaptor.getSelectedIdx(ionSpinner.getSelectedItemPosition());
	}
	private int getMaterialIdx() {
		return materialAdaptor.getSelectedIdx(materialSpinner.getSelectedItemPosition());
	}

	private void printErr(int err) {
		Toast.makeText(getApplicationContext(), dEdx.dedxGetErrorMsg(err),
				Toast.LENGTH_LONG).show();
	}
	
	public void changeUnitsRange(View v) {
		textCSDA = (TextView) findViewById(R.id.range_value);
		textDensity = (EditText) findViewById(R.id.density_value_i);
		TextView textUnit = (TextView) findViewById(R.id.unitTypeRange_i);
		float range = 0;

		if(!textCSDA.getText().toString().equals(""))
			range = Float.valueOf(textCSDA.getText().toString());

		if(!textDensity.getText().toString().equals("")) {
			float rho = Float.valueOf(textDensity.getText().toString());

			if(textUnit.getText().toString().equals("g/cm\u00B2")) {
				textCSDA.setText(String.format(Locale.US, "%4.3e", range / rho));
				textUnit.setText("cm");
			} else {
				textCSDA.setText(String.format(Locale.US, "%4.3e", range * rho));	
				textUnit.setText("g/cm\u00B2");
			}
		} else {
			Toast.makeText(getApplicationContext(), "Missing density of target", Toast.LENGTH_SHORT).show();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void copyResult(View v) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(Double.toString(result));
		} else {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); 
			android.content.ClipData clip = android.content.ClipData.newPlainText("copyRes", Double.toString(result));
			clipboard.setPrimaryClip(clip);
		}
		Toast.makeText(getApplicationContext(), "Result copied to clipboard",
				Toast.LENGTH_SHORT).show();
	}

	public void calcInverseCSDA(View v) {
		textCSDA = (EditText) findViewById(R.id.range_value);
		textEnergy_i = (TextView) findViewById(R.id.energy_text_i);
		TextView textUnit = (TextView) findViewById(R.id.unitTypeRange_i);
		textDensity = (EditText) findViewById(R.id.density_value_i);
	
		float stp;
		int atomNum;
		float rho;

		int err = dEdx.dedxLoadConfig(getProgramIdx(), getMaterialIdx(), getIonIdx());

		if( err != 0) {
			printErr(err);
			textEnergy_i.setText("Error");
		} else {
			if(!textCSDA.getText().toString().isEmpty()) {
				stp = Float.parseFloat((textCSDA.getText().toString()));
			} else {
				stp = 0;
				textCSDA.setText("0");
			}

			if(!atomValue.getText().toString().isEmpty()) {
				atomNum = Integer.valueOf(atomValue.getText().toString());
			} else {
				atomNum = 1;
				atomValue.setText("1");
			}
			if(!textDensity.getText().toString().equals("")) {
				rho = Float.valueOf(textDensity.getText().toString());

				if(textUnit.getText().toString().equals("cm"))
					stp = stp * rho;

				double energy = dEdx.dedxGetInverseCSDA(stp, atomNum);

				if(stp >= 0) {
					result = energy;
					textEnergy_i.setText(String.format(Locale.US, "%4.3e", energy));
				} else {
					printErr((int)(-1*stp));
					textEnergy_i.setText("Error");
				}		
			} else {
				Toast.makeText(getApplicationContext(), "Missing density of target", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
