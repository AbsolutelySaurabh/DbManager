package dexter.appsmoniac.androidsocket;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.content.Context;
import android.util.Pair;
import android.widget.Toast;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;

import dexter.appsmoniac.androidsocket.database.ExtTestDBHelper;

public class Utils {

    private Utils() {
        // not publicly instantiated
    }

    public static void showDebugDBAddressLogToast(Context context) {
        if (BuildConfig.DEBUG) {
            try {
                //access that DebugDb class in debugdb module
                Class<?> debugDB = Class.forName("dexter.appsmoniac.debugdb.utils.DebugDB");
                //access the getAddressLog, and get the ip address to print/show
                Method getAddressLog = debugDB.getMethod("getAddressLog");
                Object value = getAddressLog.invoke(null);
                // show, this toast to open this url on browser
                Toast.makeText(context, (String) value, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setCustomDatabaseFiles(Context context) {
        if (BuildConfig.DEBUG) {
            try {
                Class<?> debugDB = Class.forName("dexter.appsmoniac.debugdb.utils.DebugDB");
                Class[] argTypes = new Class[]{HashMap.class};
                Method setCustomDatabaseFiles = debugDB.getMethod("setCustomDatabaseFiles", argTypes);
                HashMap<String, Pair<File, String>> customDatabaseFiles = new HashMap<>();
                // set your custom database files
                customDatabaseFiles.put(ExtTestDBHelper.DATABASE_NAME,
                        new Pair<>(new File(context.getFilesDir() + "/" + ExtTestDBHelper.DIR_NAME +
                                "/" + ExtTestDBHelper.DATABASE_NAME), ""));
                //invoke and call this method in DebugDB class
                setCustomDatabaseFiles.invoke(null, customDatabaseFiles);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
