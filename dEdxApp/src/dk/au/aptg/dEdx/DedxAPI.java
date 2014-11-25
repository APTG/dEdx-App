package dk.au.aptg.dEdx;

import java.util.List;

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