package dk.au.aptg.dEdx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class Utils {
    public static void copyDataFiles(Context context) {
        AssetManager assetMan = context.getAssets();
        String[] files = null;
        try {
            files = assetMan.list("data");
        } catch (IOException e) {
            Log.e("copyDataFiles", "Failed to get asset file list.", e);
        }
        for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetMan.open("data/" + filename);
                File outFile = new File(context.getFilesDir(), filename);
                out = new FileOutputStream(outFile);
                Log.d("copyDataFiles", "Copy: data/" + filename + " to " + outFile.getParent());
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (IOException e) {
                Log.e("copyDataFiles", "Failed to copy asset file: " + filename, e);
            }
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
