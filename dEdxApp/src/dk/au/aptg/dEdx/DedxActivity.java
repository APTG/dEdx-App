package dk.au.aptg.dEdx;

import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.DeniedByServerException;
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

public class DedxActivity extends Activity {
	//Data variables
	DedxAPI dEdx;
	Spinner programSpinner;
	DedxIdxNameAdapter programAdaptor;
	Spinner ionSpinner;
	DedxIdxNameAdapter ionAdaptor;
	Spinner materialSpinner;
	DedxIdxNameAdapter materialAdaptor;
	EditText atomValue;
	EditText textEnergy;
	TextView textStp;
	TextView textCSDA;
	EditText textDensity;
	float resSTP;
	double resCSDA;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dedx_layout);

		dEdx = new DedxAPI();

		List<DedxIdxName> programList = dEdx.dedxGetProgramList();   
		programSpinner = (Spinner) findViewById(R.id.program_spinner);
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

		ionSpinner = (Spinner) findViewById(R.id.ion_spinner);
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
		textDensity = (EditText) findViewById(R.id.density_value);
		
		int err = dEdx.dedxLoadConfig(getProgramIdx(), getMaterialIdx(), getIonIdx());

		if( err != 0) {
			printErr(err);
		} else {
			float density = dEdx.dedxGetDensity();
			textDensity.setText(String.format(Locale.US, "%e", density));
		}
	}

	private void updateAtom(int ion) {
		atomValue = (EditText) findViewById(R.id.atom_value);
		atomValue.setText(Integer.toString(dEdx.getAtomNum(ion)));
	}

	private void updateSpinner(int program) {
		List<DedxIdxName> ionList = dEdx.dedxGetIons(program);
		ionAdaptor = new DedxIdxNameAdapter(this, ionList);
		ionAdaptor.notifyDataSetChanged();
		ionSpinner = (Spinner) findViewById(R.id.ion_spinner);
		ionSpinner.setAdapter(ionAdaptor);
		ionSpinner.setPrompt("ION");

		List<DedxIdxName> materialList = dEdx.dedxGetMaterials(program);		
		materialAdaptor = new DedxIdxNameAdapter(this, materialList);
		materialAdaptor.notifyDataSetChanged();
		materialSpinner = (Spinner) findViewById(R.id.material_spinner);
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
		textCSDA = (TextView) findViewById(R.id.csda_text);
		textDensity = (EditText) findViewById(R.id.density_value);
		TextView textUnit = (TextView) findViewById(R.id.unitTypeRange);

		if(!textCSDA.getText().toString().equals("") && !textCSDA.getText().toString().equals("Error")) {
			float range = Float.valueOf(textCSDA.getText().toString());

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
		} else {
			Toast.makeText(getApplicationContext(), "Calculate CSDA Range first", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void changeUnits(View v) {
		textStp = (TextView) findViewById(R.id.stp_text);
		textDensity = (EditText) findViewById(R.id.density_value);
		TextView textUnit = (TextView) findViewById(R.id.unitType);

		if(!textStp.getText().toString().equals("") && !textStp.getText().toString().equals("Error")) {
			float stp = Float.valueOf(textStp.getText().toString());

			if(!textDensity.getText().toString().equals("")) {
				float rho = Float.valueOf(textDensity.getText().toString());

				if(textUnit.getText().toString().equals("MeV cm\u00B2/g")) {
					textStp.setText(String.format(Locale.US, "%4.3e", stp * rho));
					textUnit.setText("MeV/cm");
				} else if(textUnit.getText().toString().equals("MeV/cm")) {
					textStp.setText(String.format(Locale.US, "%4.3e", 0.1 * stp));
					textUnit.setText("KeV/\u00b5m");
				} else {
					textStp.setText(String.format(Locale.US, "%4.3e", (stp / rho) / 0.1));
					textUnit.setText("MeV cm\u00B2/g");
				}
			} else {
				Toast.makeText(getApplicationContext(), "Missing density of target", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(getApplicationContext(), "Calculate Stopping Power first", Toast.LENGTH_SHORT).show();
		}
	}

	public void copySTP(View v) {
		copyResult(Float.toString(resSTP));
	}
	
	public void copyCSDA(View v) {
		copyResult(Double.toString(resCSDA));
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void copyResult(String res) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
		    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		    clipboard.setText(res);
		} else {
		    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); 
		    android.content.ClipData clip = android.content.ClipData.newPlainText("copyRes", res);
		    clipboard.setPrimaryClip(clip);
		}
		Toast.makeText(getApplicationContext(), "Result copied to clipboard",
				Toast.LENGTH_SHORT).show();
	}

	public void calcStp(View view) {
		textEnergy = (EditText) findViewById(R.id.energy_value);
		textStp = (TextView) findViewById(R.id.stp_text);
		TextView textUnit = (TextView) findViewById(R.id.unitType);
		
		float energy;

		int err = dEdx.dedxLoadConfig(getProgramIdx(), getMaterialIdx(), getIonIdx());

		if( err != 0) {
			printErr(err);
			textStp.setText("Error");
		} else {
			if(!textEnergy.getText().toString().isEmpty()) {
				energy = Float.parseFloat((textEnergy.getText().toString()));
			} else {
				energy = 0;
				textEnergy.setText("0");
			}

			float stp = dEdx.dedxGetStp(energy);

			if(stp > 0) {
				resSTP = stp;
				textStp.setText(String.format(Locale.US, "%4.3e", stp));
				textUnit.setText("MeV cm\u00B2/g");
				
			} else {
				printErr((int)(-1*stp));
				textStp.setText("Error");
			}			
		}
	}

	public void calcCSDA(View view) {
		textEnergy = (EditText) findViewById(R.id.energy_value);
		textCSDA = (TextView) findViewById(R.id.csda_text);
		TextView textUnit = (TextView) findViewById(R.id.unitTypeRange);
		float energy;
		int atomNum;

		int err = dEdx.dedxLoadConfig(getProgramIdx(), getMaterialIdx(), getIonIdx());

		if( err != 0) {
			printErr(err);
			textCSDA.setText("Error");
		} else {
			if(!textEnergy.getText().toString().isEmpty()) {
				energy = Float.parseFloat((textEnergy.getText().toString()));
			} else {
				energy = 0;
				textEnergy.setText("0");
			}
			
			if(!atomValue.getText().toString().isEmpty()) {
				atomNum = Integer.valueOf(atomValue.getText().toString());
			} else {
				atomNum = 1;
				atomValue.setText("1");
			}
			
			if(energy <= 0 || energy >= 10106) {
				Toast.makeText(getApplicationContext(), "Energy limit to low or high",
						Toast.LENGTH_LONG).show();
				textCSDA.setText("Error");
			} else {
				double csda= dEdx.dedxGetCSDARange(energy, atomNum);

				if(csda > 0) {
					resCSDA = csda;
					textCSDA.setText(String.format(Locale.US, "%4.3e", csda));
					textUnit.setText("g/cm\u00B2");
				} else {
					printErr((int)(-1*csda));
					textCSDA.setText("Error");
				}
			}			
		}
	}
}
