package dk.au.aptg.dEdx;

import java.util.List;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class DedxFragment extends Fragment {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dedx_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // No-arg constructor intentional: MainActivity has already called DedxAPI(Context)
        // which runs Utils.copyToAssets() and dedxInit(). Fragments only use the native state.
        // TODO(11-4): replace with internal storage init so this dependency is explicit.
        dEdx = new DedxAPI();

        List<DedxIdxName> programList = dEdx.dedxGetProgramList();
        programSpinner = view.findViewById(R.id.program_spinner);
        programAdaptor = new DedxIdxNameAdapter(requireContext(), programList);
        programSpinner.setAdapter(programAdaptor);
        programSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                updateSpinner(programAdaptor.getSelectedIdx(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        programSpinner.setSelection(1);
        programSpinner.setPrompt("PROGRAM");

        ionSpinner = view.findViewById(R.id.ion_spinner);
        ionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                updateAtom(ionAdaptor.getSelectedIdx(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        view.findViewById(R.id.calc_stp).setOnClickListener(this::calcStp);
        view.findViewById(R.id.unitType).setOnClickListener(this::changeUnits);
        view.findViewById(R.id.unitTypeRange).setOnClickListener(this::changeUnitsRange);
        view.findViewById(R.id.stp_text).setOnClickListener(this::copySTP);
        view.findViewById(R.id.csda_text).setOnClickListener(this::copyCSDA);
    }

    private void updateDensity() {
        textDensity = requireView().findViewById(R.id.density_value);
        int err = dEdx.dedxLoadConfig(getProgramIdx(), getMaterialIdx(), getIonIdx());
        if (err != 0) {
            printErr(err);
        } else {
            float density = dEdx.dedxGetDensity();
            textDensity.setText(dEdx.printFloatDensity(density));
        }
    }

    private void updateAtom(int ion) {
        atomValue = requireView().findViewById(R.id.atom_value);
        atomValue.setText(Integer.toString(dEdx.getAtomNum(ion)));
    }

    private void updateSpinner(int program) {
        List<DedxIdxName> ionList = dEdx.dedxGetIons(program);
        ionAdaptor = new DedxIdxNameAdapter(requireContext(), ionList);
        ionAdaptor.notifyDataSetChanged();
        ionSpinner = requireView().findViewById(R.id.ion_spinner);
        ionSpinner.setAdapter(ionAdaptor);
        ionSpinner.setPrompt("ION");

        List<DedxIdxName> materialList = dEdx.dedxGetMaterials(program);
        materialAdaptor = new DedxIdxNameAdapter(requireContext(), materialList);
        materialAdaptor.notifyDataSetChanged();
        materialSpinner = requireView().findViewById(R.id.material_spinner);
        materialSpinner.setAdapter(materialAdaptor);
        materialSpinner.setPrompt("TARGET");

        int index = 1;
        for (int i = 0; i < materialList.size(); i++) {
            if (materialList.get(i).getName().equals("WATER")) {
                index = i;
            }
        }
        materialSpinner.setSelection(index);
        materialSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                updateDensity();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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
        Toast.makeText(requireContext(), dEdx.dedxGetErrorMsg(err), Toast.LENGTH_LONG).show();
    }

    public void changeUnitsRange(View v) {
        textCSDA = requireView().findViewById(R.id.csda_text);
        textDensity = requireView().findViewById(R.id.density_value);
        TextView textUnit = requireView().findViewById(R.id.unitTypeRange);

        if (!textCSDA.getText().toString().equals("") && !textCSDA.getText().toString().equals("Error")) {
            float range = Float.valueOf(textCSDA.getText().toString());
            if (!textDensity.getText().toString().equals("")) {
                float rho = Float.valueOf(textDensity.getText().toString());
                if (textUnit.getText().toString().equals("g/cm\u00B2")) {
                    textCSDA.setText(dEdx.printFloat(range / rho));
                    textUnit.setText("cm");
                } else {
                    textCSDA.setText(dEdx.printFloat(range * rho));
                    textUnit.setText("g/cm\u00B2");
                }
            } else {
                Toast.makeText(requireContext(), "Missing density of target", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Calculate CSDA Range first", Toast.LENGTH_SHORT).show();
        }
    }

    public void changeUnits(View v) {
        textStp = requireView().findViewById(R.id.stp_text);
        textDensity = requireView().findViewById(R.id.density_value);
        TextView textUnit = requireView().findViewById(R.id.unitType);

        if (!textStp.getText().toString().equals("") && !textStp.getText().toString().equals("Error")) {
            float stp = Float.valueOf(textStp.getText().toString());
            if (!textDensity.getText().toString().equals("")) {
                float rho = Float.valueOf(textDensity.getText().toString());
                if (textUnit.getText().toString().equals("MeV cm\u00B2/g")) {
                    textStp.setText(dEdx.printFloat(stp * rho));
                    textUnit.setText("MeV/cm");
                } else if (textUnit.getText().toString().equals("MeV/cm")) {
                    textStp.setText(dEdx.printFloat((float) 0.1 * stp));
                    textUnit.setText("keV/\u00b5m");
                } else {
                    textStp.setText(dEdx.printFloat((stp / rho) / (float) 0.1));
                    textUnit.setText("MeV cm\u00B2/g");
                }
            } else {
                Toast.makeText(requireContext(), "Missing density of target", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Calculate Stopping Power first", Toast.LENGTH_SHORT).show();
        }
    }

    public void copySTP(View v) {
        copyResult(Float.toString(resSTP));
    }

    public void copyCSDA(View v) {
        copyResult(Double.toString(resCSDA));
    }

    public void copyResult(String res) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("copyRes", res));
        Toast.makeText(requireContext(), "Result copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    public void calcStp(View view) {
        textEnergy = requireView().findViewById(R.id.energy_value);
        textStp = requireView().findViewById(R.id.stp_text);
        TextView textUnit = requireView().findViewById(R.id.unitType);
        textDensity = requireView().findViewById(R.id.density_value);

        float energy;
        int err = dEdx.dedxLoadConfig(getProgramIdx(), getMaterialIdx(), getIonIdx());

        if (err != 0) {
            printErr(err);
            textStp.setText("Error");
        } else {
            if (!textEnergy.getText().toString().isEmpty()) {
                energy = Float.parseFloat(textEnergy.getText().toString());
            } else {
                energy = 0;
                textEnergy.setText("0");
            }

            float stp = dEdx.dedxGetStp(energy);

            if (stp > 0) {
                if (!textDensity.getText().toString().equals("")) {
                    float rho = Float.valueOf(textDensity.getText().toString());
                    resSTP = stp;
                    textStp.setText(dEdx.printFloat(stp * rho * (float) 0.1));
                    textUnit.setText("keV/\u00b5m");
                } else {
                    Toast.makeText(requireContext(), "Missing density of target", Toast.LENGTH_SHORT).show();
                }
            } else {
                printErr((int) (-1 * stp));
                textStp.setText("Error");
            }
        }
        calcCSDA(view);
    }

    public void calcCSDA(View view) {
        textEnergy = requireView().findViewById(R.id.energy_value);
        textCSDA = requireView().findViewById(R.id.csda_text);
        TextView textUnit = requireView().findViewById(R.id.unitTypeRange);
        textDensity = requireView().findViewById(R.id.density_value);
        float energy;
        int atomNum;

        int err = dEdx.dedxLoadConfig(getProgramIdx(), getMaterialIdx(), getIonIdx());

        if (err != 0) {
            printErr(err);
            textCSDA.setText("Error");
        } else {
            if (!textEnergy.getText().toString().isEmpty()) {
                energy = Float.parseFloat(textEnergy.getText().toString());
            } else {
                energy = 0;
                textEnergy.setText("0");
            }

            if (!atomValue.getText().toString().isEmpty()) {
                atomNum = Integer.valueOf(atomValue.getText().toString());
            } else {
                atomNum = 1;
                atomValue.setText("1");
            }

            if (energy <= 0 || energy >= 10106) {
                Toast.makeText(requireContext(), "Energy limit to low or high", Toast.LENGTH_LONG).show();
                textCSDA.setText("Error");
            } else {
                double csda = dEdx.dedxGetCSDARange(energy, atomNum);

                if (csda > 0) {
                    if (!textDensity.getText().toString().equals("")) {
                        float rho = Float.valueOf(textDensity.getText().toString());
                        resCSDA = csda;
                        textCSDA.setText(dEdx.printFloat((float) csda / rho));
                        textUnit.setText("cm");
                    } else {
                        Toast.makeText(requireContext(), "Missing density of target", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    printErr((int) (-1 * csda));
                    textCSDA.setText("Error");
                }
            }
        }
    }
}
