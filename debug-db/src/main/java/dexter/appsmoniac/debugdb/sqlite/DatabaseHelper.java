package dexter.appsmoniac.debugdb.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import dexter.appsmoniac.debugdb.model.Response;
import dexter.appsmoniac.debugdb.model.TableDataResponse;
import dexter.appsmoniac.debugdb.utils.Constants;
import dexter.appsmoniac.debugdb.utils.ConverterUtils;
import dexter.appsmoniac.debugdb.utils.DataType;
import dexter.appsmoniac.debugdb.utils.TableNameParser;

public class DatabaseHelper {

    private DatabaseHelper() {
        // not publicly instanstiated
    }

    public static Response getAllTableName(SQLiteDB database) {
        Response response = new Response();

        //  visit :   https://www.sqlite.org/faq.html   for more info about sqlite_master
        Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' OR type='view' ORDER BY name COLLATE NOCASE", null);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                response.rows.add(c.getString(0));
                c.moveToNext();
            }
        }
        c.close();
        response.isSuccessful = true;
        try {
            response.dbVersion = database.getVersion();
        } catch (Exception ignore) {

        }
        return response;
    }

    public static TableDataResponse getTableData(SQLiteDB db, String selectQuery, String tableName) {

        TableDataResponse tableData = new TableDataResponse();
        tableData.isSelectQuery = true;
        if (tableName == null) {
            tableName = getTableName(selectQuery);
        }

        final String quotedTableName = getQuotedTableName(tableName);

        if (tableName != null) {
            final String pragmaQuery = "PRAGMA table_info(" + quotedTableName + ")";
            tableData.tableInfos = getTableInfo(db, pragmaQuery);
        }
        Cursor cursor = null;
        boolean isView = false;
        try {
            cursor = db.rawQuery("SELECT type FROM sqlite_master WHERE name=?",
                    new String[]{quotedTableName});
            if (cursor.moveToFirst()) {
                isView = "view".equalsIgnoreCase(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        tableData.isEditable = tableName != null && tableData.tableInfos != null && !isView;


        if (!TextUtils.isEmpty(tableName)) {
            selectQuery = selectQuery.replace(tableName, quotedTableName);
        }

        try {

            //visit: https://stackoverflow.com/questions/10598137/rawqueryquery-selectionargs , to know more about rawqury

            cursor = db.rawQuery(selectQuery, null);

        } catch (Exception e) {
            e.printStackTrace();
            tableData.isSuccessful = false;
            tableData.errorMessage = e.getMessage();
            return tableData;
        }

        if (cursor != null) {
            cursor.moveToFirst();

            // setting tableInfo when tableName is not known and making
            // it non-editable also by making isPrimary true for all
            if (tableData.tableInfos == null) {
                tableData.tableInfos = new ArrayList<>();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    TableDataResponse.TableInfo tableInfo = new TableDataResponse.TableInfo();
                    tableInfo.title = cursor.getColumnName(i);
                    tableInfo.isPrimary = true;
                    tableData.tableInfos.add(tableInfo);
                }
            }

            tableData.isSuccessful = true;
            tableData.rows = new ArrayList<>();
            if (cursor.getCount() > 0) {

                do {
                    List<TableDataResponse.ColumnData> row = new ArrayList<>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        TableDataResponse.ColumnData columnData = new TableDataResponse.ColumnData();
                        switch (cursor.getType(i)) {
                            case Cursor.FIELD_TYPE_BLOB:
                                columnData.dataType = DataType.TEXT;
                                columnData.value = ConverterUtils.blobToString(cursor.getBlob(i));
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                columnData.dataType = DataType.REAL;
                                columnData.value = cursor.getDouble(i);
                                break;
                            case Cursor.FIELD_TYPE_INTEGER:
                                columnData.dataType = DataType.INTEGER;
                                columnData.value = cursor.getLong(i);
                                break;
                            case Cursor.FIELD_TYPE_STRING:
                                columnData.dataType = DataType.TEXT;
                                columnData.value = cursor.getString(i);
                                break;
                            default:
                                columnData.dataType = DataType.TEXT;
                                columnData.value = cursor.getString(i);
                        }
                        row.add(columnData);
                    }
                    tableData.rows.add(row);

                } while (cursor.moveToNext());
            }
            cursor.close();
            return tableData;
        } else {
            tableData.isSuccessful = false;
            tableData.errorMessage = "Cursor is null";
            return tableData;
        }

    }


    private static String getQuotedTableName(String tableName) {
        return String.format("[%s]", tableName);
    }

    private static List<TableDataResponse.TableInfo> getTableInfo(SQLiteDB db, String pragmaQuery) {

        Cursor cursor;
        try {
            cursor = db.rawQuery(pragmaQuery, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (cursor != null) {

            List<TableDataResponse.TableInfo> tableInfoList = new ArrayList<>();

            cursor.moveToFirst();

            if (cursor.getCount() > 0) {
                do {
                    TableDataResponse.TableInfo tableInfo = new TableDataResponse.TableInfo();

                    for (int i = 0; i < cursor.getColumnCount(); i++) {

                        final String columnName = cursor.getColumnName(i);

                        switch (columnName) {
                            case Constants.PK:
                                tableInfo.isPrimary = cursor.getInt(i) == 1;
                                break;
                            case Constants.NAME:
                                tableInfo.title = cursor.getString(i);
                                break;
                            default:
                        }

                    }
                    tableInfoList.add(tableInfo);

                } while (cursor.moveToNext());
            }
            cursor.close();
            return tableInfoList;
        }
        return null;
    }


    public static TableDataResponse exec(SQLiteDB database, String sql) {
        TableDataResponse tableDataResponse = new TableDataResponse();
        tableDataResponse.isSelectQuery = false;
        try {

            String tableName = getTableName(sql);

            if (!TextUtils.isEmpty(tableName)) {
                String quotedTableName = getQuotedTableName(tableName);
                sql = sql.replace(tableName, quotedTableName);
            }

            database.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
            tableDataResponse.isSuccessful = false;
            tableDataResponse.errorMessage = e.getMessage();
            return tableDataResponse;
        }
        tableDataResponse.isSuccessful = true;
        return tableDataResponse;
    }

    private static String getTableName(String selectQuery) {
        // TODO: Handle JOIN Query
        TableNameParser tableNameParser = new TableNameParser(selectQuery);
        HashSet<String> tableNames = (HashSet<String>) tableNameParser.tables();

        for (String tableName : tableNames) {
            if (!TextUtils.isEmpty(tableName)) {
                return tableName;
            }
        }
        return null;
    }
}
