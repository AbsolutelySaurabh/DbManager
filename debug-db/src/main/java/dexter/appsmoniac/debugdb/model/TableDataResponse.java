package dexter.appsmoniac.debugdb.model;

import java.util.List;
public class TableDataResponse {

    public List<TableInfo> tableInfos;
    public boolean isSuccessful;
    public List<Object> rows;
    public String errorMessage;
    public boolean isEditable;

    //this static nested class cannot access non-static outer classes.
    public static class TableInfo {
        public String title;
    }

    public static class ColumnData {
        public String dataType;
        public Object value;
    }

}
