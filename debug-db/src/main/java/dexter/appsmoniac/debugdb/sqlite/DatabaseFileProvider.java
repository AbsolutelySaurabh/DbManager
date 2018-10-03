package dexter.appsmoniac.debugdb.sqlite;

import android.content.Context;
import android.util.Pair;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;

public class DatabaseFileProvider {

    private final static String DB_PASSWORD_RESOURCE = "DB_PASSWORD_{0}";

    private DatabaseFileProvider() {
        //not publicly instantiated
    }

    public static HashMap<String, Pair<File, String>> getDatabaseFiles(Context context) {
        HashMap<String, Pair<File, String>> databaseFiles = new HashMap<>();
        try {


            //context.databaseList : Returns an array of strings naming the private databases associated with this Context's application package.
            for (String databaseName : context.databaseList()) {
                String password = getDbPasswordFromStringResources(context, databaseName);
                databaseFiles.put(databaseName, new Pair<>(context.getDatabasePath(databaseName), password));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return databaseFiles;
    }

    private static String getDbPasswordFromStringResources(Context context, String name) {
        String nameWithoutExt = name;
        if (nameWithoutExt.endsWith(".db")) {
            nameWithoutExt = nameWithoutExt.substring(0, nameWithoutExt.lastIndexOf('.'));
        }
        String resourceName = MessageFormat.format(DB_PASSWORD_RESOURCE, nameWithoutExt.toUpperCase());
        String password = "";

        int resourceId = context.getResources().getIdentifier(resourceName, "string", context.getPackageName());

        if (resourceId != 0) {
            password = context.getString(resourceId);
        }
        return password;
    }
}
