package dexter.appsmoniac.debugdb.sqlite;

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

public class DatabaseHelper {

    private DatabaseHelper() {
        // not publicly instantiated
    }

    public static Response getAllTableName(SQLiteDB database) {
        Response response = new Response();

        //  visit :   https://www.sqlite.org/faq.html   for more info about sqlite_master
        //COLLATE NO CASE :: helps sqlite3 to be case insensitive, while matching string names
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

        final String quotedTableName = getQuotedTableName(tableName);

        if (tableName != null) {

            /**
             * {@PRAGMA_QUERY https://www.safaribooksonline.com/library/view/using-sqlite/9781449394592/re205.html
             *
             * This query is useful for getting table info, about rows and columns(data type, etc.)
             */

            final String pragmaQuery = "PRAGMA table_info(" + quotedTableName + ")";
            //get the table info, like title, etc.
            tableData.tableInfos = getTableInfo(db, pragmaQuery);
        }
        Cursor cursor = null;
        boolean isView = false;
        try {
            //rawQuery is generally used for SELECT command, and execSql is used for other commnds like CREATE TABLE, etc
            /**
             * {@SELECT type FROM sqlite_master WHERE name=?
             *
             *  type : 'table' --> also, checks whether table exists or not
             *
             *  We can fetch the specific table schema by querying the SQLITE_MASTER table
             */
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


        if (!TextUtils.isEmpty(tableName)) {
            selectQuery = selectQuery.replace(tableName, quotedTableName);
        }

        try {

            //rawQuery is generally used for SELECT command, and execSql is used for other commnds like CREATE TABLE, etc
            cursor = db.rawQuery(selectQuery, null);

        } catch (Exception e) {
            e.printStackTrace();
            tableData.isSuccessful = false;
            tableData.errorMessage = e.getMessage();
            return tableData;
        }


        /**
         * {@ABOUT_BELOW_METHOD
         *
         * The below method, will query over the table, via the cursor, which we got from the rawQuery(selectStatement)
         * and adding, the column data type and data value
         *
         *
         */
        if (cursor != null) {
            cursor.moveToFirst();

            // setting tableInfo when tableName is not known and making
            // it non-editable
            if (tableData.tableInfos == null) {
                tableData.tableInfos = new ArrayList<>();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    TableDataResponse.TableInfo tableInfo = new TableDataResponse.TableInfo();
                    tableInfo.title = cursor.getColumnName(i);
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
                            //BLOB. The value is a blob of data, stored exactly as it was input.
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
}
