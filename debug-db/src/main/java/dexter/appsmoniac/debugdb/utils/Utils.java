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

    public static byte[] getDatabase(String selectedDatabase, HashMap<String, Pair<File, String>> databaseFiles) {
        if (TextUtils.isEmpty(selectedDatabase) || !databaseFiles.containsKey(selectedDatabase)) {
            return null;
        }

        byte[] byteArray = new byte[0];
        try {
            File file = databaseFiles.get(selectedDatabase).first;

            byteArray = null;
            try {
                InputStream inputStream = new FileInputStream(file);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] b = new byte[(int) file.length()];
                int bytesRead;

                while ((bytesRead = inputStream.read(b)) != -1) {
                    bos.write(b, 0, bytesRead);
                }

                byteArray = bos.toByteArray();
            } catch (IOException e) {
                Log.e(TAG, "getDatabase: ", e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return byteArray;
    }

}
