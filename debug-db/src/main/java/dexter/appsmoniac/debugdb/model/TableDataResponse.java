package dexter.appsmoniac.debugdb.model;

import java.util.List;
public class TableDataResponse {

    public List<TableInfo> tableInfos;
    public boolean isSuccessful;
    public List<Object> rows;
    public String errorMessage;
    public boolean isEditable;
    public boolean isSelectQuery;

    public static class TableInfo {
        public String title;
        public boolean isPrimary;
    }

    public static class ColumnData {
        public String dataType;
        public Object value;
    }

}
