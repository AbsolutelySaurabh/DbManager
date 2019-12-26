package dexter.appsmoniac.debugdb.model;

import java.util.ArrayList;
import java.util.List;

public class Response {
    public List<Object> rows = new ArrayList<>();
    public List<String> columns = new ArrayList<>();
    public boolean isSuccessful;
    public String error;
    public int dbVersion;

    public Response() {

    }
}
