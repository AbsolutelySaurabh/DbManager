package dexter.appsmoniac.debugdb.utils;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import java.io.File;
import java.util.HashMap;

import dexter.appsmoniac.debugdb.R;
import dexter.appsmoniac.debugdb.server.ClientServer;

public class DebugDB {

    private static final String TAG = DebugDB.class.getSimpleName();
    private static final int DEFAULT_PORT = 8080;
    private static ClientServer clientServer;
    private static String addressLog = "not available";

    private DebugDB() {}

    public static void initialize(Context context) {
        int portNumber;

        try {
            portNumber = Integer.valueOf(context.getString(R.string.PORT_NUMBER));

        } catch (NumberFormatException ex) {
            Log.e(TAG, "PORT_NUMBER should be integer", ex);
            portNumber = DEFAULT_PORT;
            Log.i(TAG, "Using Default port : " + DEFAULT_PORT);
        }

        clientServer = new ClientServer(context, portNumber);
        clientServer.start();
        addressLog = NetworkUtils.getAddressLog(context, portNumber);
        Log.d(TAG, addressLog);
    }

    public static String getAddressLog() {
        Log.d(TAG, addressLog);
        return addressLog;
    }

    public static void shutDown() {
        if (clientServer != null) {
            clientServer.stop();
            clientServer = null;
        }
    }

    public static void setCustomDatabaseFiles(HashMap<String, Pair<File, String>> customDatabaseFiles) {
        if (clientServer != null) {
            clientServer.setCustomDatabaseFiles(customDatabaseFiles);
        }
    }

    public static boolean isServerRunning() {
        return clientServer != null && clientServer.isRunning();
    }

}
