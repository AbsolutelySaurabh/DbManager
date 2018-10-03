package dexter.appsmoniac.debugdb.utils;

import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class Utils {

    private static final String TAG = "Utils";

    private Utils() {
        // not publicly instantiated
    }

    public static String detectMimeType(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        } else if (fileName.endsWith(".html")) {
            return "text/html";
        } else if (fileName.endsWith(".js")) {
            return "application/javascript";
        } else if (fileName.endsWith(".css")) {
            return "text/css";
        } else {
            return "application/octet-stream";
        }
    }

    public static byte[] loadContent(String fileName, AssetManager assetManager) throws IOException {
        InputStream input = null;
        try {
            //refer : https://www.javatpoint.com/java-bytearrayoutputstream-class
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            input = assetManager.open(fileName);
            byte[] buffer = new byte[1024];
            int size;
            while (-1 != (size = input.read(buffer))) {

                //write to the bytearray
                output.write(buffer, 0, size);
            }
            output.flush();

            //this give a copy of the bytearray
            return output.toByteArray();
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            try {
                if (null != input) {
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
