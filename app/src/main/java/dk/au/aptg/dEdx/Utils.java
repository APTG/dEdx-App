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
    public static void copyDataFiles(Context context) throws IOException {
        AssetManager assetMan = context.getAssets();
        String[] files = assetMan.list("data");
        if (files == null) {
            throw new IOException("Asset directory 'data' not found");
        }
        for (String filename : files) {
            File outFile = new File(context.getFilesDir(), filename);
            if (outFile.exists()) {
                Log.d("copyDataFiles", "Skip existing: " + outFile.getName());
                continue;
            }
            try (InputStream in = assetMan.open("data/" + filename);
                 OutputStream out = new FileOutputStream(outFile)) {
                Log.d("copyDataFiles", "Copy: data/" + filename + " to " + outFile.getParent());
                copyFile(in, out);
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
