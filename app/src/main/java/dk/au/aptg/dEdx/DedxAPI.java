package dk.au.aptg.dEdx;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;



public class DedxAPI {
	private int atomList[] = {1,4,7,9,11,12,14,16,19,20,23,24,27,28,31,32,35,40,39,40,45,48,51,52,55,56,59,58,63,64,69,74,75,80,79,84,85,88,89,90,93,98};

	public DedxAPI(Context context) {
		Utils.copyToAssets(context);
		dedxInit(context.getExternalFilesDir(null).getPath() + "/");
	}

	public DedxAPI() {}

	public int getAtomNum(int ion) {
		return ion > 42 ? 1 : atomList[ion-1];
	}

	public String printFloat(float f) {
		/* 4 significant digits  - use with stopping power, range and energy */
		if ((f < 1000.0) && (f >= 100.0))
			return String.format(Locale.US, "%.1f", f);
		else if ((f < 100.0) && (f >= 10.0))
			return String.format(Locale.US, "%.2f", f);
		else if ((f < 10.0) && (f >= 1.0))
			return String.format(Locale.US, "%.3f", f);
		else if ((f < 1.0) && (f >= 0.1))
			return String.format(Locale.US, "%.4f", f);
		else if ((f < 0.1) && (f >= 0.01))
			return String.format(Locale.US, "%.5f", f);
		else
			return String.format(Locale.US, "%.3e", f);
	}

	public String printFloatDensity(float f) {
		/* 7 significant digits - use only for density */
		if ((f < 1000.0) && (f >= 100.0))
			return String.format(Locale.US, "%.4f", f);
		else if ((f < 100.0) && (f >= 10.0))
			return String.format(Locale.US, "%.5f", f);
		else if ((f < 10.0) && (f >= 1.0))
			return String.format(Locale.US, "%.6f", f);
		else if ((f < 1.0) && (f >= 0.1))
			return String.format(Locale.US, "%.7f", f);
		else if ((f < 0.1) && (f >= 0.01))
			return String.format(Locale.US, "%.8f", f);
		else
			return String.format(Locale.US, "%.6e", f);
	}

	/*
	 * A native method that is implemented by the 'dedx' native library,
	 * which is packaged with this application.
	 */

	public native int dedxInit(String path);
	public native int dedxExit();
	public native int dedxLoadConfig(int program, int target, int ion);
	public native String dedxGetErrorMsg(int err);
	public native float dedxGetStp(float energy);
	public native float dedxGetDensity();
	public native double dedxGetCSDARange(float energy, int ion_a);
	public native double dedxGetInverseCSDA(float stp, int ion_a);
	public native List<DedxIdxName> dedxGetProgramList();
	public native List<DedxIdxName> dedxGetIons(int position);
	public native List<DedxIdxName> dedxGetMaterials(int position);

	/*
	 * this is used to load the 'dedx' library on application startup.
	 */
	static {
		System.loadLibrary("dEdx");
	}
}