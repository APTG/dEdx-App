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

public class InverseFragment extends Fragment {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.inverse_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // No-arg constructor intentional: MainActivity has already called DedxAPI(Context)
        // which runs Utils.copyToAssets() and dedxInit(). Fragments only use the native state.
        // TODO(11-4): replace with internal storage init so this dependency is explicit.
        dEdx = new DedxAPI();

        List<DedxIdxName> programList = dEdx.dedxGetProgramList();
        programSpinner = view.findViewById(R.id.program_spinner_i);
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

        ionSpinner = view.findViewById(R.id.ion_spinner_i);
        ionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                updateAtom(ionAdaptor.getSelectedIdx(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        view.findViewById(R.id.unitTypeRange_i).setOnClickListener(this::changeUnitsRange);
        view.findViewById(R.id.calc_inverse_csda).setOnClickListener(this::calcInverseCSDA);
        view.findViewById(R.id.energy_text_i).setOnClickListener(this::copyResult);
    }

    private void updateDensity() {
        textDensity = requireView().findViewById(R.id.density_value_i);
        int err = dEdx.dedxLoadConfig(getProgramIdx(), getMaterialIdx(), getIonIdx());
        if (err != 0) {
            printErr(err);
        } else {
            float density = dEdx.dedxGetDensity();
            textDensity.setText(dEdx.printFloatDensity(density));
        }
    }

    private void updateAtom(int ion) {
        atomValue = requireView().findViewById(R.id.atom_value_i);
        atomValue.setText(Integer.toString(dEdx.getAtomNum(ion)));
    }

    private void updateSpinner(int program) {
        List<DedxIdxName> ionList = dEdx.dedxGetIons(program);
        ionAdaptor = new DedxIdxNameAdapter(requireContext(), ionList);
        ionAdaptor.notifyDataSetChanged();
        ionSpinner = requireView().findViewById(R.id.ion_spinner_i);
        ionSpinner.setAdapter(ionAdaptor);
        ionSpinner.setPrompt("ION");

        List<DedxIdxName> materialList = dEdx.dedxGetMaterials(program);
        materialAdaptor = new DedxIdxNameAdapter(requireContext(), materialList);
        materialAdaptor.notifyDataSetChanged();
        materialSpinner = requireView().findViewById(R.id.material_spinner_i);
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
        textCSDA = requireView().findViewById(R.id.range_value);
        textDensity = requireView().findViewById(R.id.density_value_i);
        TextView textUnit = requireView().findViewById(R.id.unitTypeRange_i);
        float range = 0;

        if (!textCSDA.getText().toString().equals(""))
            range = Float.valueOf(textCSDA.getText().toString());

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
    }

    public void copyResult(View v) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("copyRes", Double.toString(result)));
        Toast.makeText(requireContext(), "Result copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    public void calcInverseCSDA(View v) {
        textCSDA = requireView().findViewById(R.id.range_value);
        textEnergy_i = requireView().findViewById(R.id.energy_text_i);
        TextView textUnit = requireView().findViewById(R.id.unitTypeRange_i);
        textDensity = requireView().findViewById(R.id.density_value_i);

        float stp;
        int atomNum;
        float rho;

        int err = dEdx.dedxLoadConfig(getProgramIdx(), getMaterialIdx(), getIonIdx());

        if (err != 0) {
            printErr(err);
            textEnergy_i.setText("Error");
        } else {
            if (!textCSDA.getText().toString().isEmpty()) {
                stp = Float.parseFloat(textCSDA.getText().toString());
            } else {
                stp = 0;
                textCSDA.setText("0");
            }

            if (!atomValue.getText().toString().isEmpty()) {
                atomNum = Integer.valueOf(atomValue.getText().toString());
            } else {
                atomNum = 1;
                atomValue.setText("1");
            }

            if (!textDensity.getText().toString().equals("")) {
                rho = Float.valueOf(textDensity.getText().toString());

                if (textUnit.getText().toString().equals("cm"))
                    stp = stp * rho;

                double energy = dEdx.dedxGetInverseCSDA(stp, atomNum);

                if (stp >= 0) {
                    result = energy;
                    textEnergy_i.setText(dEdx.printFloat((float) energy));
                } else {
                    printErr((int) (-1 * stp));
                    textEnergy_i.setText("Error");
                }
            } else {
                Toast.makeText(requireContext(), "Missing density of target", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
