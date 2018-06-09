package dexter.appsmoniac.debugdb.utils;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dexter.appsmoniac.debugdb.model.Response;
import dexter.appsmoniac.debugdb.model.TableDataResponse;

public class PrefHelper {

    private static final String PREFS_SUFFIX = ".xml";

    private PrefHelper() {
        // not publicly instantiated
    }

    public static List<String> getSharedPreferenceTags(Context context) {

        ArrayList<String> tags = new ArrayList<>();

        String rootPath = context.getApplicationInfo().dataDir + "/shared_prefs";
        File root = new File(rootPath);
        if (root.exists()) {
            for (File file : root.listFiles()) {
                String fileName = file.getName();
                if (fileName.endsWith(PREFS_SUFFIX)) {
                    tags.add(fileName.substring(0, fileName.length() - PREFS_SUFFIX.length()));
                }
            }
        }

        Collections.sort(tags);

        return tags;
    }

    public static Response getAllPrefTableName(Context context) {

        Response response = new Response();

        List<String> prefTags = getSharedPreferenceTags(context);

        for (String tag : prefTags) {
            response.rows.add(tag);
        }

        response.isSuccessful = true;

        return response;
    }

    public static TableDataResponse getAllPrefData(Context context, String tag) {

        TableDataResponse response = new TableDataResponse();
        response.isEditable = true;
        response.isSuccessful = true;
        response.isSelectQuery = true;

        TableDataResponse.TableInfo keyInfo = new TableDataResponse.TableInfo();
        keyInfo.isPrimary = true;
        keyInfo.title = "Key";

        TableDataResponse.TableInfo valueInfo = new TableDataResponse.TableInfo();
        valueInfo.isPrimary = false;
        valueInfo.title = "Value";

        response.tableInfos = new ArrayList<>();
        response.tableInfos.add(keyInfo);
        response.tableInfos.add(valueInfo);

        response.rows = new ArrayList<>();

        SharedPreferences preferences = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = preferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            List<TableDataResponse.ColumnData> row = new ArrayList<>();
            TableDataResponse.ColumnData keyColumnData = new TableDataResponse.ColumnData();
            keyColumnData.dataType = DataType.TEXT;
            keyColumnData.value = entry.getKey();

            row.add(keyColumnData);

            TableDataResponse.ColumnData valueColumnData = new TableDataResponse.ColumnData();
            valueColumnData.value = entry.getValue().toString();
            if (entry.getValue() != null) {
                if (entry.getValue() instanceof String) {
                    valueColumnData.dataType = DataType.TEXT;
                } else if (entry.getValue() instanceof Integer) {
                    valueColumnData.dataType = DataType.INTEGER;
                } else if (entry.getValue() instanceof Long) {
                    valueColumnData.dataType = DataType.LONG;
                } else if (entry.getValue() instanceof Float) {
                    valueColumnData.dataType = DataType.FLOAT;
                } else if (entry.getValue() instanceof Boolean) {
                    valueColumnData.dataType = DataType.BOOLEAN;
                } else if (entry.getValue() instanceof Set) {
                    valueColumnData.dataType = DataType.STRING_SET;
                }
            } else {
                valueColumnData.dataType = DataType.TEXT;
            }
            row.add(valueColumnData);
            response.rows.add(row);
        }
        return response;

    }
}
