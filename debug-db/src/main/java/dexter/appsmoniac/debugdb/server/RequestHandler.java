package dexter.appsmoniac.debugdb.server;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sqlcipher.database.SQLiteDatabase;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;

import dexter.appsmoniac.debugdb.model.Response;
import dexter.appsmoniac.debugdb.model.TableDataResponse;
import dexter.appsmoniac.debugdb.sqlite.DatabaseFileProvider;
import dexter.appsmoniac.debugdb.sqlite.DatabaseHelper;
import dexter.appsmoniac.debugdb.sqlite.DebugSQLiteDB;
import dexter.appsmoniac.debugdb.sqlite.InMemoryDebugSQLiteDB;
import dexter.appsmoniac.debugdb.sqlite.SQLiteDB;
import dexter.appsmoniac.debugdb.utils.Constants;
import dexter.appsmoniac.debugdb.utils.PrefHelper;
import dexter.appsmoniac.debugdb.utils.Utils;

public class RequestHandler {

    private final Context mContext;
    private final Gson mGson;
    private final AssetManager mAssets;
    private boolean isDbOpened;
    private SQLiteDB sqLiteDB;
    private HashMap<String, Pair<File, String>> mDatabaseFiles;
    private HashMap<String, Pair<File, String>> mCustomDatabaseFiles;
    private String mSelectedDatabase = null;
    private HashMap<String, SupportSQLiteDatabase> mRoomInMemoryDatabases = new HashMap<>();

    public RequestHandler(Context context) {
        mContext = context;
        mAssets = context.getResources().getAssets();
        mGson = new GsonBuilder().serializeNulls().create();
    }

    public void handle(Socket socket) throws IOException {
        BufferedReader reader = null;
        PrintStream output = null;
        try {
            String route = null;

            // Read HTTP headers and parse out the route.
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while (!TextUtils.isEmpty(line = reader.readLine())) {
                if (line.startsWith(Constants.GET_ROUTE)) {
                    int start = line.indexOf('/') + 1;
                    int end = line.indexOf(' ', start);
                    route = line.substring(start, end);
                    break;
                }
            }

            Log.e("Line: ", line);
            Log.e("Route: ", route);

            // Output stream that we send the response to
            output = new PrintStream(socket.getOutputStream());

            if (route == null || route.isEmpty()) {
                route = Constants.INDEX_HTML_FILE;
            }

            byte[] bytes;

            if (route.startsWith(Constants.GET_DB_LIST)) {
                final String response = getDBListResponse();

                //need to get in byte[] as it's needed to write in socket outputstream.
                bytes = response.getBytes();
            } else if (route.startsWith(Constants.GET_ALL_DATA_FROM_TABLE)) {

                final String response = getAllDataFromTheTableResponse(route);
                bytes = response.getBytes();
                Log.e(Constants.GET_ALL_DATA_FROM_TABLE + " data: ", response);

            } else if (route.startsWith(Constants.GET_TABLE_LIST)) {

                final String response = getTableListResponse(route);
                bytes = response.getBytes();

                Log.e(Constants.GET_TABLE_LIST + " data: ", response);

            }else if (route.startsWith(Constants.DOWNLOAD_DB)) {
                bytes = Utils.getDatabase(mSelectedDatabase, mDatabaseFiles);
            } else {
                //here we got the bytes[] equivalent of the index.html file.
                bytes = Utils.loadContent(route, mAssets);
            }

            if (null == bytes) {
                writeServerError(output);
                return;
            }

            // Send out the content.
            output.println("HTTP/1.0 200 OK");
            output.println("Content-Type: " + Utils.detectMimeType(route));

            if (route.startsWith(Constants.DOWNLOAD_DB)) {
                output.println("Content-Disposition: attachment; filename=" + mSelectedDatabase);
            } else {
                output.println("Content-Length: " + bytes.length);
            }
            output.println();

            //refer : https://www.tutorialspoint.com/java/io/printstream_write_byte_len.html
            output.write(bytes);
            output.flush();


        } finally {
            try {
                if (null != output) {
                    output.close();
                }
                if (null != reader) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getAllDataFromTheTableResponse(String route) {

        String tableName = null;

        if (route.contains("?tableName=")) {
            tableName = route.substring(route.indexOf("=") + 1, route.length());
        }

        TableDataResponse response;

        if (isDbOpened) {
            String sql = "SELECT * FROM " + tableName;
            response = DatabaseHelper.getTableData(sqLiteDB, sql, tableName);
        } else {
            response = PrefHelper.getAllPrefData(mContext, tableName);
        }

        return mGson.toJson(response);

    }

    private String getDBListResponse() {
        mDatabaseFiles = DatabaseFileProvider.getDatabaseFiles(mContext);
        if (mCustomDatabaseFiles != null) {
            mDatabaseFiles.putAll(mCustomDatabaseFiles);
        }
        Response response = new Response();
        if (mDatabaseFiles != null) {
            for (HashMap.Entry<String, Pair<File, String>> entry : mDatabaseFiles.entrySet()) {
                String[] dbEntry = {entry.getKey(), !entry.getValue().second.equals("") ? "true" : "false", "true"};
                response.rows.add(dbEntry);
            }
        }
//        if (mRoomInMemoryDatabases != null) {
//            for (HashMap.Entry<String, SupportSQLiteDatabase> entry : mRoomInMemoryDatabases.entrySet()) {
//                String[] dbEntry = {entry.getKey(), "false", "false"};
//                response.rows.add(dbEntry);
//            }
//        }
        response.rows.add(new String[]{Constants.APP_SHARED_PREFERENCES, "false", "false"});
        response.isSuccessful = true;
        return mGson.toJson(response);
    }


    private String getTableListResponse(String route) {
        String database = null;
        if (route.contains("?database=")) {
            database = route.substring(route.indexOf("=") + 1, route.length());
        }

        Response response;


        openDatabase(database);
        response = DatabaseHelper.getAllTableName(sqLiteDB);
        mSelectedDatabase = database;

        return mGson.toJson(response);
    }

    private void openDatabase(String database) {
        closeDatabase();
        if (mRoomInMemoryDatabases.containsKey(database)) {
            sqLiteDB = new InMemoryDebugSQLiteDB(mRoomInMemoryDatabases.get(database));
        } else {
            File databaseFile = mDatabaseFiles.get(database).first;
            String password = mDatabaseFiles.get(database).second;
            SQLiteDatabase.loadLibs(mContext);
            sqLiteDB = new DebugSQLiteDB(SQLiteDatabase.openOrCreateDatabase(databaseFile.getAbsolutePath(), password, null));
        }
        isDbOpened = true;
    }

    private void closeDatabase() {
        if (sqLiteDB != null && sqLiteDB.isOpen()) {
            sqLiteDB.close();
        }
        sqLiteDB = null;
        isDbOpened = false;
    }


    public void setCustomDatabaseFiles(HashMap<String, Pair<File, String>> customDatabaseFiles) {
        mCustomDatabaseFiles = customDatabaseFiles;
    }

    private void writeServerError(PrintStream output) {
        output.println("HTTP/1.0 500 Internal Server Error");
        output.flush();
    }
}